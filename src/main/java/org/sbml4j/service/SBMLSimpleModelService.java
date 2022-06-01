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
package org.sbml4j.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.neo4j.ogm.session.Session;
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
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml4j.Exception.ModelPersistenceException;
import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.base.GraphEnum.ExternalResourceType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.base.GraphEnum.WarehouseGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.sbml.SBMLCompartment;
import org.sbml4j.model.sbml.SBMLSBaseEntity;
import org.sbml4j.model.sbml.SBMLSpecies;
import org.sbml4j.model.sbml.SBMLSpeciesGroup;
import org.sbml4j.model.sbml.ext.qual.SBMLQualSpecies;
import org.sbml4j.model.sbml.ext.qual.SBMLQualSpeciesGroup;
import org.sbml4j.model.sbml.ext.sbml4j.BiomodelsQualifier;
import org.sbml4j.model.sbml.ext.sbml4j.ExternalResourceEntity;
import org.sbml4j.model.sbml.ext.sbml4j.NameNode;
import org.sbml4j.model.sbml.simple.SBMLSimpleReaction;
import org.sbml4j.model.sbml.simple.ext.qual.SBMLSimpleTransition;
import org.sbml4j.model.warehouse.DatabaseNode;
import org.sbml4j.model.warehouse.FileNode;
import org.sbml4j.model.warehouse.PathwayNode;
import org.sbml4j.repository.sbml.ext.sbml4j.BiomodelsQualifierRepository;
import org.sbml4j.service.SimpleSBML.SBMLCompartmentService;
import org.sbml4j.service.SimpleSBML.SBMLQualSpeciesService;
import org.sbml4j.service.SimpleSBML.SBMLSBaseEntityService;
import org.sbml4j.service.SimpleSBML.SBMLSimpleReactionService;
import org.sbml4j.service.SimpleSBML.SBMLSimpleTransitionService;
import org.sbml4j.service.SimpleSBML.SBMLSpeciesService;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SBMLSimpleModelService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	
	@Autowired
	ExternalResourceEntityService externalResourceEntityService;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	KEGGHttpService keggHttpService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	NameNodeService nameNodeService;
	
	@Autowired
	SBMLCompartmentService sbmlCompartmentService;
	
	@Autowired
	SBMLSpeciesService sbmlSpeciesService;
	
	@Autowired
	SBMLSBaseEntityService sbmlSBaseEntityService;
	
	@Autowired
	SBMLSimpleReactionService sbmlSimpleReactionService;
	
	@Autowired
	SBMLSimpleTransitionService sbmlSimpleTransitionService;
	
	@Autowired
	SBMLQualSpeciesService sbmlQualSpeciesService;

	@Autowired
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	Session session;
	
	@Autowired
	UtilityService utilityService;

	@Autowired
	SBML4jConfig sbml4jConfig;
	
	int SAVE_DEPTH = 1;

	private List<SBMLCompartment> getCompartmentList(Model model, ProvenanceGraphActivityNode persistActivity, DatabaseNode database) {
		List<SBMLCompartment> sbmlCompartmentList = new ArrayList<>();
		for(Compartment compartment : model.getListOfCompartments()) {
			//SBMLCompartment sbmlCompartment = new SBMLCompartment();
			// check if a compartment with that name (and those settings does already exist)
			//spatialDimensions, size, constant, sBaseName, sBaseId
			try {
				boolean foundCompartment = false;
				for (SBMLCompartment existingCompartment : this.sbmlCompartmentService.findBysBaseId(compartment.getId(), 0)) {
					if (existingCompartment != null 
							&& existingCompartment.getDatabase().equals(database)) {
						// is the compartment with the same sBaseId from the same database?
						sbmlCompartmentList.add(existingCompartment);
						this.provenanceGraphService.connect(persistActivity, existingCompartment, ProvenanceGraphEdgeType.used);
						foundCompartment = true;
						break;
					}
				}
				// other cases would be, that the name and Id are the same, but are actually different compartments
				// with different properties.
				// then append the name of the new compartment with a number (or something) and link all entities in this
				// model to that new one.
				// This means that further down, when looking up compartment, we can no longer match by sbaseId directly
				// but also need a mapping info from the original sbasid (that all entities in jsbml will reference)
				// to the newly given name.
				// but for now, as kegg only has one default compartment and it's the only time we load more than one model
				// we keep it like that.
				if (!foundCompartment) {
					SBMLCompartment sbmlCompartment = new SBMLCompartment();
					this.graphBaseEntityService.setGraphBaseEntityProperties(sbmlCompartment);
					this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(compartment, sbmlCompartment);
					this.sbmlSimpleModelUtilityServiceImpl.setCompartmentProperties(compartment, sbmlCompartment, database);
					sbmlCompartment = this.sbmlCompartmentService.save(sbmlCompartment, SAVE_DEPTH);
					sbmlCompartmentList.add(sbmlCompartment);
					this.provenanceGraphService.connect(sbmlCompartment, persistActivity, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
			} catch (IncorrectResultSizeDataAccessException e) {
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " was found multiple times");
				return null; // need to find better error handling way..
			}
		}
		return sbmlCompartmentList;
	}

	
	private void processQualSpecies(	ListOf<QualitativeSpecies> qualSpeciesListOf,
										Map<String, SBMLCompartment> compartmentLookupMap,
										Map<String, SBMLQualSpecies> sbmlQualSpeciesToPersist,
										Map<String, List<CVTerm>> cvTermsToProcess) {
		List<QualitativeSpecies> groupSpeciesList = new ArrayList<>();
		Map<String,SBMLQualSpecies> sBaseNameToSBMLQualSpeciesMap = new HashMap<>();
		for (QualitativeSpecies species :qualSpeciesListOf) {
			if(species.getName().equals("Group")) {
				groupSpeciesList.add(species);
			} else {
				SBMLQualSpecies newQualSpecies = new SBMLQualSpecies();
				this.graphBaseEntityService.setGraphBaseEntityProperties(newQualSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(species, newQualSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(species, newQualSpecies, compartmentLookupMap);
				this.sbmlSimpleModelUtilityServiceImpl.setQualSpeciesProperties(species, newQualSpecies);
				sbmlQualSpeciesToPersist.put(newQualSpecies.getEntityUUID(), newQualSpecies);
				cvTermsToProcess.put(newQualSpecies.getEntityUUID(), species.getCVTerms());
				sBaseNameToSBMLQualSpeciesMap.put(newQualSpecies.getsBaseName(), newQualSpecies);
			}
		}
		for (QualitativeSpecies species : groupSpeciesList) {
			SBMLQualSpeciesGroup newSBMLQualSpeciesGroup = new SBMLQualSpeciesGroup();
			StringBuilder groupName = new StringBuilder();
			groupName.append("Group");
			XMLNode ulNode = species.getNotes().getChildElement("body", "http://www.w3.org/1999/xhtml").getChildElement("p", null).getChildElement("ul", null);
			for (int i = 0; i != ulNode.getChildCount(); i++) {
				if(ulNode.getChild(i).getChildCount() > 0) {
					for (int j = 0; j != ulNode.getChild(i).getChildCount(); j++) {
						String chars = ulNode.getChild(i).getChild(j).getCharacters();
						groupName.append("_");
						groupName.append(chars);
						newSBMLQualSpeciesGroup.addQualSpeciesToGroup(sBaseNameToSBMLQualSpeciesMap.get(chars));
					}
				}
			}
			this.graphBaseEntityService.setGraphBaseEntityProperties(newSBMLQualSpeciesGroup);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(species, newSBMLQualSpeciesGroup);
			this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(species, newSBMLQualSpeciesGroup, compartmentLookupMap);
			this.sbmlSimpleModelUtilityServiceImpl.setQualSpeciesProperties(species, newSBMLQualSpeciesGroup);
			newSBMLQualSpeciesGroup.setsBaseName(groupName.toString());
			newSBMLQualSpeciesGroup.setsBaseMetaId("meta_" + groupName.toString());
			sbmlQualSpeciesToPersist.put(newSBMLQualSpeciesGroup.getEntityUUID(), newSBMLQualSpeciesGroup);
			sBaseNameToSBMLQualSpeciesMap.put(groupName.toString(), newSBMLQualSpeciesGroup);
			cvTermsToProcess.put(newSBMLQualSpeciesGroup.getEntityUUID(), species.getCVTerms());	
		}
	}
										
	private void processSpecies(ListOf<Species> speciesListOf,
								Map<String, SBMLCompartment> compartmentLookupMap,
								Map<String, SBMLSpecies> sbmlSpeciesToPersist,
								Map<String, List<CVTerm>> cvTermsToProcess) {
		List<Species> groupSpeciesList = new ArrayList<>();
		Map<String,SBMLSpecies> sBaseNameToSBMLSpeciesMap = new HashMap<>();
		for (Species species :speciesListOf) {
			if(species.getName().equals("Group")) {
				groupSpeciesList.add(species);
			} else {
				SBMLSpecies newSpecies = new SBMLSpecies();
				this.graphBaseEntityService.setGraphBaseEntityProperties(newSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(species, newSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(species, newSpecies, compartmentLookupMap);
				this.sbmlSimpleModelUtilityServiceImpl.setSpeciesProperties(species, newSpecies);
				sbmlSpeciesToPersist.put(newSpecies.getEntityUUID(), newSpecies);
				cvTermsToProcess.put(newSpecies.getEntityUUID(), species.getCVTerms());
				sBaseNameToSBMLSpeciesMap.put(newSpecies.getsBaseName(), newSpecies);
			}
		}
		for (Species species : groupSpeciesList) {
			SBMLSpeciesGroup newSBMLSpeciesGroup = new SBMLSpeciesGroup();
			StringBuilder groupName = new StringBuilder();
			groupName.append("Group");
			XMLNode ulNode = species.getNotes().getChildElement("body", "http://www.w3.org/1999/xhtml").getChildElement("p", null).getChildElement("ul", null);
			for (int i = 0; i != ulNode.getChildCount(); i++) {
				if(ulNode.getChild(i).getChildCount() > 0) {
					for (int j = 0; j != ulNode.getChild(i).getChildCount(); j++) {
						String chars = ulNode.getChild(i).getChild(j).getCharacters();
						groupName.append("_");
						groupName.append(chars);
						newSBMLSpeciesGroup.addSpeciesToGroup(sBaseNameToSBMLSpeciesMap.get(chars));
					}
				}
			}
			this.graphBaseEntityService.setGraphBaseEntityProperties(newSBMLSpeciesGroup);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(species, newSBMLSpeciesGroup);
			this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(species, newSBMLSpeciesGroup, compartmentLookupMap);
			this.sbmlSimpleModelUtilityServiceImpl.setSpeciesProperties(species, newSBMLSpeciesGroup);
			newSBMLSpeciesGroup.setsBaseName(groupName.toString());
			newSBMLSpeciesGroup.setsBaseMetaId("meta_" + groupName.toString());
			sbmlSpeciesToPersist.put(newSBMLSpeciesGroup.getEntityUUID(), newSBMLSpeciesGroup);
			sBaseNameToSBMLSpeciesMap.put(groupName.toString(), newSBMLSpeciesGroup);
			cvTermsToProcess.put(newSBMLSpeciesGroup.getEntityUUID(), species.getCVTerms());	
		}
	}
	
	private Map<String, Collection<BiomodelsQualifier>> processCVTerms(Map<String, List<CVTerm>> cvTerms) {
		Map<String, Collection<BiomodelsQualifier>> uuidToBQMap = new TreeMap<>();
		Map<String, NameNode> nameToNameNodeMap = new TreeMap<>();
		Map<String, ExternalResourceEntity> resourceToExternalResourceEntityMap = new TreeMap<>();
		for (String uuid : cvTerms.keySet()) {
			List<CVTerm> cv = cvTerms.get(uuid);
			uuidToBQMap.put(uuid, this.processCVTermList(cv, resourceToExternalResourceEntityMap, nameToNameNodeMap));
		}
		return uuidToBQMap;
	}
	
	private Collection<BiomodelsQualifier> processCVTermList(List<CVTerm> cvTerms, Map<String, ExternalResourceEntity> resourceToExternalResourceEntityMap, Map<String, NameNode> nameToNameNodeMap) {
		Map<String, BiomodelsQualifier> resourceUriToBiomodelsQualifierMap = new TreeMap<>();
		
		for (CVTerm cv : cvTerms) {
			this.createBiomodelsQualifierFromCVTerm(cv).forEach(resourceUriToBiomodelsQualifierMap::put);
			this.createExternalResourceEntitiesFromCVTerm(cv, resourceToExternalResourceEntityMap);
		}
		for (String resource : resourceUriToBiomodelsQualifierMap.keySet()) {
			BiomodelsQualifier bq = resourceUriToBiomodelsQualifierMap.get(resource);
			ExternalResourceEntity er = resourceToExternalResourceEntityMap.get(resource);
			if (er.getNames() != null) {
				List<String> namesToReplace = new ArrayList<>();
				for (NameNode n : er.getNames()) {
					NameNode seenNameNode = nameToNameNodeMap.putIfAbsent(n.getName(), n);
					if (seenNameNode != null) {
						// a nameNode with that value has already been produced
						if (seenNameNode.getId()==null && n.getId() != null) {
							// the already seen NameNode has no Id Set (does not come from the database) but the current one (n), does, so we need to replace the seen one
							//seenNameNode.setId(n.getId());
							//seenNameNode.setVersion(n.getVersion());
							//seenNameNode.setEntityUUID(n.getEntityUUID());
							seenNameNode = n;
						} else {
							// either both are new, or seenNameNode is new and n is not
							namesToReplace.add(n.getName());
						}
					}
				}
				namesToReplace.forEach(n -> er.replaceName(n, nameToNameNodeMap.get(n)));
			}
			bq.setEndNode(er);
		}
		return resourceUriToBiomodelsQualifierMap.values();
	}
	
	
	private Map<String, ExternalResourceEntity> createExternalResourceEntitiesFromCVTerm(CVTerm cvTerm, Map<String, ExternalResourceEntity> resourceUriToExternalResourceEntityMap) {
		
		for (String resource : cvTerm.getResources()) {
			if (!resourceUriToExternalResourceEntityMap.containsKey(resource)) {
				if (resourceUriToExternalResourceEntityMap.put(resource, this.createExternalResourceEntityFromResource(resource)) != null) {
					logger.debug("How can this be?");
				}
			}
		}
		return resourceUriToExternalResourceEntityMap;
	}

	
	private ExternalResourceEntity createExternalResourceEntityFromResource(String resource) {
		ExternalResourceEntity existingExternalResourceEntity = this.externalResourceEntityService.findByUri(resource);
		if(existingExternalResourceEntity != null) {
			return existingExternalResourceEntity;
		} else {
			ExternalResourceEntity newExternalResourceEntity = new ExternalResourceEntity();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newExternalResourceEntity);
			
			newExternalResourceEntity.setUri(resource);
			if (resource.contains("identifiers.org")) {
				// we can set the id and database
				newExternalResourceEntity.setDatabaseFromUri(this.extractDatabaseFromIdentifiersorgUri(resource));
				newExternalResourceEntity.setShortIdentifierFromUri(this.extractIdentifierFromIdentifiersorgUri(resource));
			}
			if (resource.contains("kegg.genes")) {
				newExternalResourceEntity.setType(ExternalResourceType.KEGGGENES);
				newExternalResourceEntity.setDatabaseFromUri("KEGG");
				Set<String> nameList = this.keggHttpService.getGeneNamesFromKeggURL(resource);
				if (nameList != null) {
					Iterator<String> nameIterator = nameList.iterator();
					if (nameIterator.hasNext()) {
						newExternalResourceEntity.setPrimaryName(nameIterator.next());
					}
					while (nameIterator.hasNext()) {
						String name = nameIterator.next();
						this.createNameNode(newExternalResourceEntity, name);
					}
				}
			} else if(resource.contains("kegg.reaction")) {
				newExternalResourceEntity.setType(ExternalResourceType.KEGGREACTION);
				newExternalResourceEntity.setDatabaseFromUri("KEGG");
				newExternalResourceEntity.setPrimaryName(this.keggHttpService.getReactionName(resource));
			} else if(resource.contains("kegg.drug")) {
				newExternalResourceEntity.setType(ExternalResourceType.KEGGDRUG);
				newExternalResourceEntity.setDatabaseFromUri("KEGG");
				List<String> secondaryNames = this.keggHttpService.getDrugInformationFromKEGGDrugURL(resource, newExternalResourceEntity); // changes newExternalResource entity as side effect.. bad
				if (secondaryNames != null && !secondaryNames.isEmpty()) {
					for (String name : secondaryNames) {
						this.createNameNode(newExternalResourceEntity, name);
					}
				}
			} else if(resource.contains("kegg.compound")) {
				newExternalResourceEntity.setType(ExternalResourceType.KEGGCOMPOUND);
				newExternalResourceEntity.setDatabaseFromUri("KEGG");
				List<String> secondaryNames = this.keggHttpService.setCompoundAnnotationFromResource(resource, newExternalResourceEntity); // changes newExternalResource entity as side effect.. bad
				if (secondaryNames != null && !secondaryNames.isEmpty()) {
					for (String secName : secondaryNames) {
						this.createNameNode(newExternalResourceEntity, secName);
					}
				}
			} 
			return newExternalResourceEntity;
		}

		
		
		
		
	}

	private Map<String, BiomodelsQualifier> createBiomodelsQualifierFromCVTerm(CVTerm cvTerm) {
		Map<String, BiomodelsQualifier> resourceUriToBiomodelsQualifierMap = new TreeMap<>();
		for (String resource : cvTerm.getResources()) {
			resourceUriToBiomodelsQualifierMap.put(resource, createBiomodelsQualifier(
					cvTerm.getQualifierType(), cvTerm.getQualifier()));
		}
		return resourceUriToBiomodelsQualifierMap;
	}

	
	
	private void processReactions(	ListOf<Reaction> reactionListOf,
									Map<String, SBMLCompartment> compartmentLookupMap,
									Map<String, SBMLSpecies> sBaseIdToSBMLSpeciesMap,
									Map<String, SBMLSimpleReaction> reactionUUIDToSBMLSimpleReactionMap,
									Map<String, List<CVTerm>> cvTermsToProcess) {
		for (Reaction reaction : reactionListOf) {
			SBMLSimpleReaction newReaction = new SBMLSimpleReaction();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newReaction);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(reaction, newReaction);

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
			// cvTerm annotations
			cvTermsToProcess.put(newReaction.getEntityUUID(), reaction.getCVTerms());
			
			
			// add reaction to map
			reactionUUIDToSBMLSimpleReactionMap.put(newReaction.getEntityUUID(), newReaction);
		}
	}
	
	private List<SBMLSimpleReaction> buildAndPersistSBMLSimpleReactions(ListOf<Reaction> listOfReactions,
			Map<String, SBMLCompartment> compartmentLookupMap,
			Map<String, SBMLSpecies> sBaseIdToSBMLSpeciesMap,
			ProvenanceGraphActivityNode activityNode) {
		
		if (sBaseIdToSBMLSpeciesMap == null ||sBaseIdToSBMLSpeciesMap.size() == 0) {
			// no SBMLSpecies loaded, we cannot load reactions either, unless all species have already been loaded before
			// very unlikely scenario, revisit if it occurs
			return new ArrayList<>();
		}
		
		List<SBMLSimpleReaction> sbmlSimpleReactionList = new ArrayList<>();
		for (Reaction reaction : listOfReactions) {
			
			// reaction not yet in db, build and persist it
			SBMLSimpleReaction newReaction = new SBMLSimpleReaction();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newReaction);
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
			SBMLSimpleReaction persistedSimpleReaction = this.sbmlSimpleReactionService.save(newReaction, SAVE_DEPTH);
			persistedSimpleReaction.setCvTermList(reaction.getCVTerms());
			newReaction = (SBMLSimpleReaction) buildAndPersistExternalResourcesForSBaseEntity(persistedSimpleReaction, activityNode);
			sbmlSimpleReactionList.add(this.sbmlSimpleReactionService.save(newReaction, SAVE_DEPTH));
		}
		
		
		return sbmlSimpleReactionList;
	}
	
	private void processTransitions(ListOf<Transition> transitionListOf,
			Map<String, SBMLQualSpecies> qualSBaseLookupMap,
			Map<String, SBMLSimpleTransition> transitionUUIDToSBMLSimpleTransitionMap,
			Map<String, List<CVTerm>> cvTermsToProcess
			) {
		for (Transition transition : transitionListOf) {
			SBMLSimpleTransition newSimpleTransition = new SBMLSimpleTransition();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newSimpleTransition);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(transition, newSimpleTransition);
			
			String newTransitionId = "";
			if(transition.getListOfInputs().size() > 1) {
				logger.warn("More than one Input in transition: " + transition.getName());
			}
			boolean isFirstInput = true;
			List<SBMLQualSpecies> inputQualSpeciesList = new ArrayList<>();
			for (Input input : transition.getListOfInputs()) {
				if (qualSBaseLookupMap.containsKey(input.getQualitativeSpecies())) {
					if (!isFirstInput) newTransitionId += "_";
					SBMLQualSpecies inputQualSpecies = qualSBaseLookupMap.get(input.getQualitativeSpecies());
					newTransitionId += inputQualSpecies.getsBaseName();
					isFirstInput = false;
					inputQualSpeciesList.add(inputQualSpecies);
				}
			}
			newTransitionId += "-";
			newTransitionId += this.utilityService.translateSBOString(transition.getSBOTermID());
			newTransitionId += "->"; // TODO: Should this be directional?
			boolean isFirstOutput = true;
			List<SBMLQualSpecies> outputQualSpeciesList = new ArrayList<>();
			for (Output output : transition.getListOfOutputs()) {
				if (qualSBaseLookupMap.containsKey(output.getQualitativeSpecies())) {
					if (!isFirstOutput) newTransitionId += "_";
					SBMLQualSpecies outputQualSpecies = qualSBaseLookupMap.get(output.getQualitativeSpecies());
					newTransitionId += outputQualSpecies.getsBaseName();
					isFirstOutput = false;
					outputQualSpeciesList.add(outputQualSpecies);
				}
			}
			
			newSimpleTransition.setTransitionId(newTransitionId);
			newSimpleTransition.setInputSpecies(inputQualSpeciesList); 
			newSimpleTransition.setOutputSpecies(outputQualSpeciesList);
			cvTermsToProcess.put(newSimpleTransition.getEntityUUID(), transition.getCVTerms());
			transitionUUIDToSBMLSimpleTransitionMap.put(newSimpleTransition.getEntityUUID(), newSimpleTransition);
			
		}
	}

	
	private List<SBMLSimpleTransition> buildAndPersistTransitions(
			Map<String, SBMLQualSpecies> qualSBaseLookupMap,
			ListOf<Transition> transitionListOf,
			ProvenanceGraphActivityNode activityNode) {
		List<SBMLSimpleTransition> transitionList = new ArrayList<>();
		for (Transition transition : transitionListOf) {

			String newTransitionId = "";
			if(transition.getListOfInputs().size() > 1) {
				logger.warn("More than one Input in transition: " + transition.getName());
			}
			boolean isFirstInput = true;
			List<SBMLQualSpecies> inputQualSpeciesList = new ArrayList<>();
			for (Input input : transition.getListOfInputs()) {
				if (qualSBaseLookupMap.containsKey(input.getQualitativeSpecies())) {
					if (!isFirstInput) newTransitionId += "_";
					SBMLQualSpecies inputQualSpecies = qualSBaseLookupMap.get(input.getQualitativeSpecies());
					newTransitionId += inputQualSpecies.getsBaseName();
					isFirstInput = false;
					inputQualSpeciesList.add(inputQualSpecies);
				}
			}
			newTransitionId += "-";
			newTransitionId += this.utilityService.translateSBOString(transition.getSBOTermID());
			newTransitionId += "->"; // TODO: Should this be directional?
			boolean isFirstOutput = true;
			List<SBMLQualSpecies> outputQualSpeciesList = new ArrayList<>();
			for (Output output : transition.getListOfOutputs()) {
				if (qualSBaseLookupMap.containsKey(output.getQualitativeSpecies())) {
					if (!isFirstOutput) newTransitionId += "_";
					SBMLQualSpecies outputQualSpecies = qualSBaseLookupMap.get(output.getQualitativeSpecies());
					newTransitionId += outputQualSpecies.getsBaseName();
					isFirstOutput = false;
					outputQualSpeciesList.add(outputQualSpecies);
				}
			}
			
			
			SBMLSimpleTransition newSimpleTransition = new SBMLSimpleTransition();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newSimpleTransition);
			this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(transition, newSimpleTransition);
			newSimpleTransition.setTransitionId(newTransitionId);
			newSimpleTransition.setInputSpecies(inputQualSpeciesList); // not sure if this works
			newSimpleTransition.setOutputSpecies(outputQualSpeciesList); // not sure if this works
			SBMLSimpleTransition persistedNewSimpleTransition = this.sbmlSimpleTransitionService.save(newSimpleTransition, SAVE_DEPTH);
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
			//logger.debug("Found MdAnderson Gene in sBaseName: " + sBaseEntity.getsBaseName());
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
				
				// create external resource
				
				//ExternalResourceEntity existingExternalResourceEntity eER = this.create
				// build the ExternalResource or link to existing one
				ExternalResourceEntity existingExternalResourceEntity = this.externalResourceEntityService.findByUri(resource);
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
						newExternalResourceEntity.setShortIdentifierFromUri(this.keggHttpService.getKEGGIdentifier(resource));
						Set<String> nameList = this.keggHttpService.getGeneNamesFromKeggURL(resource);
						Iterator<String> nameIterator = nameList.iterator();
						if (nameIterator.hasNext()) {
							newExternalResourceEntity.setPrimaryName(nameIterator.next());
						}
						while (nameIterator.hasNext()) {
							String name = nameIterator.next();
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
						setKeggCompoundNames(currentNames, resource, newExternalResourceEntity);
					} 
					
					//newBiomodelsQualifier.setEndNode(newExternalResourceEntity);
					ExternalResourceEntity persistedNewExternalResourceEntity = this.externalResourceEntityService.save(newExternalResourceEntity, 1); // TODO can I change this depth to one to persist the nameNodes or does that break BiomodelQualifier connections?
					newBiomodelsQualifier.setEndNode(persistedNewExternalResourceEntity);
					this.provenanceGraphService.connect(persistedNewExternalResourceEntity, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
				BiomodelsQualifier persistedNewBiomodelsQualifier = this.biomodelsQualifierRepository.save(newBiomodelsQualifier, 0);
				updatedSBaseEntity.addBiomodelsQualifier(persistedNewBiomodelsQualifier);
				updatedSBaseEntity = this.sbmlSBaseEntityService.save(updatedSBaseEntity, 0);
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
				
				ExternalResourceEntity existingMdAndersonResourceEntity = this.externalResourceEntityService.findByUri(mdAndersonUriStringBuilder.toString());
				if (existingMdAndersonResourceEntity != null) {
					mdAndersonBiomodelsQualifier.setEndNode(existingMdAndersonResourceEntity);
					this.provenanceGraphService.connect(activityNode, existingMdAndersonResourceEntity, ProvenanceGraphEdgeType.used);
				} else {
					ExternalResourceEntity newMdAndersonResourceEntity = new ExternalResourceEntity();
					this.graphBaseEntityService.setGraphBaseEntityProperties(newMdAndersonResourceEntity);
					newMdAndersonResourceEntity.setUri(mdAndersonUriStringBuilder.toString());
					newMdAndersonResourceEntity.setType(ExternalResourceType.MDANDERSON);
					newMdAndersonResourceEntity.setDatabaseFromUri("MDAnderson");
					ExternalResourceEntity persistedNewMdAndersonResourceEntity = this.externalResourceEntityService.save(newMdAndersonResourceEntity, 0);
					mdAndersonBiomodelsQualifier.setEndNode(persistedNewMdAndersonResourceEntity);
					this.provenanceGraphService.connect(persistedNewMdAndersonResourceEntity, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
				BiomodelsQualifier persistedMDAndersonBiomodelsQualifier = biomodelsQualifierRepository.save(mdAndersonBiomodelsQualifier, 0);
				updatedSBaseEntity.addBiomodelsQualifier(persistedMDAndersonBiomodelsQualifier);
				updatedSBaseEntity = this.sbmlSBaseEntityService.save(updatedSBaseEntity, 0);
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
			seenNames.put(name, nameNode);
		}
		nameNode.addResource(externalResourceEntity);
		externalResourceEntity.addName(nameNode);
	}
	private void createNameNode(ExternalResourceEntity externalResourceEntity,
			String name) {
		NameNode nameNode = this.nameNodeService.findByName(name);
		// if not found, build a new name node
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
	private BiomodelsQualifier createBiomodelsQualifier(Type qualifierType,
			Qualifier qualifier) {
		BiomodelsQualifier newBiomodelsQualifier = new BiomodelsQualifier();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newBiomodelsQualifier);
		//newBiomodelsQualifier.setEntityUUID(UUID.randomUUID().toString());
		newBiomodelsQualifier.setType(qualifierType);
		newBiomodelsQualifier.setQualifier(qualifier);
		return newBiomodelsQualifier;
	}	
	
	
	private void setKeggCompoundNames(Map<String, NameNode> seenNames, String resource, ExternalResourceEntity newExternalResourceEntity) {
		// TODO Here httpService needs to fetch the resource from KEGG Compound and set the name and secondary Name
		// what about formula? Is this more interesting? -> Do this as annotation
		List<String> secondaryNames = this.keggHttpService.setCompoundAnnotationFromResource(resource, newExternalResourceEntity);
		if (!secondaryNames.isEmpty()) {
			for (String secName : secondaryNames) {
				createNameNode(seenNames, newExternalResourceEntity, secName);
			}
		}
	}
	
	/**
	 * Extract the identifier from a identifier.org resource uri
	 * @param resource The identifiers.org uri-string to extract the identifier from
	 * @return the identifier as String
	 */
	private String extractIdentifierFromIdentifiersorgUri(String uri) {
		String[] resourceParts =  uri.split("/");
		return resourceParts[resourceParts.length - 1];
	}
	
	/**
	 * Extract the database name from a identifier.org resource uri
	 * @param resource The identifiers.org uri-string to extract the database name from
	 * @return the database name as String
	 */
	private String extractDatabaseFromIdentifiersorgUri(String uri) {
		String[] resourceParts =  uri.split("/");
		return resourceParts[resourceParts.length - 2];
	}
	
	
	/**
	 * Use an {@link SBMLReader} to extract the {@link Model} from the {@link SBMLDocument} in the provided file
	 * @param file The file to read the {@link Model} from
	 * @return The extracted {@link Model}
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public Model extractSBMLModel(MultipartFile file) throws XMLStreamException, IOException {
		InputStream fileStream = file.getInputStream();
		SBMLDocument doc;
		try {
			doc = SBMLReader.read(fileStream); 
			logger.info("Read document");
		} catch (Exception e) {
			logger.error("Exception while extracting model: ", e);
			return null;
		} finally {
			fileStream.close();
		}
		return doc.getModel();
	}

	/**
	 * Build the SBML4j model components from the provided {@link Model} and persist it in the database
	 * @param model The {@link Model} to persist
	 * @param sbmlfile The {@link FileNode} this model is connected to
	 * @param activityNode The {@link ProvenanceGraphActivityNode} that is associated with the {@link PathwayNode} created
	 * @param pathwayNode The {@link PathwayNode} to attach the extracted model components to
	 * @param database The {@link DatabaseNode} that the model is for
	 * @throws ModelPersistenceException
	 */
	public void buildAndPersist(
			Model model, 
			FileNode sbmlfile, 
			ProvenanceGraphActivityNode activityNode, 
			PathwayNode pathwayNode,
			DatabaseNode database) throws ModelPersistenceException { //Map<String, ProvenanceEntity>
		//Instant begin = Instant.now();
		Map<String, SBMLSpecies> persistedSBMLSpeciesMap = new TreeMap<>();
		// compartment
		Map<String, ProvenanceEntity> returnList= new TreeMap<>();
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model, activityNode, database);
		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLCompartment compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getsBaseId(), compartment); // sBaseId here will be the matchingAttribute, might even want to have a map <Entity, matchingAttribute> to be able to match different entities with different attributes
			returnList.putIfAbsent(compartment.getEntityUUID(), compartment);
		}
		//Instant compInst = Instant.now();
		// species
		
		Map<String, List<CVTerm>> speciesUUIDToCVTermMap = new TreeMap<>();
		this.processSpecies(model.getListOfSpecies(), compartmentLookupMap, persistedSBMLSpeciesMap, speciesUUIDToCVTermMap);
		Map<String, Collection<BiomodelsQualifier>> speciesUUIDToBiomodelsQualfierMap = this.processCVTerms(speciesUUIDToCVTermMap);
		List<BiomodelsQualifier> persistBQList = new ArrayList<>();
		List<SBMLSpecies> speciesWithoutBQ = new ArrayList<>();
		for (String speciesUUID : speciesUUIDToBiomodelsQualfierMap.keySet()) {
			Collection<BiomodelsQualifier> bqList = speciesUUIDToBiomodelsQualfierMap.get(speciesUUID);
			if (bqList == null || bqList.isEmpty()) {
				speciesWithoutBQ.add(persistedSBMLSpeciesMap.get(speciesUUID));
			} else {
				for (BiomodelsQualifier bq : bqList) {
					bq.setStartNode(persistedSBMLSpeciesMap.get(speciesUUID));
					persistBQList.add(bq);
				}
			}
		
		}
		//Instant speciesBuilt = Instant.now();
		
		//Instant speciesSessionClear = null;
		//Instant speciesPersisted = null;
		Iterable<BiomodelsQualifier> persistedBiomodelQualifier = null;
		Iterable<SBMLSpecies> sbmlSpeciesWithoutBQPersisted = null;
		try {
			this.session.clear();
			//speciesSessionClear = Instant.now();
			//persistedBiomodelQualifier = this.biomodelsQualifierRepository.save(persistBQList, 1);
			persistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(persistBQList);
			if (!speciesWithoutBQ.isEmpty()) {
				sbmlSpeciesWithoutBQPersisted = this.sbmlSpeciesService.saveAll(speciesWithoutBQ);
			}
			//speciesPersisted = Instant.now();
		} catch (Exception e) {
			// retry once
			e.printStackTrace();
			this.session.clear();
			System.gc();
			try {
				//persistedBiomodelQualifier = this.biomodelsQualifierRepository.save(persistBQList, 1);
				persistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(persistBQList);
				if (!speciesWithoutBQ.isEmpty()) {
					sbmlSpeciesWithoutBQPersisted = this.sbmlSpeciesService.saveAll(speciesWithoutBQ);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				// try again with other save method
				this.session.clear();
				System.gc();
				try {
					persistedBiomodelQualifier = this.biomodelsQualifierRepository.save(persistBQList, 1);
					if (!speciesWithoutBQ.isEmpty()) {
						sbmlSpeciesWithoutBQPersisted = this.sbmlSpeciesService.save(speciesWithoutBQ, 1);
					}
					//persistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(persistBQList);
				} catch (Exception e3) {
					throw new ModelPersistenceException("SBMLSpecies: Failed to persist. " + e3.getMessage());
				}
			
			}
		}
		Map<String, SBMLSpecies> sBaseIdToSBMLSpeciesMap = new HashMap<>();
		Map<String, SBMLSpecies> sBaseNameToSBMLSpeciesMap = new HashMap<>();
		try {
			for (BiomodelsQualifier persistedBQ : persistedBiomodelQualifier) {
				SBMLSBaseEntity startNode = persistedBQ.getStartNode();
				sBaseIdToSBMLSpeciesMap.put(startNode.getsBaseId(), (SBMLSpecies) startNode);
				sBaseNameToSBMLSpeciesMap.put(startNode.getsBaseName(), (SBMLSpecies) startNode);
				returnList.putIfAbsent(startNode.getEntityUUID(), startNode);
			}
			if (sbmlSpeciesWithoutBQPersisted != null) {
				for (SBMLSpecies persistedSBMLSpeciesWithoutBQ : sbmlSpeciesWithoutBQPersisted) {
					sBaseIdToSBMLSpeciesMap.put(persistedSBMLSpeciesWithoutBQ.getsBaseId(), persistedSBMLSpeciesWithoutBQ);
					sBaseNameToSBMLSpeciesMap.put(persistedSBMLSpeciesWithoutBQ.getsBaseName(), persistedSBMLSpeciesWithoutBQ);
					returnList.putIfAbsent(persistedSBMLSpeciesWithoutBQ.getEntityUUID(), persistedSBMLSpeciesWithoutBQ);
				}
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
			// TODO:Rollback?
			return;// null;
		}
		/*Map<String, SBMLSpecies> sBaseIdToSBMLSpeciesMap = new HashMap<>();
		if (model.getListOfSpecies() != null && model.getListOfSpecies().size() > 0) {
			persistedSBMLSpeciesMap = buildAndPersistSBMLSpecies(model.getListOfSpecies(), compartmentLookupMap, activityNode);
			persistedSBMLSpeciesMap.forEach((name, species)->{
				returnList.add(species);
				sBaseIdToSBMLSpeciesMap.put(species.getsBaseId(), species);
			});
		}*/
		//Instant speciesInst = Instant.now();
		
		logger.info("Read and persisted SBMLSpecies");
		
		// reactions
		/*
		 * This was the old way for reactions
		 * List<SBMLSimpleReaction> persistedSBMLSimpleReactions = null;
		if(model.getListOfReactions() != null && model.getListOfReactions().size() > 0) {
			persistedSBMLSimpleReactions = buildAndPersistSBMLSimpleReactions(model.getListOfReactions(), compartmentLookupMap, sBaseIdToSBMLSpeciesMap, activityNode);
			persistedSBMLSimpleReactions.forEach(reaction->{
				returnList.putIfAbsent(reaction.getEntityUUID(), reaction);
			});
		}
		*/
		// here starts the new way for reactions
		Map<String, List<CVTerm>> reactionUUIDToCVTermMap = new TreeMap<>();
		Map<String, SBMLSimpleReaction> reactionUUIDToSBMLSimpleReactionMap = new TreeMap<>();
		this.processReactions(model.getListOfReactions(), compartmentLookupMap, sBaseIdToSBMLSpeciesMap, reactionUUIDToSBMLSimpleReactionMap, reactionUUIDToCVTermMap);
		Map<String, Collection<BiomodelsQualifier>> reactionUUIDToBiomodelsQualfierMap = this.processCVTerms(reactionUUIDToCVTermMap);
		List<BiomodelsQualifier> reactionPersistBQList = new ArrayList<>();
		List<SBMLSimpleReaction> reactionsWithoutBQ = new ArrayList<>();
		for (String reactionUUID : reactionUUIDToBiomodelsQualfierMap.keySet()) {
			Collection<BiomodelsQualifier> bqList = reactionUUIDToBiomodelsQualfierMap.get(reactionUUID);
			if (bqList == null || bqList.isEmpty()) {
				reactionsWithoutBQ.add(reactionUUIDToSBMLSimpleReactionMap.get(reactionUUID));
			} else {
				for (BiomodelsQualifier bq : bqList) {
					bq.setStartNode(reactionUUIDToSBMLSimpleReactionMap.get(reactionUUID));
					reactionPersistBQList.add(bq);
				}
			}
		
		}
		
		// persist the reactions
		Iterable<BiomodelsQualifier> reactionPersistedBiomodelQualifier = null;
		Iterable<SBMLSimpleReaction> reactionsWithoutBQPersisted = null;
		try {
			this.session.clear();
			//speciesSessionClear = Instant.now();
			//persistedBiomodelQualifier = this.biomodelsQualifierRepository.save(persistBQList, 1);
			reactionPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(reactionPersistBQList);
			if (!reactionsWithoutBQ.isEmpty()) {
				reactionsWithoutBQPersisted = this.sbmlSimpleReactionService.saveAll(reactionsWithoutBQ);
			}
			//speciesPersisted = Instant.now();
		} catch (Exception e) {
			// retry once
			e.printStackTrace();
			this.session.clear();
			System.gc();
			try {
				reactionPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(reactionPersistBQList);
				if (!reactionsWithoutBQ.isEmpty()) {
					reactionsWithoutBQPersisted = this.sbmlSimpleReactionService.saveAll(reactionsWithoutBQ);
				}
			} catch (Exception e2) {
				e2.printStackTrace();
				// try again with other save method
				this.session.clear();
				System.gc();
				try {
					reactionPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(reactionPersistBQList, 2);
					if (!reactionsWithoutBQ.isEmpty()) {
						reactionsWithoutBQPersisted = this.sbmlSimpleReactionService.save(reactionsWithoutBQ, 1);
					}
				} catch (Exception e3) {
					throw new ModelPersistenceException("SBMLSimpleReactions: Failed to persist. " + e3.getMessage());
				}
			
			}
		}
		
		// add reactions to return list
		for (BiomodelsQualifier persistedBQ : reactionPersistedBiomodelQualifier) {
			SBMLSBaseEntity startNode = persistedBQ.getStartNode();
			returnList.putIfAbsent(startNode.getEntityUUID(), startNode);
		}
		if (sbmlSpeciesWithoutBQPersisted != null) {
			for (SBMLSimpleReaction persistedSBMLReactionWithoutBQ : reactionsWithoutBQPersisted) {
				returnList.putIfAbsent(persistedSBMLReactionWithoutBQ.getEntityUUID(), persistedSBMLReactionWithoutBQ);
			}
		}
		logger.info("Read and persisted SBMLSimpleReactions");
		// end reactions
		
		Map<String, SBMLQualSpecies> persistedSBMLQualSpeciesMap = null;
		List<SBMLSimpleTransition> persistedTransitionList = null;
		/*Instant reactionsInst = Instant.now();
		Instant qualSpeciesInst = null;
		
		Instant qualSpeciesBuilt  = null;
		Instant qualSpeciesSessionClear = null;
		Instant qualSpeciesPersisted = null;
		Instant transInst = null;*/
		// Qual Model Plugin:
		if(model.getExtension("qual") != null ) {
			QualModelPlugin qualModelPlugin = (QualModelPlugin) model.getExtension("qual");

			// qualitative Species
			if(qualModelPlugin.getListOfQualitativeSpecies() != null && qualModelPlugin.getListOfQualitativeSpecies().size() > 0) {
				/*HelperQualSpeciesReturn qualSpeciesHelper = buildAndPersistSBMLQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), persistedSBMLSpeciesMap, compartmentLookupMap, activityNode);
				
				persistedQualSpeciesList = qualSpeciesHelper.getSBaseNameToQualSpeciesMap();
				persistedQualSpeciesList.forEach((k, qualSpecies)-> {
					returnList.add(qualSpecies);
				});
				
				Map<String, SBMLQualSpecies> qualSBaseLookupMap = qualSpeciesHelper.getSBaseIdToQualSpeciesMap();
				*/
				persistedSBMLQualSpeciesMap = new TreeMap<>();
				Map<String, List<CVTerm>> qualSpeciesUUIDToCVTermMap = new TreeMap<>();
				persistedSBMLQualSpeciesMap = new TreeMap<>();
				this.processQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap, persistedSBMLQualSpeciesMap, qualSpeciesUUIDToCVTermMap);
				Map<String, Collection<BiomodelsQualifier>> qualSpeciesUUIDToBiomodelsQualfierMap = this.processCVTerms(qualSpeciesUUIDToCVTermMap);
				List<BiomodelsQualifier> qualPersistBQList = new ArrayList<>();
				List<SBMLQualSpecies> qualSpeciesWithoutBQ = new ArrayList<>();
				for (String qualSpeciesUUID :qualSpeciesUUIDToBiomodelsQualfierMap.keySet()) {
					Collection<BiomodelsQualifier> bqList = qualSpeciesUUIDToBiomodelsQualfierMap.get(qualSpeciesUUID);
					if (bqList == null || bqList.isEmpty()) {
						// the qualSpecies with qualSpeciesUUID does not have biomodelsqualifier on it
						qualSpeciesWithoutBQ.add(persistedSBMLQualSpeciesMap.get(qualSpeciesUUID));
					} else {
						for (BiomodelsQualifier bq : bqList) {
							SBMLQualSpecies startNode = persistedSBMLQualSpeciesMap.get(qualSpeciesUUID);
							startNode.setCorrespondingSpecies(sBaseNameToSBMLSpeciesMap.get(startNode.getsBaseName()));
							bq.setStartNode(startNode);
							qualPersistBQList.add(bq);
						}
					}
				}
				//qualSpeciesBuilt = Instant.now();
				Iterable<BiomodelsQualifier> qualPersistedBiomodelQualifier = null;
				Iterable<SBMLQualSpecies> qualSpeciesWithoutBQPersisted = null;
				try {
					this.session.clear();
					//qualSpeciesSessionClear = Instant.now();
					//qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(qualPersistBQList, 1);
					qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(qualPersistBQList);
					if (!qualSpeciesWithoutBQ.isEmpty()) {
						qualSpeciesWithoutBQPersisted = this.sbmlQualSpeciesService.saveAll(qualSpeciesWithoutBQ);
					}
					//qualSpeciesPersisted = Instant.now();
				} catch (Exception e) {
					// retry once
					e.printStackTrace();
					this.session.clear();
					System.gc();
					try {
						//qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(qualPersistBQList, 1);
						qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(qualPersistBQList);
						if (!qualSpeciesWithoutBQ.isEmpty()) {
							qualSpeciesWithoutBQPersisted = this.sbmlQualSpeciesService.saveAll(qualSpeciesWithoutBQ);
						}
					} catch (Exception e2) {
						e2.printStackTrace();
						this.session.clear();
						System.gc();
						try {
							qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(qualPersistBQList, 1);
							//qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(qualPersistBQList);
							if (!qualSpeciesWithoutBQ.isEmpty()) {
								qualSpeciesWithoutBQPersisted = this.sbmlQualSpeciesService.save(qualSpeciesWithoutBQ, 1);
							}
						} catch (Exception e3) {
							e2.printStackTrace();
							throw new ModelPersistenceException("SBMLQualSpecies: Failed to persist. " + e3.getMessage());
						}
					}
				}
				Map<String, SBMLQualSpecies> sBaseIdToSBMLQualSpeciesMap = new HashMap<>();
				
				try {
					for (BiomodelsQualifier persistedBQ : qualPersistedBiomodelQualifier) {
						SBMLSBaseEntity startNode = persistedBQ.getStartNode();
						sBaseIdToSBMLQualSpeciesMap.put(startNode.getsBaseId(), (SBMLQualSpecies) startNode);
						returnList.putIfAbsent(startNode.getEntityUUID(), startNode);
					}
					if (qualSpeciesWithoutBQPersisted != null) {
						for (SBMLQualSpecies persistedQualSpeciesWithoutBQ :qualSpeciesWithoutBQPersisted) {
							sBaseIdToSBMLQualSpeciesMap.put(persistedQualSpeciesWithoutBQ.getsBaseId(), persistedQualSpeciesWithoutBQ);
							returnList.putIfAbsent(persistedQualSpeciesWithoutBQ.getEntityUUID(), persistedQualSpeciesWithoutBQ);
						}
					}
				} catch (ClassCastException e) {
					e.printStackTrace();
					// TODO:Rollback?
					return;// null;
				}
				
				//qualSpeciesInst = Instant.now();
				logger.info("Read and persisted SBMLQualSpecies");
				// transitions (qual model plugin)
				if(qualModelPlugin.getListOfTransitions() != null && qualModelPlugin.getListOfTransitions().size() > 0) {
					persistedTransitionList	= buildAndPersistTransitions(sBaseIdToSBMLQualSpeciesMap, qualModelPlugin.getListOfTransitions(), activityNode);
					persistedTransitionList.forEach(transition-> {
						returnList.putIfAbsent(transition.getEntityUUID(), transition);
					});
					Map<String, List<CVTerm>> transitionUUIDToCVTermMap = new TreeMap<>();
					Map<String, SBMLSimpleTransition> transitionUUIDToSBMLSimpleTransitionMap = new TreeMap<>();
					this.processTransitions(qualModelPlugin.getListOfTransitions(), 
											sBaseIdToSBMLQualSpeciesMap, 
											transitionUUIDToSBMLSimpleTransitionMap, 
											transitionUUIDToCVTermMap);
					Map<String, Collection<BiomodelsQualifier>> transitionUUIDToBiomodelsQualfierMap = this.processCVTerms(transitionUUIDToCVTermMap);
					
					List<BiomodelsQualifier> transitionPersistBQList = new ArrayList<>();
					List<SBMLSimpleTransition> transitionWithoutBQ = new ArrayList<>();
					for (String transitionUUID : transitionUUIDToBiomodelsQualfierMap.keySet()) {
						Collection<BiomodelsQualifier> bqList = transitionUUIDToBiomodelsQualfierMap.get(transitionUUID);
						if (bqList == null || bqList.isEmpty()) {
							transitionWithoutBQ.add(transitionUUIDToSBMLSimpleTransitionMap.get(transitionUUID));
						} else {
							for (BiomodelsQualifier bq : bqList) {
								bq.setStartNode(transitionUUIDToSBMLSimpleTransitionMap.get(transitionUUID));
								transitionPersistBQList.add(bq);
							}
						}
					
					}
					
					// then persist transitions
					Iterable<BiomodelsQualifier> simpleTransitionsPersistedBiomodelQualifier = null;
					Iterable<SBMLSimpleTransition> simpleTransitionsWithoutBQPersisted = null;
					try {
						this.session.clear();

						simpleTransitionsPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(transitionPersistBQList);
						if (!transitionWithoutBQ.isEmpty()) {
							simpleTransitionsWithoutBQPersisted = this.sbmlSimpleTransitionService.saveAll(transitionWithoutBQ);
						}
					} catch (Exception e) {
						// retry once
						e.printStackTrace();
						this.session.clear();
						System.gc();
						try {
							simpleTransitionsPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(transitionPersistBQList);
							if (!transitionWithoutBQ.isEmpty()) {
								simpleTransitionsWithoutBQPersisted = this.sbmlSimpleTransitionService.saveAll(transitionWithoutBQ);
							}
						} catch (Exception e2) {
							e2.printStackTrace();
							// try again with other save method
							this.session.clear();
							System.gc();
							try {
								simpleTransitionsPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(transitionPersistBQList, 2);
								if (!transitionWithoutBQ.isEmpty()) {
									simpleTransitionsWithoutBQPersisted = this.sbmlSimpleTransitionService.save(transitionWithoutBQ, 1);
								}
							} catch (Exception e3) {
								throw new ModelPersistenceException("SBMLimpleTransitions: Failed to persist. " + e3.getMessage());
							}
						
						}
					}
					try {
						for (BiomodelsQualifier persistedBQ : simpleTransitionsPersistedBiomodelQualifier) {
							SBMLSBaseEntity startNode = persistedBQ.getStartNode();
							returnList.putIfAbsent(startNode.getEntityUUID(), startNode);
						}
						if (simpleTransitionsWithoutBQPersisted != null) {
							for (SBMLSimpleTransition persistedTransitionWithoutBQ :simpleTransitionsWithoutBQPersisted) {
								returnList.putIfAbsent(persistedTransitionWithoutBQ.getEntityUUID(), persistedTransitionWithoutBQ);
							}
						}
					} catch (ClassCastException e) {
						e.printStackTrace();
						// TODO:Rollback?
						return;// null;
					}
					
				}
				//transInst = Instant.now();
				logger.info("Read and persisted SBMLSimpleTransitions");
			}
		}
		/*StringBuilder sb = new StringBuilder();
		sb.append("PersistanceTime: ");
		this.utilityService.appendDurationString(sb, Duration.between(begin, compInst), "compartments");
		this.utilityService.appendDurationString(sb, Duration.between(compInst, speciesBuilt), "speciesbuilt");
		if (speciesSessionClear != null) this.utilityService.appendDurationString(sb, Duration.between(speciesBuilt, speciesSessionClear), "speciesSessionClear");
		if (speciesSessionClear != null && speciesPersisted != null) this.utilityService.appendDurationString(sb, Duration.between(speciesSessionClear, speciesPersisted), "speciesPersisted");
		if (speciesPersisted != null) this.utilityService.appendDurationString(sb, Duration.between(speciesPersisted, speciesInst), "speciesDone");
		if(speciesSessionClear == null || speciesPersisted == null) this.utilityService.appendDurationString(sb, Duration.between(compInst, speciesBuilt), "speciesbuilt");
		
		this.utilityService.appendDurationString(sb, Duration.between(speciesInst, reactionsInst), "reactions");
		if(qualSpeciesBuilt != null) this.utilityService.appendDurationString(sb, Duration.between(reactionsInst, qualSpeciesBuilt), "qualSpeciesBuilt");
		if (qualSpeciesSessionClear != null) this.utilityService.appendDurationString(sb, Duration.between(qualSpeciesBuilt, qualSpeciesSessionClear), "qualSpeciesSessionClear");
		if (qualSpeciesSessionClear != null && qualSpeciesPersisted != null) this.utilityService.appendDurationString(sb, Duration.between(qualSpeciesSessionClear, qualSpeciesPersisted), "qualSpeciesPersisted");
		if (qualSpeciesPersisted != null && qualSpeciesInst != null) this.utilityService.appendDurationString(sb, Duration.between(qualSpeciesPersisted, qualSpeciesInst), "qualSpeciesDone");
				
		if (transInst != null) this.utilityService.appendDurationString(sb, Duration.between(qualSpeciesInst, transInst), "transitions");
		logger.info("Persisted " + returnList.size() + " entities. " 	+ (persistedSBMLSpeciesMap != null ? "Species: " + persistedSBMLSpeciesMap.size() + "; ":"")
																		+ (persistedSBMLSimpleReactions != null ? "Ractions: " + persistedSBMLSimpleReactions.size() + "; " :"")
																		+ (persistedSBMLQualSpeciesMap != null ? "QualSpecies: " + persistedSBMLQualSpeciesMap.size() + "; " :"")
																		+ (persistedTransitionList != null ? "Transitions: " + persistedTransitionList.size() :"")
																		);	
		logger.info(sb.toString());
		 */
		for (ProvenanceEntity entity : returnList.values()) {
			this.warehouseGraphService.connect(pathwayNode, entity.getEntityUUID(), WarehouseGraphEdgeType.CONTAINS);
		}
		logger.info("Connected Entitites to pathway Node");
		//return returnList; // This is the list<sources> of the Knowledge Graph "WarehouseGraphNode" Node, which then serves as Source for the network mappings, derived from full KEGG
	}
}
