package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
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
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.HelperQualSpeciesReturn;
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

@Service
public class SBMLSimpleModelServiceImpl implements SBMLService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
			UtilityService utilityService) {
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
	}

	private List<SBMLCompartment> getCompartmentList(Model model, ProvenanceGraphActivityNode persistActivity) {
		List<SBMLCompartment> sbmlCompartmentList = new ArrayList<>();
		for(Compartment compartment : model.getListOfCompartments()) {
			//SBMLCompartment sbmlCompartment = new SBMLCompartment();
			// check if a compartment with that name (and those settings does already exist)
			//spatialDimensions, size, constant, sBaseName, sBaseId
			try {
				SBMLCompartment existingCompartment = (SBMLCompartment) this.sbmlSBaseEntityRepository.findBySBaseId(compartment.getId(), 2);
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

	private List<SBMLSpecies> buildAndPersistSBMLSpecies(
			ListOf<Species> speciesListOf,
			Map<String, SBMLCompartment> compartmentLookupMap, 
			ProvenanceGraphActivityNode activityNode) {
		List<SBMLSpecies> speciesList = new ArrayList<>();
		List<Species> groupSpeciesList = new ArrayList<>();
		for (Species species : speciesListOf) {
			if(species.getName().equals("Group")) {
				logger.debug("found group");
				groupSpeciesList.add(species);
			} else {
				SBMLSpecies existingSpecies = this.sbmlSpeciesRepository.findBySBaseName(species.getName());
				if(existingSpecies != null) {
					speciesList.add(existingSpecies);
					this.provenanceGraphService.connect(activityNode, existingSpecies, ProvenanceGraphEdgeType.used);
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
					this.provenanceGraphService.connect(persistedSpeciesWithExternalResources, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
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
						logger.debug(chars);
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
				SBMLSpecies existingSpecies = this.sbmlSpeciesRepository.findBySBaseName(symbol);
				newSBMLSpeciesGroup.addSpeciesToGroup(existingSpecies);
				speciesSbaseName += "_";
				speciesSbaseName += symbol;
			}
			SBMLSpeciesGroup existingSBMLSpeciesGroup = (SBMLSpeciesGroup) sbmlSpeciesRepository.findBySBaseName(speciesSbaseName);
			if (existingSBMLSpeciesGroup != null) {
				speciesList.add(existingSBMLSpeciesGroup);
				this.provenanceGraphService.connect(activityNode, existingSBMLSpeciesGroup, ProvenanceGraphEdgeType.used);
			} else {
				newSBMLSpeciesGroup.setsBaseName(speciesSbaseName);
				newSBMLSpeciesGroup.setsBaseId(speciesSbaseName);
				newSBMLSpeciesGroup.setsBaseMetaId("meta_" + speciesSbaseName);
				SBMLSpeciesGroup persistedNewSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup, SAVE_DEPTH);
				persistedNewSpeciesGroup.setCvTermList(species.getCVTerms());
				newSBMLSpeciesGroup = (SBMLSpeciesGroup) buildAndPersistExternalResourcesForSBaseEntity(persistedNewSpeciesGroup, activityNode);
				persistedNewSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup, SAVE_DEPTH);
				speciesList.add(persistedNewSpeciesGroup);
				this.provenanceGraphService.connect(persistedNewSpeciesGroup, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			}
		}
		return speciesList;
	}

	

	private List<SBMLSimpleReaction> buildAndPersistSBMLSimpleReactions(ListOf<Reaction> listOfReactions,
			Map<String, SBMLCompartment> compartmentLookupMap,
			ProvenanceGraphActivityNode activityNode) {
		
		List<SBMLSimpleReaction> sbmlSimpleReactionList = new ArrayList<>();
		for (Reaction reaction : listOfReactions) {
			
			SBMLSimpleReaction existingSimpleReaction = this.sbmlSimpleReactionRepository.findBySBaseName(reaction.getName(), 0);
			if(existingSimpleReaction != null) {
				// found existing Reaction
				// TODO: Check if it is the same reaction with reactants and products
				sbmlSimpleReactionList.add(existingSimpleReaction);
			} else {
				// reaction not yet in db, build and persist it
				SBMLSimpleReaction newReaction = new SBMLSimpleReaction();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newReaction);
				this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(reaction, newReaction);
				// uncomment to connect entities to compartments
				this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(reaction, newReaction, compartmentLookupMap);
				this.sbmlSimpleModelUtilityServiceImpl.setSimpleReactionProperties(reaction, newReaction);
				// reactants
				for (int i=0; i != reaction.getReactantCount(); i++) {
					SBMLSpecies referencedSpecies = this.sbmlSpeciesRepository.findBySBaseId(reaction.getReactant(i).getSpecies());
					if(referencedSpecies == null) {
						logger.error("Reactant " + reaction.getReactant(i).getSpecies() + " of Reaction " + reaction.getId() + " missing in database!");
					} else {
						newReaction.addReactant(referencedSpecies);
					}
				}
				// products
				for (int i=0; i != reaction.getProductCount(); i++) {
					SBMLSpecies referencedSpecies = this.sbmlSpeciesRepository.findBySBaseId(reaction.getProduct(i).getSpecies());
					if(referencedSpecies == null) {
						logger.error("Product " + reaction.getProduct(i).getSpecies() + " of Reaction " + reaction.getId() + " missing in database!");
					} else {
						newReaction.addProduct(referencedSpecies);
					}
				}
				// catalysts
				for (int i=0; i != reaction.getModifierCount(); i++) {
					SBMLSpecies referencedSpecies = this.sbmlSpeciesRepository.findBySBaseId(reaction.getModifier(i).getSpecies());
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
		}
		
		return sbmlSimpleReactionList;
	}
	
	private HelperQualSpeciesReturn buildAndPersistSBMLQualSpecies(
			ListOf<QualitativeSpecies> qualSpeciesListOf, 
			Map<String, SBMLCompartment> compartmentLookupMap,
			ProvenanceGraphActivityNode activityNode) {
		HelperQualSpeciesReturn helperQualSpeciesReturn = new HelperQualSpeciesReturn();
		Map<String, SBMLQualSpecies> qualSpeciesMap = new HashMap<>();
		List<QualitativeSpecies> groupQualSpeciesList = new ArrayList<>();
		for (QualitativeSpecies qualSpecies : qualSpeciesListOf) {
			if(qualSpecies.getName().equals("Group")) {
				logger.debug("found qual Species group");
				groupQualSpeciesList.add(qualSpecies);// need to do them after all species have been persisted
			} else {
				SBMLQualSpecies existingSpecies = this.sbmlQualSpeciesRepository.findBySBaseName(qualSpecies.getName());
				if (existingSpecies != null) {
					if(!qualSpeciesMap.containsKey(existingSpecies.getsBaseId())) {
						qualSpeciesMap.put(existingSpecies.getsBaseId(), existingSpecies);
						this.provenanceGraphService.connect(activityNode, existingSpecies, ProvenanceGraphEdgeType.used);
					}	
					if(!qualSpecies.getId().substring(5).equals(qualSpecies.getName())) {
						// possibly this is the case when we have an entity duplicated in sbml, but deduplicate it here
						// transitions might reference this duplicate entity and need to be pointed to the original one
						helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), existingSpecies.getsBaseId());
					}
					
				} else {
					SBMLQualSpecies newQualSpecies = new SBMLQualSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newQualSpecies);
					this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(qualSpecies, newQualSpecies);
					// uncomment to connect entities to compartments
					this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(qualSpecies, newQualSpecies, compartmentLookupMap);
					this.sbmlSimpleModelUtilityServiceImpl.setQualSpeciesProperties(qualSpecies, newQualSpecies);
					newQualSpecies.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpecies.getName()));
					SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newQualSpecies, SAVE_DEPTH);
					qualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
					this.provenanceGraphService.connect(persistedNewQualSpecies, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
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
						logger.debug(chars);
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
				SBMLQualSpecies existingQualSpecies = this.sbmlQualSpeciesRepository.findBySBaseName(symbol);
				newSBMLQualSpeciesGroup.addQualSpeciesToGroup(existingQualSpecies);
				qualSpeciesSbaseName += "_";
				qualSpeciesSbaseName += symbol;
			}
			SBMLQualSpeciesGroup existingSBMLQualSpeciesGroup = (SBMLQualSpeciesGroup) sbmlQualSpeciesRepository.findBySBaseName(qualSpeciesSbaseName);
			if (existingSBMLQualSpeciesGroup != null) {
				if (!qualSpeciesMap.containsKey(existingSBMLQualSpeciesGroup.getsBaseId())) {
					qualSpeciesMap.put(existingSBMLQualSpeciesGroup.getsBaseId(), existingSBMLQualSpeciesGroup);
					this.provenanceGraphService.connect(activityNode, existingSBMLQualSpeciesGroup, ProvenanceGraphEdgeType.used);
				}
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
			} else {
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseName(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseId(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseMetaId("meta_" + qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpeciesSbaseName));
				
				SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup, SAVE_DEPTH);
				qualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
				this.provenanceGraphService.connect(persistedNewQualSpecies, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			}
		}
		helperQualSpeciesReturn.setSpeciesMap(qualSpeciesMap);
		if(helperQualSpeciesReturn.getsBaseIdMap() == null) {
			Map<String, String> emptySBaseIdMap = new HashMap<>();
			helperQualSpeciesReturn.setsBaseIdMap(emptySBaseIdMap);
		}
		return helperQualSpeciesReturn;
	}
	
	private List<SBMLSimpleTransition> buildAndPersistTransitions(
			Map<String, String> qualSBaseLookupMap,
			ListOf<Transition> transitionListOf,
			ProvenanceGraphActivityNode activityNode) {
		List<SBMLSimpleTransition> transitionList = new ArrayList<>();
		for (Transition transition : transitionListOf) {
			SBMLQualSpecies tmp = null;
			String newTransitionId = "";
			if(transition.getListOfInputs().size() > 1) {
				logger.info("More than one Input in transition: " + transition.getName());
			}
			if(qualSBaseLookupMap.containsKey(transition.getListOfInputs().get(0).getQualitativeSpecies())) {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(qualSBaseLookupMap.get(transition.getListOfInputs().get(0).getQualitativeSpecies()));
			} else {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(transition.getListOfInputs().get(0).getQualitativeSpecies());
			}
			if(tmp == null) {
				logger.debug("Tmp is null!!");
			}
			newTransitionId += tmp.getsBaseName();
			newTransitionId += "-";
			newTransitionId += this.utilityService.translateSBOString(transition.getSBOTermID());
			newTransitionId += "->"; // TODO: Should this be directional?
			if(transition.getListOfOutputs().size() > 1) {
				logger.info("More than one Output in transition: " + transition.getName());
			}
			if(qualSBaseLookupMap.containsKey(transition.getListOfOutputs().get(0).getQualitativeSpecies())) {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(qualSBaseLookupMap.get(transition.getListOfOutputs().get(0).getQualitativeSpecies()));
			} else {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(transition.getListOfOutputs().get(0).getQualitativeSpecies());
			}
			if (tmp != null) {
				newTransitionId += (tmp).getsBaseName();
			} else {
				logger.debug("Species is null");
			}
			
			SBMLSimpleTransition existingSimpleTransition = this.sbmlSimpleTransitionRepository.getByTransitionId(newTransitionId, 2);
			String inputName = null;
			String outputName = null;
			if(qualSBaseLookupMap.containsKey(transition.getListOfInputs().get(0).getQualitativeSpecies())) {
				inputName = qualSBaseLookupMap.get(transition.getListOfInputs().get(0).getQualitativeSpecies());
			} else {
				inputName = transition.getListOfInputs().get(0).getQualitativeSpecies();
			}
			if(qualSBaseLookupMap.containsKey(transition.getListOfOutputs().get(0).getQualitativeSpecies())) {
				outputName = qualSBaseLookupMap.get(transition.getListOfOutputs().get(0).getQualitativeSpecies());
			} else {
				outputName = transition.getListOfOutputs().get(0).getQualitativeSpecies();
			}
			if(existingSimpleTransition != null ) {
				if(existingSimpleTransition.getInputSpecies() == null || existingSimpleTransition.getOutputSpecies() == null) {
					logger.debug("How?");
				}
				if(existingSimpleTransition.getInputSpecies().getsBaseName().equals(inputName) &&
						existingSimpleTransition.getOutputSpecies().getsBaseName().equals(outputName)) {
					transitionList.add(existingSimpleTransition);
					this.provenanceGraphService.connect(activityNode, existingSimpleTransition, ProvenanceGraphEdgeType.used);
				}
			} else {
				SBMLSimpleTransition newSimpleTransition = new SBMLSimpleTransition();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newSimpleTransition);
				this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(transition, newSimpleTransition);
				newSimpleTransition.setTransitionId(newTransitionId);
				newSimpleTransition.setInputSpecies(this.sbmlQualSpeciesRepository.findBySBaseId(inputName));
				newSimpleTransition.setOutputSpecies(this.sbmlQualSpeciesRepository.findBySBaseId(outputName));
				SBMLSimpleTransition persistedNewSimpleTransition = this.sbmlSimpleTransitionRepository.save(newSimpleTransition, SAVE_DEPTH);
				transitionList.add(persistedNewSimpleTransition);
				this.provenanceGraphService.connect(persistedNewSimpleTransition, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			}
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
		SBMLSBaseEntity updatedSBaseEntity = sBaseEntity;
		for (CVTerm cvTerm : sBaseEntity.getCvTermList()) {
			//createExternalResources(cvTerm, qualSpecies);
			for (String resource : cvTerm.getResources()) {
				// build a BiomodelsQualifier RelationshopEntity
				BiomodelsQualifier newBiomodelsQualifier = new BiomodelsQualifier();
				newBiomodelsQualifier.setEntityUUID(UUID.randomUUID().toString());
				newBiomodelsQualifier.setType(cvTerm.getQualifierType());
				newBiomodelsQualifier.setQualifier(cvTerm.getQualifier());
				newBiomodelsQualifier.setStartNode(updatedSBaseEntity);
				
				// build the ExternalResource or link to existing one
				ExternalResourceEntity existingExternalResourceEntity = this.externalResourceEntityRepository.findByUri(resource);
				if(existingExternalResourceEntity != null) {
					newBiomodelsQualifier.setEndNode(existingExternalResourceEntity);
					this.provenanceGraphService.connect(activityNode, existingExternalResourceEntity, ProvenanceGraphEdgeType.used);
				} else {
					ExternalResourceEntity newExternalResourceEntity = new ExternalResourceEntity();
					newExternalResourceEntity.setEntityUUID(UUID.randomUUID().toString());
					newExternalResourceEntity.setUri(resource);
					if (resource.contains("kegg.genes")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGGENES);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
						setKeggGeneNames(resource, newExternalResourceEntity);
					} else if(resource.contains("kegg.reaction")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGREACTION);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
					} else if(resource.contains("kegg.drug")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGDRUG);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
						newExternalResourceEntity.setName(sBaseEntity.getsBaseName());
					} else if(resource.contains("kegg.compound")) {
						newExternalResourceEntity.setType(ExternalResourceType.KEGGCOMPOUND);
						newExternalResourceEntity.setDatabaseFromUri("KEGG");
						setKeggCompoundNames(resource, newExternalResourceEntity);
					} 
					//newBiomodelsQualifier.setEndNode(newExternalResourceEntity);
					ExternalResourceEntity persistedNewExternalResourceEntity = this.externalResourceEntityRepository.save(newExternalResourceEntity, 0);
					newBiomodelsQualifier.setEndNode(persistedNewExternalResourceEntity);
					this.provenanceGraphService.connect(persistedNewExternalResourceEntity, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
				BiomodelsQualifier persistedNewBiomodelsQualifier = biomodelsQualifierRepository.save(newBiomodelsQualifier, 0);
				updatedSBaseEntity.addBiomodelsQualifier(persistedNewBiomodelsQualifier);
				updatedSBaseEntity = this.sbmlSBaseEntityRepository.save(updatedSBaseEntity, 0);
				//updatedSBaseEntity.addBiomodelsQualifier(newBiomodelsQualifier);
			}
		}
		return updatedSBaseEntity;
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
	public Map<String, Iterable<SBMLSBaseEntity>> extractSBMLEntities(Model model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Iterable<Object>> extractAndConnectExternalResources(
			Map<String, Iterable<SBMLSBaseEntity>> allEntities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ProvenanceEntity> buildAndPersist(Model model, FileNode sbmlfile, ProvenanceGraphActivityNode activityNode) { 
		
		
		// compartment
		List<ProvenanceEntity> returnList= new ArrayList<>();
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model, activityNode);
		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLCompartment compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getsBaseId(), compartment); // sBaseId here will be the matchingAttribute, might even want to have a map <Entity, matchingAttribute> to be able to match different entities with different attributes
			returnList.add(compartment);
		}
		
		// species
		if (model.getListOfSpecies() != null && model.getListOfSpecies().size() > 0) {
			List<SBMLSpecies> persistedSBMLSpecies = buildAndPersistSBMLSpecies(model.getListOfSpecies(), compartmentLookupMap, activityNode);
			persistedSBMLSpecies.forEach(species->{
				returnList.add(species);
			});
		}
		// reactions
		if(model.getListOfReactions() != null && model.getListOfReactions().size() > 0) {
			List<SBMLSimpleReaction> persistedSBMLSimpleReactions = buildAndPersistSBMLSimpleReactions(model.getListOfReactions(), compartmentLookupMap, activityNode);
			persistedSBMLSimpleReactions.forEach(reaction->{
				returnList.add(reaction);
			});
		}
		// Qual Model Plugin:
		if(model.getExtension("qual") != null ) {
			QualModelPlugin qualModelPlugin = (QualModelPlugin) model.getExtension("qual");

			// qualitative Species
			if(qualModelPlugin.getListOfQualitativeSpecies() != null && qualModelPlugin.getListOfQualitativeSpecies().size() > 0) {
				HelperQualSpeciesReturn qualSpeciesHelper = buildAndPersistSBMLQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap, activityNode);
				
				Map<String, SBMLQualSpecies> persistedQualSpeciesList = qualSpeciesHelper.getSpeciesMap();
				persistedQualSpeciesList.forEach((k, qualSpecies)-> {
					returnList.add(qualSpecies);
				});
				Map<String, String> qualSBaseLookupMap = qualSpeciesHelper.getsBaseIdMap();
				
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

	



	@Override
	public boolean clearDatabase() {
		this.graphBaseEntityRepository.deleteAll();
		if (((List<GraphBaseEntity>) this.graphBaseEntityRepository.findAll()).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<GraphBaseEntity> getAllEntities() {
		return (List<GraphBaseEntity>) this.graphBaseEntityRepository.findAll();
	}

}
