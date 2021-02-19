/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.xml.XMLNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tts.config.SBML4jConfig;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.HelperQualSpeciesReturn;
import org.tts.model.common.NameNode;
import org.tts.model.common.SBMLCompartment;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLQualSpeciesGroup;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.common.SBMLSpeciesGroup;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.simple.SBMLSimpleReaction;
import org.tts.model.simple.SBMLSimpleTransition;
import org.tts.model.warehouse.FileNode;
import org.tts.repository.common.BiomodelsQualifierRepository;
import org.tts.repository.common.ExternalResourceEntityRepository;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLQualSpeciesRepository;
import org.tts.repository.common.SBMLSBaseEntityRepository;
import org.tts.repository.common.SBMLSpeciesRepository;
import org.tts.repository.simpleModel.SBMLSimpleReactionRepository;
import org.tts.repository.simpleModel.SBMLSimpleTransitionRepository;
import org.tts.service.SimpleSBML.SBMLSpeciesService;

@Service
public class SBMLSimpleModelServiceImpl implements SBMLService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	SBMLSpeciesService sbmlSpeciesService;
	
	
	SBMLSpeciesRepository sbmlSpeciesRepository;
	SBMLSimpleReactionRepository sbmlSimpleReactionRepository;
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	SBMLQualSpeciesRepository sbmlQualSpeciesRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	ExternalResourceEntityRepository externalResourceEntityRepository;
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	GraphBaseEntityRepository graphBaseEntityRepository;
	HttpService httpService;
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	ProvenanceGraphService provenanceGraphService;
	UtilityService utilityService;
	NameNodeService nameNodeService;
	SBML4jConfig sbml4jConfig;
	
	int SAVE_DEPTH = 1;

	@Autowired
	public SBMLSimpleModelServiceImpl(SBMLSpeciesRepository sbmlSpeciesRepository,
			SBMLSimpleReactionRepository sbmlSimpleReactionRepository,
			SBMLSBaseEntityRepository sbmlSBaseEntityRepository, SBMLQualSpeciesRepository sbmlQualSpeciesRepository,
			SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository,
			ExternalResourceEntityRepository externalResourceEntityRepository,
			BiomodelsQualifierRepository biomodelsQualifierRepository,
			GraphBaseEntityRepository graphBaseEntityRepository,
			HttpService httpService,
			SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl,
			ProvenanceGraphService provenanceGraphService,
			UtilityService utilityService,
			NameNodeService nameNodeService,
			SBML4jConfig sbml4jConfig) {
		super();
		this.sbmlSpeciesRepository = sbmlSpeciesRepository;
		this.sbmlSimpleReactionRepository = sbmlSimpleReactionRepository;
		this.sbmlSBaseEntityRepository = sbmlSBaseEntityRepository;
		this.sbmlQualSpeciesRepository = sbmlQualSpeciesRepository;
		this.sbmlSimpleTransitionRepository = sbmlSimpleTransitionRepository;
		this.externalResourceEntityRepository = externalResourceEntityRepository;
		this.biomodelsQualifierRepository = biomodelsQualifierRepository;
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.httpService = httpService;
		this.sbmlSimpleModelUtilityServiceImpl = sbmlSimpleModelUtilityServiceImpl;
		this.provenanceGraphService = provenanceGraphService;
		this.utilityService = utilityService;
		this.nameNodeService = nameNodeService;
		this.sbml4jConfig = sbml4jConfig;
	}

	private List<SBMLCompartment> getCompartmentList(Model model, ProvenanceGraphActivityNode persistActivity) {
		List<SBMLCompartment> sbmlCompartmentList = new ArrayList<>();
		for(Compartment compartment : model.getListOfCompartments()) {
			//SBMLCompartment sbmlCompartment = new SBMLCompartment();
			// check if a compartment with that name (and those settings does already exist)
			//spatialDimensions, size, constant, sBaseName, sBaseId
			try {
				SBMLCompartment existingCompartment = (SBMLCompartment) this.sbmlSBaseEntityRepository.findBysBaseId(compartment.getId(), 2);
				if (existingCompartment != null 
						&& existingCompartment.getsBaseName().equals(compartment.getName())
						//&& existingCompartment.getSize() == compartment.getSize()
						//&& existingCompartment.getSpatialDimensions() == compartment.getSpatialDimensions()
						//&& existingCompartment.isConstant() == compartment.getConstant() 
						) {
					// they are obviously the same, so take the one already persisted
					//sbmlCompartment = existingCompartment;
					sbmlCompartmentList.add(existingCompartment);
					this.provenanceGraphService.connect(persistActivity, existingCompartment, ProvenanceGraphEdgeType.used);
					// other cases would be, that the name and Id are the same, but are actually different compartments
					// with different properties.
					// then append the name of the new compartment with a number (or something) and link all entities in this
					// model to that new one.
					// This means that further down, when looking up compartment, we can no longer match by sbaseId directly
					// but also need a mapping info from the original sbasid (that all entities in jsbml will reference)
					// to the newly given name.
					// but for now, as kegg only has one default compartment and it's the only time we load more than one model
					// we keep it like that.
				} else {
					SBMLCompartment sbmlCompartment = new SBMLCompartment();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(sbmlCompartment);
					this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(compartment, sbmlCompartment);
					this.sbmlSimpleModelUtilityServiceImpl.setCompartmentProperties(compartment, sbmlCompartment);
					sbmlCompartment = this.sbmlSBaseEntityRepository.save(sbmlCompartment, SAVE_DEPTH);
					sbmlCompartmentList.add(sbmlCompartment);
					this.provenanceGraphService.connect(sbmlCompartment, persistActivity, ProvenanceGraphEdgeType.wasGeneratedBy);
					
				}
			} catch (ClassCastException e) {	
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " is not of type SBMLCompartment! Aborting...");
				return null; // need to find better error handling way..
			} catch (IncorrectResultSizeDataAccessException e) {
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " was found multiple times");
				return null; // need to find better error handling way..
			}
		}
		return sbmlCompartmentList;
	}

	private Map<String, SBMLSpecies> buildAndPersistSBMLSpecies(
			ListOf<Species> speciesListOf,
			Map<String, SBMLCompartment> compartmentLookupMap, 
			ProvenanceGraphActivityNode activityNode) {
		List<SBMLSpecies> speciesList = new ArrayList<>();
		List<Species> groupSpeciesList = new ArrayList<>();
		Map<String, SBMLSpecies> sBaseNameToSBMLSpeciesMap = new HashMap<>();
		for (Species species : speciesListOf) {
			if(species.getName().equals("Group")) {
				//logger.debug("found group");
				groupSpeciesList.add(species);
			} else {
				SBMLSpecies newSpecies = new SBMLSpecies();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(species, newSpecies);
				// uncomment to connect entities to compartments
				this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(species, newSpecies, compartmentLookupMap);
				this.sbmlSimpleModelUtilityServiceImpl.setSpeciesProperties(species, newSpecies);
				
				SBMLSpecies persistedNewSpecies = this.sbmlSpeciesRepository.save(newSpecies, SAVE_DEPTH);
				persistedNewSpecies.setCvTermList(species.getCVTerms());
				newSpecies = (SBMLSpecies) buildAndPersistExternalResourcesForSBaseEntity(persistedNewSpecies, activityNode);
				SBMLSpecies persistedSpeciesWithExternalResources = this.sbmlSpeciesRepository.save(newSpecies, SAVE_DEPTH);
				speciesList.add(persistedSpeciesWithExternalResources);
				sBaseNameToSBMLSpeciesMap.put(newSpecies.getsBaseName(), newSpecies);
				this.provenanceGraphService.connect(persistedSpeciesWithExternalResources, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			}
		}
		// now build the groups nodes
		for (Species species : groupSpeciesList) {
			SBMLSpeciesGroup newSBMLSpeciesGroup = new SBMLSpeciesGroup();
			List<String> groupMemberSymbols = new ArrayList<>();
			XMLNode ulNode = species.getNotes().getChildElement("body", "http://www.w3.org/1999/xhtml").getChildElement("p", null).getChildElement("ul", null);
			for (int i = 0; i != ulNode.getChildCount(); i++) {
				if(ulNode.getChild(i).getChildCount() > 0) {
					for (int j = 0; j != ulNode.getChild(i).getChildCount(); j++) {
						String chars = ulNode.getChild(i).getChild(j).getCharacters();
						//logger.debug(chars);
						groupMemberSymbols.add(chars);
					}
				}
			}
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newSBMLSpeciesGroup);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(species, newSBMLSpeciesGroup);
			// uncomment to connect entities to compartments
			this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(species, newSBMLSpeciesGroup, compartmentLookupMap);
			this.sbmlSimpleModelUtilityServiceImpl.setSpeciesProperties(species, newSBMLSpeciesGroup);
			String speciesSbaseName = newSBMLSpeciesGroup.getsBaseName();
			for (String symbol : groupMemberSymbols) {
				// SBMLSpecies existingSpecies = this.sbmlSpeciesRepository.findBysBaseName(symbol); // This is why the sBaseName is so important. it is the only way to match the group members to database entities
				newSBMLSpeciesGroup.addSpeciesToGroup(sBaseNameToSBMLSpeciesMap.get(symbol));
				speciesSbaseName += "_";
				speciesSbaseName += symbol;
			}
			newSBMLSpeciesGroup.setsBaseName(speciesSbaseName);
			newSBMLSpeciesGroup.setsBaseId(species.getId());
			newSBMLSpeciesGroup.setsBaseMetaId("meta_" + speciesSbaseName);
			SBMLSpeciesGroup persistedNewSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup, SAVE_DEPTH);
			persistedNewSpeciesGroup.setCvTermList(species.getCVTerms());
			newSBMLSpeciesGroup = (SBMLSpeciesGroup) buildAndPersistExternalResourcesForSBaseEntity(persistedNewSpeciesGroup, activityNode);
			persistedNewSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup, SAVE_DEPTH);
			speciesList.add(persistedNewSpeciesGroup);
			sBaseNameToSBMLSpeciesMap.put(speciesSbaseName, persistedNewSpeciesGroup);
			this.provenanceGraphService.connect(persistedNewSpeciesGroup, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			
		}
		return sBaseNameToSBMLSpeciesMap;
	}

	

	private List<SBMLSimpleReaction> buildAndPersistSBMLSimpleReactions(ListOf<Reaction> listOfReactions,
			Map<String, SBMLCompartment> compartmentLookupMap,
			Map<String, SBMLSpecies> sBaseIdToSBMLSpeciesMap,
			ProvenanceGraphActivityNode activityNode) {
		
		List<SBMLSimpleReaction> sbmlSimpleReactionList = new ArrayList<>();
		for (Reaction reaction : listOfReactions) {
			
			// reaction not yet in db, build and persist it
			SBMLSimpleReaction newReaction = new SBMLSimpleReaction();
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newReaction);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(reaction, newReaction);
			// uncomment to connect entities to compartments
			this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(reaction, newReaction, compartmentLookupMap);
			this.sbmlSimpleModelUtilityServiceImpl.setSimpleReactionProperties(reaction, newReaction);
			// reactants
			for (int i=0; i != reaction.getReactantCount(); i++) {
				SBMLSpecies referencedSpecies = sBaseIdToSBMLSpeciesMap.get(reaction.getReactant(i).getSpecies());
				if(referencedSpecies == null) {
					logger.error("Reactant " + reaction.getReactant(i).getSpecies() + " of Reaction " + reaction.getId() + " missing in database!");
				} else {
					newReaction.addReactant(referencedSpecies);
				}
			}
			// products
			for (int i=0; i != reaction.getProductCount(); i++) {
				SBMLSpecies referencedSpecies = sBaseIdToSBMLSpeciesMap.get(reaction.getProduct(i).getSpecies());
				if(referencedSpecies == null) {
					logger.error("Product " + reaction.getProduct(i).getSpecies() + " of Reaction " + reaction.getId() + " missing in database!");
				} else {
					newReaction.addProduct(referencedSpecies);
				}
			}
			// catalysts
			for (int i=0; i != reaction.getModifierCount(); i++) {
				SBMLSpecies referencedSpecies = sBaseIdToSBMLSpeciesMap.get(reaction.getModifier(i).getSpecies());
				if(referencedSpecies == null) {
					logger.error("Modifier " + reaction.getModifier(i).getSpecies() + " of Reaction " + reaction.getId() + " missing in database!");
				} else if (reaction.getModifier(i).getSBOTermID().equals("SBO:0000460")){
					newReaction.addCatalysts(referencedSpecies);
				} else {
					logger.warn("Skipping modifier " + reaction.getModifier(i).getSpecies() + " of Reaction " + reaction.getId() + " with sboTerm " + reaction.getModifier(i).getSBOTermID());
				}
			}
			
			// annotations
			SBMLSimpleReaction persistedSimpleReaction = this.sbmlSimpleReactionRepository.save(newReaction, SAVE_DEPTH);
			persistedSimpleReaction.setCvTermList(reaction.getCVTerms());
			newReaction = (SBMLSimpleReaction) buildAndPersistExternalResourcesForSBaseEntity(persistedSimpleReaction, activityNode);
			sbmlSimpleReactionList.add(this.sbmlSimpleReactionRepository.save(newReaction, SAVE_DEPTH));
		}
		
		
		return sbmlSimpleReactionList;
	}
	
	private HelperQualSpeciesReturn buildAndPersistSBMLQualSpecies(
			ListOf<QualitativeSpecies> qualSpeciesListOf, 
			Map<String, SBMLSpecies> persistedSBMLSpeciesMap, 
			Map<String, SBMLCompartment> compartmentLookupMap,
			ProvenanceGraphActivityNode activityNode) {
		HelperQualSpeciesReturn helperQualSpeciesReturn = new HelperQualSpeciesReturn();
		//Map<String, SBMLQualSpecies> sBaseIdToQualSpeciesMap = new HashMap<>();
		//Map<String, SBMLQualSpecies> sBaseNameToQualSpeciesMap = new HashMap<>();
		List<QualitativeSpecies> groupQualSpeciesList = new ArrayList<>();
		for (QualitativeSpecies qualSpecies : qualSpeciesListOf) {
			if(qualSpecies.getName().equals("Group")) {
				//logger.debug("found qual Species group");
				groupQualSpeciesList.add(qualSpecies);// need to do them after all species have been persisted
			} else {
				SBMLQualSpecies newQualSpecies = new SBMLQualSpecies();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newQualSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(qualSpecies, newQualSpecies);
				// uncomment to connect entities to compartments
				this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(qualSpecies, newQualSpecies, compartmentLookupMap);
				this.sbmlSimpleModelUtilityServiceImpl.setQualSpeciesProperties(qualSpecies, newQualSpecies);
				newQualSpecies.setCorrespondingSpecies(persistedSBMLSpeciesMap.get(qualSpecies.getName()));
				SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newQualSpecies, SAVE_DEPTH);
				
				helperQualSpeciesReturn.addQualSpecies(persistedNewQualSpecies);
				//sBaseIdToQualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
				//sBaseNameToQualSpeciesMap.put(persistedNewQualSpecies.getsBaseName(), persistedNewQualSpecies);
				
				this.provenanceGraphService.connect(persistedNewQualSpecies, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			}
		}
		// now build the group nodes
		for (QualitativeSpecies qualSpecies : groupQualSpeciesList) {
			SBMLQualSpeciesGroup newSBMLQualSpeciesGroup = new SBMLQualSpeciesGroup();
			List<String> groupMemberSymbols = new ArrayList<>();
			XMLNode ulNode = qualSpecies.getNotes().getChildElement("body", "http://www.w3.org/1999/xhtml").getChildElement("p", null).getChildElement("ul", null);
			for (int i = 0; i != ulNode.getChildCount(); i++) {
				if(ulNode.getChild(i).getChildCount() > 0) {
					for (int j = 0; j != ulNode.getChild(i).getChildCount(); j++) {
						String chars = ulNode.getChild(i).getChild(j).getCharacters();
						//logger.debug(chars);
						groupMemberSymbols.add(chars);
					}
				}
			}
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newSBMLQualSpeciesGroup);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(qualSpecies, newSBMLQualSpeciesGroup);
			// uncomment to connect entities to compartments
			this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(qualSpecies, newSBMLQualSpeciesGroup, compartmentLookupMap);
			this.sbmlSimpleModelUtilityServiceImpl.setQualSpeciesProperties(qualSpecies, newSBMLQualSpeciesGroup);
			String qualSpeciesSbaseName = newSBMLQualSpeciesGroup.getsBaseName();
			for (String symbol : groupMemberSymbols) {
				// here I need a symbol/sbaseName to QualSpecies map, but qualSpeciesMap maps the sbaseId of the qualSpecies to the QualSpecies.
				newSBMLQualSpeciesGroup.addQualSpeciesToGroup(helperQualSpeciesReturn.getSBaseNameToQualSpeciesMap().get(symbol)); // TODO: THIS LEADS TO 
				//org.springframework.dao.InvalidDataAccessApiUsageException: The relationship 'HAS_GROUP_MEMBER' from 'org.tts.model.common.SBMLQualSpeciesGroup' to 'org.tts.model.common.SBMLQualSpecies' stored on '#qualSpeciesInGroup' contains 'null', which is an invalid target for this relationship.'; nested exception is org.neo4j.ogm.exception.core.InvalidRelationshipTargetException: The relationship 'HAS_GROUP_MEMBER' from 'org.tts.model.common.SBMLQualSpeciesGroup' to 'org.tts.model.common.SBMLQualSpecies' stored on '#qualSpeciesInGroup' contains 'null', which is an invalid target for this relationship.'

				qualSpeciesSbaseName += "_";
				qualSpeciesSbaseName += symbol;
			}
			
			newSBMLQualSpeciesGroup.setsBaseName(qualSpeciesSbaseName);
			newSBMLQualSpeciesGroup.setsBaseId(qualSpecies.getId());
			newSBMLQualSpeciesGroup.setsBaseMetaId("meta_" + qualSpeciesSbaseName);
			
			newSBMLQualSpeciesGroup.setCorrespondingSpecies(persistedSBMLSpeciesMap.get(qualSpeciesSbaseName));
			
			SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup, SAVE_DEPTH);
			helperQualSpeciesReturn.addQualSpecies(persistedNewQualSpecies);
			
			//sBaseIdToQualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
			//helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), persistedNewQualSpecies);
			this.provenanceGraphService.connect(persistedNewQualSpecies, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			
		}
		//helperQualSpeciesReturn.setSBaseNameToQualSpeciesMap(sBaseIdToQualSpeciesMap);
		//if(helperQualSpeciesReturn.getSBaseIdToQualSpeciesMap() == null) {
		//	Map<String, SBMLQualSpecies> emptySBaseIdMap = new HashMap<>();
		//	helperQualSpeciesReturn.setSBaseIdToQualSpeciesMap(emptySBaseIdMap);
		//}
		return helperQualSpeciesReturn;
	}
	
	private List<SBMLSimpleTransition> buildAndPersistTransitions(
			Map<String, SBMLQualSpecies> qualSBaseLookupMap,
			ListOf<Transition> transitionListOf,
			ProvenanceGraphActivityNode activityNode) {
		List<SBMLSimpleTransition> transitionList = new ArrayList<>();
		for (Transition transition : transitionListOf) {
			SBMLQualSpecies tmp = null;
			String newTransitionId = "";
			if(transition.getListOfInputs().size() > 1) {
				logger.warn("More than one Input in transition: " + transition.getName());
			}
			if(qualSBaseLookupMap.containsKey(transition.getListOfInputs().get(0).getQualitativeSpecies())) {
				tmp = qualSBaseLookupMap.get(transition.getListOfInputs().get(0).getQualitativeSpecies());
			} else {
				logger.warn("Could not find QualSpecies in this model for : " + transition.getListOfInputs().get(0).getQualitativeSpecies());
				//tmp = this.sbmlQualSpeciesRepository.findBysBaseId(transition.getListOfInputs().get(0).getQualitativeSpecies());
				tmp = null;
			}
			if(tmp == null) {
				logger.debug("Tmp is null!!");
			} else {
				newTransitionId += tmp.getsBaseName();
				newTransitionId += "-";
			}
			newTransitionId += this.utilityService.translateSBOString(transition.getSBOTermID());
			newTransitionId += "->"; // TODO: Should this be directional?
			if(transition.getListOfOutputs().size() > 1) {
				logger.warn("More than one Output in transition: " + transition.getName());
			}
			if(qualSBaseLookupMap.containsKey(transition.getListOfOutputs().get(0).getQualitativeSpecies())) {
				tmp = qualSBaseLookupMap.get(transition.getListOfOutputs().get(0).getQualitativeSpecies());
			} else {
				logger.warn("Could not find QualSpecies in this model for : " + transition.getListOfOutputs().get(0).getQualitativeSpecies());
				//tmp = this.sbmlQualSpeciesRepository.findBysBaseId(transition.getListOfOutputs().get(0).getQualitativeSpecies());
				tmp = null;
			}
			if (tmp != null) {
				newTransitionId += (tmp).getsBaseName();
			} else {
				logger.warn("Species is null");
			}
			
			SBMLSimpleTransition newSimpleTransition = new SBMLSimpleTransition();
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newSimpleTransition);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(transition, newSimpleTransition);
			newSimpleTransition.setTransitionId(newTransitionId);
			newSimpleTransition.setInputSpecies(qualSBaseLookupMap.get(transition.getListOfInputs().get(0).getQualitativeSpecies())); // not sure if this works
			newSimpleTransition.setOutputSpecies(qualSBaseLookupMap.get(transition.getListOfOutputs().get(0).getQualitativeSpecies())); // not sure if this works
			SBMLSimpleTransition persistedNewSimpleTransition = this.sbmlSimpleTransitionRepository.save(newSimpleTransition, SAVE_DEPTH);
			transitionList.add(persistedNewSimpleTransition);
			this.provenanceGraphService.connect(persistedNewSimpleTransition, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			
		}
		return transitionList;
	}
	
	/**
	 * @param externalResources
	 * @param externalResourceEntityLookupMap
	 * @param biologicalModifierRelationships
	 * @param sBaseEntity
	 */
	private SBMLSBaseEntity buildAndPersistExternalResourcesForSBaseEntity(SBMLSBaseEntity sBaseEntity, ProvenanceGraphActivityNode activityNode) {
		
		boolean addMdAnderson = sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().isAddMdAndersonAnnotation();
		boolean foundMdAndersonGene = false;
		List<String> mdAndersonNamesList = new ArrayList<>();
		SBMLSBaseEntity updatedSBaseEntity = sBaseEntity;
		List<String> mdAndersonGeneList = sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().getGenelist();
		if (addMdAnderson && mdAndersonGeneList != null && mdAndersonGeneList.contains(sBaseEntity.getsBaseName())) {
			logger.debug("Found MdAnderson Gene in sBaseName: " + sBaseEntity.getsBaseName());
			foundMdAndersonGene = true;
			mdAndersonNamesList.add(sBaseEntity.getsBaseName());
		}
		for (CVTerm cvTerm : sBaseEntity.getCvTermList()) {
			//createExternalResources(cvTerm, qualSpecies);
			for (String resource : cvTerm.getResources()) {
				Map<String,NameNode> currentNames = new HashMap<>();
				// build a BiomodelsQualifier RelationshopEntity
				BiomodelsQualifier newBiomodelsQualifier = createBiomodelsQualifier(updatedSBaseEntity,
						cvTerm.getQualifierType(), cvTerm.getQualifier());
				
				// build the ExternalResource or link to existing one
				ExternalResourceEntity existingExternalResourceEntity = this.externalResourceEntityRepository.findByUri(resource);
				if(existingExternalResourceEntity != null) {
					newBiomodelsQualifier.setEndNode(existingExternalResourceEntity);
					this.provenanceGraphService.connect(activityNode, existingExternalResourceEntity, ProvenanceGraphEdgeType.used);
				} else {
					ExternalResourceEntity newExternalResourceEntity = new ExternalResourceEntity();
					this.graphBaseEntityService.setGraphBaseEntityProperties(newExternalResourceEntity);
					//newExternalResourceEntity.setEntityUUID(UUID.randomUUID().toString());
					newExternalResourceEntity.setUri(resource);
					if (resource.contains("kegg.genes")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGGENES);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
						List<String> nameList = httpService.getGeneNamesFromKeggURL(resource);
						for (String name : nameList) {
							createNameNode(currentNames, newExternalResourceEntity, name);
							// MDAnderson
							if (addMdAnderson && !foundMdAndersonGene && mdAndersonGeneList.contains(name)) {
								foundMdAndersonGene = true;
								mdAndersonNamesList.add(name);
							}
						}
					} else if(resource.contains("kegg.reaction")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGREACTION);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
					} else if(resource.contains("kegg.drug")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGDRUG);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
						//newExternalResourceEntity.setName(sBaseEntity.getsBaseName());
						createNameNode(currentNames, newExternalResourceEntity, sBaseEntity.getsBaseName());
					} else if(resource.contains("kegg.compound")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGCOMPOUND);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
						setKeggCompoundNames(resource, newExternalResourceEntity);
					} 
					//newBiomodelsQualifier.setEndNode(newExternalResourceEntity);
					ExternalResourceEntity persistedNewExternalResourceEntity = this.externalResourceEntityRepository.save(newExternalResourceEntity, 1); // TODO can I change this depth to one to persist the nameNodes or does that break BiomodelQualifier connections?
					newBiomodelsQualifier.setEndNode(persistedNewExternalResourceEntity);
					this.provenanceGraphService.connect(persistedNewExternalResourceEntity, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
				BiomodelsQualifier persistedNewBiomodelsQualifier = biomodelsQualifierRepository.save(newBiomodelsQualifier, 0);
				updatedSBaseEntity.addBiomodelsQualifier(persistedNewBiomodelsQualifier);
				updatedSBaseEntity = this.sbmlSBaseEntityRepository.save(updatedSBaseEntity, 0);
				//updatedSBaseEntity.addBiomodelsQualifier(newBiomodelsQualifier);
			}
		}
		if (addMdAnderson && foundMdAndersonGene) {
			for (String mdAndersonGeneName : mdAndersonNamesList) {
				BiomodelsQualifier mdAndersonBiomodelsQualifier = this.createBiomodelsQualifier(updatedSBaseEntity, Type.BIOLOGICAL_QUALIFIER, Qualifier.BQB_IS_DESCRIBED_BY);
				StringBuilder mdAndersonUriStringBuilder = new StringBuilder();
				mdAndersonUriStringBuilder.append(sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().getBaseurl());
				mdAndersonUriStringBuilder.append(mdAndersonGeneName);
				mdAndersonUriStringBuilder.append("?section=");
				mdAndersonUriStringBuilder.append(sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().getSection());
				
				ExternalResourceEntity existingMdAndersonResourceEntity = this.externalResourceEntityRepository.findByUri(mdAndersonUriStringBuilder.toString());
				if (existingMdAndersonResourceEntity != null) {
					mdAndersonBiomodelsQualifier.setEndNode(existingMdAndersonResourceEntity);
					this.provenanceGraphService.connect(activityNode, existingMdAndersonResourceEntity, ProvenanceGraphEdgeType.used);
				} else {
					ExternalResourceEntity newMdAndersonResourceEntity = new ExternalResourceEntity();
					this.graphBaseEntityService.setGraphBaseEntityProperties(newMdAndersonResourceEntity);
					newMdAndersonResourceEntity.setUri(mdAndersonUriStringBuilder.toString());
					newMdAndersonResourceEntity.setType(ExternalResourceType.MDANDERSON);
					newMdAndersonResourceEntity.setDatabaseFromUri("MDAnderson");
					ExternalResourceEntity persistedNewMdAndersonResourceEntity = this.externalResourceEntityRepository.save(newMdAndersonResourceEntity, 0);
					mdAndersonBiomodelsQualifier.setEndNode(persistedNewMdAndersonResourceEntity);
					this.provenanceGraphService.connect(persistedNewMdAndersonResourceEntity, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
				BiomodelsQualifier persistedMDAndersonBiomodelsQualifier = biomodelsQualifierRepository.save(mdAndersonBiomodelsQualifier, 0);
				updatedSBaseEntity.addBiomodelsQualifier(persistedMDAndersonBiomodelsQualifier);
				updatedSBaseEntity = this.sbmlSBaseEntityRepository.save(updatedSBaseEntity, 0);
			}			
		}
		
		return updatedSBaseEntity;
	}

	private void createNameNode(Map<String, NameNode> seenNames, ExternalResourceEntity externalResourceEntity,
			String name) {
		// check if we have seen this name already for this cvTerm (i.e. there is a duplicate)
		NameNode nameNode = seenNames.getOrDefault(name, null);
		// if not, check if we already have a name node with that name in the database
		if (nameNode == null) {
			nameNode = this.nameNodeService.findByName(name);
		}
		// if not, build a new name node
		if (nameNode == null) {
			nameNode = new NameNode();
			this.graphBaseEntityService.setGraphBaseEntityProperties(nameNode);
			nameNode.setName(name);
		}
		nameNode.addResource(externalResourceEntity);
		externalResourceEntity.addName(nameNode);
	}

	private BiomodelsQualifier createBiomodelsQualifier(SBMLSBaseEntity startNodeSBaseEntity, Type qualifierType,
			Qualifier qualifier) {
		BiomodelsQualifier newBiomodelsQualifier = new BiomodelsQualifier();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newBiomodelsQualifier);
		//newBiomodelsQualifier.setEntityUUID(UUID.randomUUID().toString());
		newBiomodelsQualifier.setType(qualifierType);
		newBiomodelsQualifier.setQualifier(qualifier);
		newBiomodelsQualifier.setStartNode(startNodeSBaseEntity);
		return newBiomodelsQualifier;
	}	
	
	private void setKeggCompoundNames(String resource, ExternalResourceEntity newExternalResourceEntity) {
		// TODO Here httpService needs to fetch the resource from KEGG Compound and set the name and secondary Name
		// what about formula? Is this more interesting? -> Do this as annotation
		this.httpService.setCompoundAnnotationFromResource(resource, newExternalResourceEntity);
	}
	
	private boolean setKeggGeneNames(String resource, ExternalResourceEntity entity) {
		
		List<String> keggGeneNames = httpService.getGeneNamesFromKeggURL(resource);
		if (keggGeneNames != null) {
			if (!keggGeneNames.isEmpty()) {
				entity.setName(keggGeneNames.remove(0)); // remove returns the element it removes, so it becomes the name and keggGeneNames holds all secondary names
			}
			if (!keggGeneNames.isEmpty()) {
				String[] secondaryNames = new String[keggGeneNames.size()];
				for (int i = 0; i != keggGeneNames.size(); i++) {
					secondaryNames[i] = keggGeneNames.get(i);
				}
				entity.setSecondaryNames(secondaryNames);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isValidSBML(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSBMLVersionCompatible(File file) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Model extractSBMLModel(MultipartFile file) throws XMLStreamException, IOException {
		SBMLDocument doc = SBMLReader.read(file.getInputStream()); 
		return doc.getModel();
	}

	@Override
	public List<ProvenanceEntity> buildAndPersist(Model model, FileNode sbmlfile, ProvenanceGraphActivityNode activityNode) { 
		
		Map<String, SBMLSpecies> persistedSBMLSpeciesMap = null;
		// compartment
		List<ProvenanceEntity> returnList= new ArrayList<>();
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model, activityNode);
		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLCompartment compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getsBaseId(), compartment); // sBaseId here will be the matchingAttribute, might even want to have a map <Entity, matchingAttribute> to be able to match different entities with different attributes
			returnList.add(compartment);
		}
		
		// species
		Map<String, SBMLSpecies> sBaseIdToSBMLSpeciesMap = new HashMap<>();
		if (model.getListOfSpecies() != null && model.getListOfSpecies().size() > 0) {
			persistedSBMLSpeciesMap = buildAndPersistSBMLSpecies(model.getListOfSpecies(), compartmentLookupMap, activityNode);
			persistedSBMLSpeciesMap.forEach((name, species)->{
				returnList.add(species);
				sBaseIdToSBMLSpeciesMap.put(species.getsBaseId(), species);
			});
		}
		// reactions
		if(model.getListOfReactions() != null && model.getListOfReactions().size() > 0) {
			List<SBMLSimpleReaction> persistedSBMLSimpleReactions = buildAndPersistSBMLSimpleReactions(model.getListOfReactions(), compartmentLookupMap, sBaseIdToSBMLSpeciesMap, activityNode);
			persistedSBMLSimpleReactions.forEach(reaction->{
				returnList.add(reaction);
			});
		}
		// Qual Model Plugin:
		if(model.getExtension("qual") != null ) {
			QualModelPlugin qualModelPlugin = (QualModelPlugin) model.getExtension("qual");

			// qualitative Species
			if(qualModelPlugin.getListOfQualitativeSpecies() != null && qualModelPlugin.getListOfQualitativeSpecies().size() > 0) {
				HelperQualSpeciesReturn qualSpeciesHelper = buildAndPersistSBMLQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), persistedSBMLSpeciesMap, compartmentLookupMap, activityNode);
				
				Map<String, SBMLQualSpecies> persistedQualSpeciesList = qualSpeciesHelper.getSBaseNameToQualSpeciesMap();
				persistedQualSpeciesList.forEach((k, qualSpecies)-> {
					returnList.add(qualSpecies);
				});
				Map<String, SBMLQualSpecies> qualSBaseLookupMap = qualSpeciesHelper.getSBaseIdToQualSpeciesMap();
				
				// transitions (qual model plugin)
				if(qualModelPlugin.getListOfTransitions() != null && qualModelPlugin.getListOfTransitions().size() > 0) {
					List<SBMLSimpleTransition> persistedTransitionList	= buildAndPersistTransitions(qualSBaseLookupMap, qualModelPlugin.getListOfTransitions(), activityNode);
					persistedTransitionList.forEach(transition-> {
						returnList.add(transition);
					});
				}
			}
		}
		
		return returnList; // This is the list<sources> of the Knowledge Graph "WarehouseGraphNode" Node, which then serves as Source for the network mappings, derived from full KEGG
	}
}
