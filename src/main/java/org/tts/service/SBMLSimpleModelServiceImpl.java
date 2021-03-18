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
import org.tts.Exception.ModelPersistenceException;
import org.tts.config.SBML4jConfig;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
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
import org.tts.model.warehouse.PathwayNode;
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
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	Session session;
	
	SBMLSpeciesRepository sbmlSpeciesRepository;
	SBMLSimpleReactionRepository sbmlSimpleReactionRepository;
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	SBMLQualSpeciesRepository sbmlQualSpeciesRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	ExternalResourceEntityRepository externalResourceEntityRepository;
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	GraphBaseEntityRepository graphBaseEntityRepository;
	KEGGHttpService keggHttpService;
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
			KEGGHttpService keggHttpService,
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
		this.keggHttpService = keggHttpService;
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
		ExternalResourceEntity existingExternalResourceEntity = this.externalResourceEntityRepository.findByUri(resource);
		if(existingExternalResourceEntity != null) {
			return existingExternalResourceEntity;
		} else {
			ExternalResourceEntity newExternalResourceEntity = new ExternalResourceEntity();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newExternalResourceEntity);
			
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
					this.createNameNode(newExternalResourceEntity, name);
				}
			} else if(resource.contains("kegg.reaction")) {
				newExternalResourceEntity.setType(ExternalResourceType.KEGGREACTION);
				newExternalResourceEntity.setDatabaseFromUri("KEGG");
			} else if(resource.contains("kegg.drug")) {
				newExternalResourceEntity.setType(ExternalResourceType.KEGGDRUG);
				newExternalResourceEntity.setDatabaseFromUri("KEGG");
				//newExternalResourceEntity.setName(sBaseEntity.getsBaseName());
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
						createNameNode(newExternalResourceEntity, secName);
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
				
				newSpecies = this.sbmlSpeciesRepository.save(newSpecies, SAVE_DEPTH);
				newSpecies.setCvTermList(species.getCVTerms());
				newSpecies = (SBMLSpecies) buildAndPersistExternalResourcesForSBaseEntity(newSpecies, activityNode);
				newSpecies = this.sbmlSpeciesRepository.save(newSpecies, SAVE_DEPTH);
				speciesList.add(newSpecies);
				sBaseNameToSBMLSpeciesMap.put(newSpecies.getsBaseName(), newSpecies);
				this.provenanceGraphService.connect(newSpecies, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
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
			newSBMLSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup, SAVE_DEPTH);
			newSBMLSpeciesGroup.setCvTermList(species.getCVTerms());
			newSBMLSpeciesGroup = (SBMLSpeciesGroup) buildAndPersistExternalResourcesForSBaseEntity(newSBMLSpeciesGroup, activityNode);
			newSBMLSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup, SAVE_DEPTH);
			speciesList.add(newSBMLSpeciesGroup);
			sBaseNameToSBMLSpeciesMap.put(speciesSbaseName, newSBMLSpeciesGroup);
			this.provenanceGraphService.connect(newSBMLSpeciesGroup, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			
		}
		return sBaseNameToSBMLSpeciesMap;
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
		boolean coreLoaded = true;
		if (persistedSBMLSpeciesMap == null || persistedSBMLSpeciesMap.size() == 0) {
			coreLoaded = false;
		}
		
		for (QualitativeSpecies qualSpecies : qualSpeciesListOf) {
			if(qualSpecies.getName().equals("Group")) {
				//logger.debug("found qual Species group");
				groupQualSpeciesList.add(qualSpecies);// need to do them after all species have been persisted
			} else {
				SBMLQualSpecies newQualSpecies = new SBMLQualSpecies();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newQualSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setSbaseProperties(qualSpecies, newQualSpecies);
				this.sbmlSimpleModelUtilityServiceImpl.setCompartmentalizedSbaseProperties(qualSpecies, newQualSpecies, compartmentLookupMap);
				this.sbmlSimpleModelUtilityServiceImpl.setQualSpeciesProperties(qualSpecies, newQualSpecies);
				if (coreLoaded) newQualSpecies.setCorrespondingSpecies(persistedSBMLSpeciesMap.get(qualSpecies.getName()));
				newQualSpecies = this.sbmlQualSpeciesRepository.save(newQualSpecies, SAVE_DEPTH);
				
				newQualSpecies.setCvTermList(qualSpecies.getCVTerms());
				newQualSpecies = (SBMLQualSpecies) buildAndPersistExternalResourcesForSBaseEntity(newQualSpecies, activityNode);
				newQualSpecies = this.sbmlQualSpeciesRepository.save(newQualSpecies, SAVE_DEPTH);
				
				helperQualSpeciesReturn.addQualSpecies(newQualSpecies);
				//sBaseIdToQualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
				//sBaseNameToQualSpeciesMap.put(persistedNewQualSpecies.getsBaseName(), persistedNewQualSpecies);
				
				this.provenanceGraphService.connect(newQualSpecies, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
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
			
			if (coreLoaded) newSBMLQualSpeciesGroup.setCorrespondingSpecies(persistedSBMLSpeciesMap.get(qualSpeciesSbaseName));
			
			newSBMLQualSpeciesGroup = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup, SAVE_DEPTH);
			
			newSBMLQualSpeciesGroup = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup, SAVE_DEPTH);
			newSBMLQualSpeciesGroup.setCvTermList(qualSpecies.getCVTerms());
			newSBMLQualSpeciesGroup = (SBMLQualSpeciesGroup) buildAndPersistExternalResourcesForSBaseEntity(newSBMLQualSpeciesGroup, activityNode);
			newSBMLQualSpeciesGroup = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup, SAVE_DEPTH);
			
			helperQualSpeciesReturn.addQualSpecies(newSBMLQualSpeciesGroup);
			
			//sBaseIdToQualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
			//helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), persistedNewQualSpecies);
			this.provenanceGraphService.connect(newSBMLQualSpeciesGroup, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			
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
					ExternalResourceEntity persistedNewExternalResourceEntity = this.externalResourceEntityRepository.save(newExternalResourceEntity, 1); // TODO can I change this depth to one to persist the nameNodes or does that break BiomodelQualifier connections?
					newBiomodelsQualifier.setEndNode(persistedNewExternalResourceEntity);
					this.provenanceGraphService.connect(persistedNewExternalResourceEntity, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
				BiomodelsQualifier persistedNewBiomodelsQualifier = this.biomodelsQualifierRepository.save(newBiomodelsQualifier, 0);
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
		InputStream fileStream = file.getInputStream();
		SBMLDocument doc;
		try {
			doc = SBMLReader.read(fileStream); 
		} finally {
			fileStream.close();
		}
		return doc.getModel();
	}

	@Override
	public void buildAndPersist(Model model, FileNode sbmlfile, ProvenanceGraphActivityNode activityNode, PathwayNode pathwayNode) throws ModelPersistenceException { //Map<String, ProvenanceEntity>
		//Instant begin = Instant.now();
		Map<String, SBMLSpecies> persistedSBMLSpeciesMap = new TreeMap<>();
		// compartment
		Map<String, ProvenanceEntity> returnList= new TreeMap<>();
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model, activityNode);
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
		for (String speciesUUID :speciesUUIDToBiomodelsQualfierMap.keySet()) {
			speciesUUIDToBiomodelsQualfierMap.get(speciesUUID).forEach(bq -> {
				bq.setStartNode(persistedSBMLSpeciesMap.get(speciesUUID));
				persistBQList.add(bq);
			});
		}
		//Instant speciesBuilt = Instant.now();
		
		//Instant speciesSessionClear = null;
		//Instant speciesPersisted = null;
		Iterable<BiomodelsQualifier> persistedBiomodelQualifier = null;
		try {
			this.session.clear();
			//speciesSessionClear = Instant.now();
			//persistedBiomodelQualifier = this.biomodelsQualifierRepository.save(persistBQList, 1);
			persistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(persistBQList);
			//speciesPersisted = Instant.now();
		} catch (Exception e) {
			// retry once
			e.printStackTrace();
			this.session.clear();
			System.gc();
			try {
				//persistedBiomodelQualifier = this.biomodelsQualifierRepository.save(persistBQList, 1);
				persistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(persistBQList);
			} catch (Exception e2) {
				e2.printStackTrace();
				// try again with other save method
				this.session.clear();
				System.gc();
				try {
					persistedBiomodelQualifier = this.biomodelsQualifierRepository.save(persistBQList, 1);
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
		// reactions
		List<SBMLSimpleReaction> persistedSBMLSimpleReactions = null;
		if(model.getListOfReactions() != null && model.getListOfReactions().size() > 0) {
			persistedSBMLSimpleReactions = buildAndPersistSBMLSimpleReactions(model.getListOfReactions(), compartmentLookupMap, sBaseIdToSBMLSpeciesMap, activityNode);
			persistedSBMLSimpleReactions.forEach(reaction->{
				returnList.putIfAbsent(reaction.getEntityUUID(), reaction);
			});
		}
		
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
				for (String qualSpeciesUUID :qualSpeciesUUIDToBiomodelsQualfierMap.keySet()) {
					for (BiomodelsQualifier bq : qualSpeciesUUIDToBiomodelsQualfierMap.get(qualSpeciesUUID)) {
						SBMLQualSpecies startNode = persistedSBMLQualSpeciesMap.get(qualSpeciesUUID);
						startNode.setCorrespondingSpecies(sBaseNameToSBMLSpeciesMap.get(startNode.getsBaseName()));
						bq.setStartNode(startNode);
						qualPersistBQList.add(bq);
					}
				}
				//qualSpeciesBuilt = Instant.now();
				Iterable<BiomodelsQualifier> qualPersistedBiomodelQualifier = null;
				try {
					this.session.clear();
					//qualSpeciesSessionClear = Instant.now();
					//qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(qualPersistBQList, 1);
					qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(qualPersistBQList);
					//qualSpeciesPersisted = Instant.now();
				} catch (Exception e) {
					// retry once
					e.printStackTrace();
					this.session.clear();
					System.gc();
					try {
						//qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(qualPersistBQList, 1);
						qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(qualPersistBQList);
					} catch (Exception e2) {
						e2.printStackTrace();
						this.session.clear();
						System.gc();
						try {
							qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.save(qualPersistBQList, 1);
							//qualPersistedBiomodelQualifier = this.biomodelsQualifierRepository.saveAll(qualPersistBQList);
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
				} catch (ClassCastException e) {
					e.printStackTrace();
					// TODO:Rollback?
					return;// null;
				}
				
				//qualSpeciesInst = Instant.now();
				
				// transitions (qual model plugin)
				if(qualModelPlugin.getListOfTransitions() != null && qualModelPlugin.getListOfTransitions().size() > 0) {
					persistedTransitionList	= buildAndPersistTransitions(sBaseIdToSBMLQualSpeciesMap, qualModelPlugin.getListOfTransitions(), activityNode);
					persistedTransitionList.forEach(transition-> {
						returnList.putIfAbsent(transition.getEntityUUID(), transition);
					});
				}
				//transInst = Instant.now();
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
			this.warehouseGraphService.connect(pathwayNode, entity, WarehouseGraphEdgeType.CONTAINS, false);
		}
		//return returnList; // This is the list<sources> of the Knowledge Graph "WarehouseGraphNode" Node, which then serves as Source for the network mappings, derived from full KEGG
	}
}
