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
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentalizedSBase;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.Output;
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
import org.tts.model.common.HelperQualSpeciesReturn;
import org.tts.model.common.SBMLCompartment;
import org.tts.model.common.SBMLCompartmentalizedSBaseEntity;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLQualSpeciesGroup;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.full.SBMLDocumentEntity;
import org.tts.model.full.SBMLModelEntity;
import org.tts.model.full.ext.SBMLModelExtension;
import org.tts.model.full.ext.SBMLSBaseExtension;
import org.tts.model.full.ext.qual.SBMLQualInput;
import org.tts.model.full.ext.qual.SBMLQualModelExtension;
import org.tts.model.full.ext.qual.SBMLQualOutput;
import org.tts.model.full.ext.qual.SBMLQualTransition;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.warehouse.FileNode;
import org.tts.repository.common.BiomodelsQualifierRepository;
import org.tts.repository.common.ExternalResourceEntityRepository;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLCompartmentalizedSBaseEntityRepository;
import org.tts.repository.common.SBMLQualSpeciesRepository;
import org.tts.repository.common.SBMLSBaseEntityRepository;
import org.tts.repository.common.SBMLSpeciesRepository;

@Service
public class SBMLFullModelServiceImpl implements SBMLService {

	GraphBaseEntityRepository graphBaseRepository;
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	SBMLCompartmentalizedSBaseEntityRepository sbmlCompartmentalizedSBaseEntityRepository;
	ExternalResourceEntityRepository externalResourceEntityRepository;
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	SBMLQualSpeciesRepository sbmlQualSpeciesRepository;
	SBMLSpeciesRepository sbmlSpeciesRepository;
	
	@Autowired
	public SBMLFullModelServiceImpl(	GraphBaseEntityRepository graphBaseRepository, 
							SBMLSBaseEntityRepository sbmlSBaseEntityRepository,
							ExternalResourceEntityRepository externalResourceEntityRepository,
							BiomodelsQualifierRepository biomodelsQualifierRepository,
							SBMLCompartmentalizedSBaseEntityRepository sbmlCompartmentalizedSBaseEntityRepository,
							SBMLQualSpeciesRepository sbmlQualSpeciesRepository,
							SBMLSpeciesRepository sbmlSpeciesRepository) {
		super();
		this.graphBaseRepository = graphBaseRepository;
		this.sbmlSBaseEntityRepository = sbmlSBaseEntityRepository;
		this.externalResourceEntityRepository = externalResourceEntityRepository;
		this.biomodelsQualifierRepository = biomodelsQualifierRepository;
		this.sbmlCompartmentalizedSBaseEntityRepository = sbmlCompartmentalizedSBaseEntityRepository;
		this.sbmlQualSpeciesRepository = sbmlQualSpeciesRepository;
		this.sbmlSpeciesRepository = sbmlSpeciesRepository;
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public boolean isValidSBML(File file) {
		
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
		
		List<ProvenanceEntity> allModelEntities = new ArrayList<>();
		
		String filename = sbmlfile.getFilename();
		// 1. SBMLDocument
		SBMLDocumentEntity sbmlDocumentEntity = getSBMLDocument(model, filename);
		if (sbmlDocumentEntity == null) {
			// somthing went wrong, we should abort
			logger.info("Could not create SBMLDocumentEntity for file: " + filename + ". Aborting..");
			return null;
		} else {
			// SBMLDocumentEntity is finished
			allModelEntities.add(sbmlDocumentEntity);
		}
		
		// 2. Compartment
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model);
		if (sbmlCompartmentList == null) {
			// something went wrong, we should abort
			logger.info("Could not create SBMLCompartments for file: " + filename + ". Aborting..");
			return null;
		} 
		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLCompartment compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getsBaseId(), compartment);
			allModelEntities.add(compartment);
		}
		
		
		// 3. Units 
		// 4. Parameters
		// 5. InitialAssignments
		// 6. Rules 
		// 7. Constraints
		// 8. Events
		// 9. Species 
		// 10. Reactions
		
		// 11. Model - all of the above need to be linked in the model, before the model can get linked itself
		SBMLModelEntity sbmlModelEntity = getSBMLModelEntity(model, sbmlDocumentEntity);
		
		// 12. Extensions
		// 12.1. Extensions to the Document
			// build the extensions to link them
			// TODO: extract document extensions
		
		// 12.2. Extensions to the Model
		logger.debug("extracting extensions");
		Map<String, SBasePlugin> modelExtensions = model.getExtensionPackages();
		// 12.2.1. Qualitative Models
		
		for (String key : modelExtensions.keySet()) {
			switch (key) {
			case "qual":
				QualModelPlugin qualModelPlugin = (QualModelPlugin) modelExtensions.get(key);
				SBMLQualModelExtension sbmlQualModelExtension = convertQualModelPlugin(qualModelPlugin);
				// now go through contents of extension:
				// 1. QualSpecies
				List<SBMLQualSpecies> sbmlQualSpeciesList = convertQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap);
				// 1.1. ExternalResources through BiomodelsQualifier
				// 2. Transitions
				// 3. other?
				break;
			}
		}
		//List<SBMLModelExtension> sBaseExtensionList = getModelExtensions(model, compartmentLookupMap);
		
		
		
		// Fill list which compartmentalizedEntities are in each compartment
		// set parentModel of all ModelExtensions after Model is finished
		
		// set parentDocument of all DocumentExtensions after document is finsied
		
		return null;
	}
	


	private List<SBMLModelExtension> getModelExtensions(Model model, Map<String, SBMLCompartment> compartmentLookupMap) {
		logger.debug("extracting extensions");
		Map<String, SBasePlugin> modelExtensions = model.getExtensionPackages();
		List<SBMLModelExtension> sbmlModelExtensionList = new ArrayList<>();
		for (String key : modelExtensions.keySet()) {
			switch (key) {
			case "qual":
				QualModelPlugin qualModelPlugin = (QualModelPlugin) modelExtensions.get(key);
				// should I check here, whether an qualModelPlugin to the parentModel already exists?
				SBMLQualModelExtension sbmlSBaseExtensionQual = convertQualModelPlugin(qualModelPlugin);
				//sbmlSBaseExtensionQual.setParentModel(sbmlModelEntity);
				
				List<SBMLQualSpecies> sbmlQualSpeciesList = convertQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap);
				Map<String, SBMLQualSpecies> qualSpeciesLookupMap = new HashMap<>();
				for (SBMLQualSpecies qualSpecies : sbmlQualSpeciesList) {
					qualSpeciesLookupMap.put(qualSpecies.getsBaseId(), qualSpecies);
				}
				List<SBMLQualTransition> sbmlQualTransitionList = convertQualTransition(qualModelPlugin.getListOfTransitions(), qualSpeciesLookupMap);
				logger.debug("Created QualSpecies and Transitions");
				sbmlSBaseExtensionQual.setSbmlQualSpecies(sbmlQualSpeciesList);
				sbmlSBaseExtensionQual.setSbmlQualTransitions(sbmlQualTransitionList);
				//sbmlExtensionList.add(sbmlSBaseExtensionQual);
				break;

			default:
				break;
			}
		}
	
		return null;
	}

	

	private SBMLModelEntity getSBMLModelEntity(Model model, SBMLDocumentEntity sbmlDocument) {
		SBMLModelEntity sbmlModelEntity = new SBMLModelEntity();
		setSbaseProperties(model, sbmlModelEntity);
		setModelProperties(model, sbmlDocument, sbmlModelEntity);
		return sbmlModelEntity;
	}
	
	private SBMLDocumentEntity getSBMLDocument(Model model, String filename) {
		
		try {
			SBMLDocumentEntity existingSBMLDocument = (SBMLDocumentEntity) this.sbmlSBaseEntityRepository.findBysBaseId(model.getSBMLDocument().getId(), 1);
			if (existingSBMLDocument != null) {
				if (existingSBMLDocument.getsBaseName().equals(model.getSBMLDocument().getName())
						&& existingSBMLDocument.getSbmlFileName().equals(filename)) {
					// document with id and name exists
					// comes from the same file (as far as we can tell here)
					return existingSBMLDocument;
				} else {
					// we did find a match, but name and filename did not match. 
					// For now, we do not want to continue
					logger.info("SBMLDocument with sBaseId " + model.getSBMLDocument().getId() + " does exist, but is not the same as we trying to load:");
					logger.info("Existing: Name is " + existingSBMLDocument.getsBaseName() + "; filename is " + existingSBMLDocument.getSbmlFileName());
					logger.info("New doc: Name is " + model.getSBMLDocument().getName() + "; filename is " + filename);
					return null;
				}
			} else {
				// did not find existing.. create new
				SBMLDocumentEntity sbmlDocument = new SBMLDocumentEntity();
				setSbaseProperties(model.getSBMLDocument(), sbmlDocument);
				setSBMLDocumentProperties(model.getSBMLDocument(), sbmlDocument, filename);
				return (SBMLDocumentEntity) this.sbmlSBaseEntityRepository.save(sbmlDocument, 1);
			}
		} catch (ClassCastException e) {	
			e.printStackTrace();
			logger.info("Entity with sBaseId " + model.getSBMLDocument().getId() + " is not of type SBMLDocumentEntity! Aborting...");
			return null; // need to find better error handling way..
		} catch (IncorrectResultSizeDataAccessException e) {
			e.printStackTrace();
			logger.info("Entity with sBaseId " + model.getSBMLDocument().getId() + " was found multiple times");
			return null; // need to find better error handling way..
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception " + e.getMessage() + " while persisting SBMLDocument " + model.getSBMLDocument().getId());
			return null;
		}
		
	}
	
	private List<SBMLCompartment> getCompartmentList(Model model) {
		List<SBMLCompartment> sbmlCompartmentList = new ArrayList<>();
		for(Compartment compartment : model.getListOfCompartments()) {
			// check if a compartment with that name (and those settings does already exist)
			//spatialDimensions, size, constant, sBaseName, sBaseId
			try {
				SBMLCompartment existingCompartment = (SBMLCompartment) this.sbmlSBaseEntityRepository.findBysBaseId(compartment.getId(), 2);
				if (existingCompartment != null 
						&& existingCompartment.getsBaseName().equals(compartment.getName())
						&& existingCompartment.getSize() == compartment.getSize()
						&& existingCompartment.getSpatialDimensions() == compartment.getSpatialDimensions()
						&& existingCompartment.isConstant() == compartment.getConstant() ) {
					// they are obviously the same, so take the one already persisted
					sbmlCompartmentList.add(existingCompartment);
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
					setSbaseProperties(compartment, sbmlCompartment);
					setCompartmentProperties(compartment, sbmlCompartment);
					sbmlCompartmentList.add((SBMLCompartment) this.sbmlSBaseEntityRepository.save(sbmlCompartment));
				}
			} catch (ClassCastException e) {	
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " is not of type SBMLCompartment! Aborting...");
				return null; // need to find better error handling way..
			} catch (IncorrectResultSizeDataAccessException e) {
				e.printStackTrace();
				logger.info("Entity with sBaseId " + compartment.getId() + " was found multiple times");
				return null; // need to find better error handling way..
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Exception " + e.getMessage() + " while persisting SBMLCompartment " + compartment.getId());
				return null;
			}
		}
		return sbmlCompartmentList;
	}
	
	@Deprecated
	public Map<String, Iterable<SBMLSBaseEntity>> extractSBMLEntities(Model model) {
		
		/*
		 * while building all the entities
		 * keep a table to connect the uuid of an entity
		 * with the id from the sbml document by which this entity
		 * is referenced in the model.
		 * That way, once I need to reference an entity and only have the 
		 * sbml-internal id, I can look up the respective uuid,
		 * find that element by uuid in the respective Iterable
		 * and link to it.
		 * Or something like this.
		 */
		
		Map<String, Iterable<SBMLSBaseEntity>> allModelEntities = new HashMap<>();
		
		// sbmlDocument does not make sense. 
		// either we use it to save all declared namespaces
		// which correspond to the extensions used
		// or we skip it.
		// check again.
		SBMLDocumentEntity sbmlDocument = new SBMLDocumentEntity();
		List<SBMLSBaseEntity> sbmlDocumentList = new ArrayList<>();
		
		setSbaseProperties(model.getSBMLDocument(), sbmlDocument);
		sbmlDocument.setSbmlFileName("Not accessible in here");
		// set all sbmlDocument specific attributes here

		sbmlDocumentList.add(sbmlDocument);
		allModelEntities.put("SBMLDocumentEntity", sbmlDocumentList);
		
		/**
		 * Start Building the model here
		 * Note that the final SBMLModel Object will only be finished at the end
		 * when all contained objects have been built and can be linked
		 */
		SBMLModelEntity sbmlModelEntity = new SBMLModelEntity();
		setSbaseProperties(model, sbmlModelEntity);
		setModelProperties(model, sbmlDocument, sbmlModelEntity);
		
		/** 
		 * Compartment(s)
		 */
		List<SBMLSBaseEntity> sbmlCompartmentList = new ArrayList<>();
		
		for(Compartment compartment : model.getListOfCompartments()) {

			SBMLCompartment sbmlCompartment = new SBMLCompartment();
			// check if a compartment with that name (and those settings does already exist)
			//spatialDimensions, size, constant, sBaseName, sBaseId
			try {
				SBMLCompartment existingCompartment = (SBMLCompartment) this.sbmlSBaseEntityRepository.findBysBaseId(compartment.getId(), 2);
				if (existingCompartment != null 
						&& existingCompartment.getsBaseName().equals(compartment.getName())
						&& existingCompartment.getSize() == compartment.getSize()
						&& existingCompartment.getSpatialDimensions() == compartment.getSpatialDimensions()
						&& existingCompartment.isConstant() == compartment.getConstant() ) {
					// they are obviously the same, so take the one already persisted
					sbmlCompartment = existingCompartment;
					sbmlCompartmentList.add(sbmlCompartment);
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
					setSbaseProperties(compartment, sbmlCompartment);
					setCompartmentProperties(compartment, sbmlCompartment);
					sbmlCompartmentList.add(sbmlCompartment);
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

		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLSBaseEntity  compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getsBaseId(), (SBMLCompartment) compartment);
		}
		/**
		 * Extension qual can be checked here and appropriate elements created
		 * so that they can be linked in the model
		 */
		Map<String, SBasePlugin> modelExtensions = model.getExtensionPackages();
		List<SBMLSBaseEntity> sbmlExtensionList = new ArrayList<>();
		for (String key : modelExtensions.keySet()) {
			switch (key) {
			case "qual":
				QualModelPlugin qualModelPlugin = (QualModelPlugin) modelExtensions.get(key);
				SBMLQualModelExtension sbmlSBaseExtensionQual = new SBMLQualModelExtension();
				sbmlSBaseExtensionQual.setEntityUUID(UUID.randomUUID().toString());
				sbmlSBaseExtensionQual.setShortLabel(qualModelPlugin.getPackageName());
				sbmlSBaseExtensionQual.setNamespaceURI(qualModelPlugin.getElementNamespace());
				sbmlSBaseExtensionQual.setRequired(false);
				sbmlSBaseExtensionQual.setParentModel(sbmlModelEntity);
				
				List<SBMLQualSpecies> sbmlQualSpeciesList = convertQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap);
				Map<String, SBMLQualSpecies> qualSpeciesLookupMap = new HashMap<>();
				for (SBMLQualSpecies qualSpecies : sbmlQualSpeciesList) {
					qualSpeciesLookupMap.put(qualSpecies.getsBaseId(), qualSpecies);
				}
				List<SBMLQualTransition> sbmlQualTransitionList = convertQualTransition(qualModelPlugin.getListOfTransitions(), qualSpeciesLookupMap);
				logger.debug("Created QualSpecies and Transitions");
				sbmlSBaseExtensionQual.setSbmlQualSpecies(sbmlQualSpeciesList);
				sbmlSBaseExtensionQual.setSbmlQualTransitions(sbmlQualTransitionList);
				sbmlExtensionList.add(sbmlSBaseExtensionQual);
				break;

			default:
				break;
			}
		}
		allModelEntities.put("SBMLSBaseExtension", sbmlExtensionList);
		
		List<SBMLSBaseEntity> sbmlModelList = new ArrayList<>();
		sbmlModelList.add(sbmlModelEntity);
		allModelEntities.put("SBMLModelEntity", sbmlModelList);

		return allModelEntities;
	}

	@Deprecated
	public Map<String, Iterable<Object>> extractAndConnectExternalResources(
			Map<String, Iterable<SBMLSBaseEntity>> entityMap) {
		List<Object> qualSpeciesWithExternalResources = new ArrayList<>();
		List<Object> externalResources = new ArrayList<>();
		Map<String, ExternalResourceEntity> externalResourceEntityLookupMap = new HashMap<>();
		List<Object> biologicalModifierRelationships = new ArrayList<>();
		List<Object> qualTransitionsWithExternalResources = new ArrayList<>();
		List<Object> modelWithExternalResources = new ArrayList<>();
		List<Object> sbmlDocumentWithExternalResources = new ArrayList<>();
		List<Object> sbmlSBaseExtensionWithExternalResources = new ArrayList<>();
		
		for (String key : entityMap.keySet()) {
			switch (key) {
			case "SBMLSBaseExtension":
				for (SBMLSBaseEntity extension : entityMap.get(key)) {
					SBMLSBaseExtension sBaseExtension = (SBMLSBaseExtension) extension;
					switch (sBaseExtension.getShortLabel()) {
					case "qual":
						SBMLQualModelExtension sbmlQualModelExtension = (SBMLQualModelExtension) sBaseExtension;
						for(SBMLQualSpecies qualSpecies : sbmlQualModelExtension.getSbmlQualSpecies()) {
							SBMLQualSpecies existingQualSpecies = (SBMLQualSpecies) sbmlCompartmentalizedSBaseEntityRepository.findBysBaseId(qualSpecies.getsBaseId(), 2);
							if(existingQualSpecies != null) {
								// for now we do it the old way and only check for this attribute. might add more complex check later
								qualSpecies.setId(existingQualSpecies.getId());
								qualSpecies.setVersion(existingQualSpecies.getVersion());
								qualSpecies.setEntityUUID(existingQualSpecies.getEntityUUID());
							}
							if (qualSpecies.getCvTermList() != null && qualSpecies.getCvTermList().size() > 0) {
								buildAndPersistExternalResourcesForSBaseEntity(qualSpecies); // old additional params were: externalResources,	externalResourceEntityLookupMap, biologicalModifierRelationships,
								
								qualSpeciesWithExternalResources.add(qualSpecies);
							}
						}
						List<SBMLQualTransition> newTransitions = sbmlQualModelExtension.getSbmlQualTransitions();
						for (SBMLQualTransition transition : newTransitions) {
							// for now, we add them back in to see if above works before implementing this for transitions
							// or rather extract it into method to be used here as well.
							qualTransitionsWithExternalResources.add(transition);
						}
						break;

					default:
						break;
					}
					sbmlSBaseExtensionWithExternalResources.add(sBaseExtension); // no resources added so far
				}
				
				break;

			case "SBMLDocumentEntity":
				for (SBMLSBaseEntity documentEntity : entityMap.get(key)) {
					sbmlDocumentWithExternalResources.add(documentEntity); // no resources added so far
				}
				break;
			case "SBMLModelEntity":
				for (SBMLSBaseEntity modelEntity : entityMap.get(key)) {
					modelWithExternalResources.add(modelEntity); // no resources added so far
				}
				break;
			
			default:
				break;
			}
		}
		
		Map<String, Iterable<Object>> allEntitiesAsObjectsWithExternalResources = new HashMap<>();
		allEntitiesAsObjectsWithExternalResources.put("SBMLModelEntity", modelWithExternalResources);
		allEntitiesAsObjectsWithExternalResources.put("SBMLDocumentEntity", sbmlDocumentWithExternalResources);
		allEntitiesAsObjectsWithExternalResources.put("SBMLSBaseExtension", sbmlSBaseExtensionWithExternalResources);
		allEntitiesAsObjectsWithExternalResources.put("ExternalResourceEntity", externalResources);
		allEntitiesAsObjectsWithExternalResources.put("BiologicalQualifier", biologicalModifierRelationships);
		
		return allEntitiesAsObjectsWithExternalResources;
	}

	/**
	 * @param externalResources
	 * @param externalResourceEntityLookupMap
	 * @param biologicalModifierRelationships
	 * @param qualSpecies
	 */
	private void buildAndPersistExternalResourcesForSBaseEntity(SBMLSBaseEntity qualSpecies) {
		boolean existsExternalResourceEntity;
		for (CVTerm cvTerm : qualSpecies.getCvTermList()) {
			//createExternalResources(cvTerm, qualSpecies);
			for (String resource : cvTerm.getResources()) {
				existsExternalResourceEntity = false;
				ExternalResourceEntity newExternalResourceEntity;
				
				ExternalResourceEntity existingExternalResourceEntity = this.externalResourceEntityRepository.findByUri(resource);
				if(existingExternalResourceEntity != null) {
					newExternalResourceEntity = existingExternalResourceEntity;
					existsExternalResourceEntity = true;
				} else {
					ExternalResourceEntity createExternalResourceEntity = new ExternalResourceEntity();
					createExternalResourceEntity.setEntityUUID(UUID.randomUUID().toString());
					createExternalResourceEntity.setUri(resource);
					newExternalResourceEntity = this.externalResourceEntityRepository.save(createExternalResourceEntity);
					existsExternalResourceEntity = false;
				}
				/** TODO: Solve the biomodelsQualifier Dilemma here with new constraint that we persist immediately */
				BiomodelsQualifier newBiomodelsQualifier = new BiomodelsQualifier();
				if (existsExternalResourceEntity) {
					boolean isSetNewBQ = false;
				
					List<BiomodelsQualifier> existingBiomodelsQualifierList = this.biomodelsQualifierRepository.findByTypeAndQualifier(cvTerm.getQualifierType(), cvTerm.getQualifier());
					if (existingBiomodelsQualifierList != null && !existingBiomodelsQualifierList.isEmpty() ) {
						for (BiomodelsQualifier bq : existingBiomodelsQualifierList) {
							if(!isSetNewBQ && bq.getEndNode().getUri().equals(newExternalResourceEntity.getUri())
											&& bq.getStartNode().equals(qualSpecies)) {
								// this is the same biomodelsQualifier that points to the same resource
								newBiomodelsQualifier.setEntityUUID(bq.getEntityUUID());
								newBiomodelsQualifier.setId(bq.getId());
								newBiomodelsQualifier.setVersion(bq.getVersion());
								isSetNewBQ = true;
							}
						}
						if(!isSetNewBQ) {
							
							newBiomodelsQualifier.setEntityUUID(UUID.randomUUID().toString());
							
						}
					} else {
						// The externalResourceEntity does already exist
						// it therefore also has a biomodelsQualifierRelation to it, which is of a different type or qualifier
						// we thus need to and can create it new one
						
						newBiomodelsQualifier.setEntityUUID(UUID.randomUUID().toString());
						
					}
				} else {
					
					newBiomodelsQualifier.setEntityUUID(UUID.randomUUID().toString());
					
				}
				newBiomodelsQualifier.setType(cvTerm.getQualifierType());
				newBiomodelsQualifier.setQualifier(cvTerm.getQualifier());
				newBiomodelsQualifier.setStartNode(qualSpecies);
				newBiomodelsQualifier.setEndNode(newExternalResourceEntity);
				
				newExternalResourceEntity.addRelatedSBMLSBaseEntity(newBiomodelsQualifier);
				// TODO: which of the next 4 do we need?
				//externalResources.add(newExternalResourceEntity);
				//externalResourceEntityLookupMap.put(newExternalResourceEntity.getUri(), newExternalResourceEntity);
				//biologicalModifierRelationships.add(newBiomodelsQualifier);
				//qualSpecies.addExternalResource(newBiomodelsQualifier);
			}
		}
		// TODO: What to return?
	}
	
/******************************************************************************************************************************
 * 													Converter for Extensions
 ******************************************************************************************************************************/
	
/******************************************************************************************************************************
 * 													Converter for Extension QUAL
 ******************************************************************************************************************************/
	
	/**
	 * Convert jSBML QualModelPlugin ({@link org.sbml.jsbml.ext.qual.QualModelPlugin}
	 * to SBMLQualModelExtension with base properties set
	 * Note: The contained QualitativeSpecies and Transitions are not created here.
	 * @param qualModelPlugin The jSBML QualModelPlugin
	 * @return Newly created SBMLQualModelExtension with base properties set specific to this extension
	 */
	private SBMLQualModelExtension convertQualModelPlugin(QualModelPlugin qualModelPlugin) {
		SBMLQualModelExtension sbmlSBaseExtensionQual = new SBMLQualModelExtension();
		sbmlSBaseExtensionQual.setEntityUUID(UUID.randomUUID().toString());
		sbmlSBaseExtensionQual.setShortLabel(qualModelPlugin.getPackageName());
		sbmlSBaseExtensionQual.setNamespaceURI(qualModelPlugin.getElementNamespace());
		sbmlSBaseExtensionQual.setRequired(false);
		return sbmlSBaseExtensionQual;
	}
	
	/**
	 * Convert a ListOf jSBML-Extension-Qual Transition Entities 
	 * to SBMLQualTransition Entities
	 * This function does build the needed SBMLInput and SBMLOutput Entities for the SBMLQualTransition Entities
	 * @param listOfTransitions ListOf ({@link org.sbml.jsbml.ListOf}) of the jSBML-Extension-Qual Transition entities 
	 * {@link org.sbml.jsbml.ext.qual.Transition}
	 * @param qualSpeciesLookupMap A Map to lookup the SBMLQualSpecies referenced by the SBMLInput and SBMLOutput entities
	 * @return List of SBMLQualTransition entities with connected SBMLInput/SBMLOutput entities (which are connected to SBMLQualSpecies entities)
	 */
	private List<SBMLQualTransition> convertQualTransition(ListOf<Transition> listOfTransitions,
			Map<String, SBMLQualSpecies> qualSpeciesLookupMap) {
		List<SBMLQualTransition> sbmlQualTransitionList = new ArrayList<>();
		for (Transition transition : listOfTransitions) {
			SBMLQualTransition loopQualTransition = new SBMLQualTransition();
			setSbaseProperties(transition, loopQualTransition);
			List<SBMLQualInput> loopSBMLQualInputList = convertQualInputList(transition.getListOfInputs(), qualSpeciesLookupMap);
			List<SBMLQualOutput> loopSBMLQualOutputList = convertQualOutputList(transition.getListOfOutputs(), qualSpeciesLookupMap);
			// also do this for function Terms -> need to build them before hand though
			// as we do not seem to have function terms in KEGG-SBML Models, we postpone this
			loopQualTransition.setInputs(loopSBMLQualInputList);
			loopQualTransition.setOutputs(loopSBMLQualOutputList);
			sbmlQualTransitionList.add(loopQualTransition);
			
		}
		return sbmlQualTransitionList;
	}

	/**
	 * Convert a ListOf jSBML-Qual-Extension Output Entities
	 * to SBMLQualOutput entities
	 * @param listOfOutputs ListOf ({@link org.sbml.jsbml.ListOf}) of the jSBML-Extension-Qual Output entities ({@link org.sbml.jsbml.ext.qual.Output})
	 * @param qualSpeciesLookupMap A Map to lookup the SBMLQualSpecies referenced by the SBMLOutput entities
	 * @return List of SBMLQualOutput Entities with connected SBMLQualSpecies entities
	 */
	private List<SBMLQualOutput> convertQualOutputList(ListOf<Output> listOfOutputs,
			Map<String, SBMLQualSpecies> qualSpeciesLookupMap) {
		List<SBMLQualOutput> sbmlQualOutputList = new ArrayList<>();
		for (Output output : listOfOutputs) {
			SBMLQualOutput loopSBMLQualOutput = new SBMLQualOutput();
			setSbaseProperties(output, loopSBMLQualOutput);
			setOutputProperties(output, loopSBMLQualOutput, qualSpeciesLookupMap);
			sbmlQualOutputList.add(loopSBMLQualOutput);
		}
		return sbmlQualOutputList;
	}

	/**
	 * Convert a ListOf jSBML-Qual-Extension Input Entities
	 * to SBMLQualInput entities
	 * @param listOfInputsListOf ListOf ({@link org.sbml.jsbml.ListOf}) of the jSBML-Extension-Qual Input entities ({@link org.sbml.jsbml.ext.qual.Input})
	 * @param qualSpeciesLookupMap A Map to lookup the SBMLQualSpecies referenced by the SBMLInput entities
	 * @return List of SBMLQualInput entities with connected SBMLQualSpecies entities
	 */
	private List<SBMLQualInput> convertQualInputList(ListOf<Input> listOfInputs,
			Map<String, SBMLQualSpecies> qualSpeciesLookupMap) {
		List<SBMLQualInput> sbmlQualInputList = new ArrayList<>();
		for (Input input : listOfInputs) {
			SBMLQualInput loopSBMLQualInput = new SBMLQualInput();
			setSbaseProperties(input, loopSBMLQualInput);
			setInputProperties(input, loopSBMLQualInput, qualSpeciesLookupMap);
			sbmlQualInputList.add(loopSBMLQualInput);
		}
		return sbmlQualInputList;
	}

	/**
	 * Convert a ListOf jSBML-Qual-Extension QualitativeSpecies entities
	 * to SBMLQualSpecies entities
	 * Deprecated: Use buildAndPersistSBMLQualSpecies instead
	 * @param listOfQualitativeSpecies ListOf ({@link org.sbml.jsbml.ListOf}) of the jSBML-Extension-Qual QualitativeSpecies entities ({@link org.sbml.jsbml.ext.qual.QualitativeSpecies})
	 * @param compartmentLookupMap A Map to lookup the correct SBMLCompartment which this CompartmentalizedSBase (from which QualitativeSpecies is derived) is located in
	 * @return List of SBMLQualSpecies entities with connected compartment entities
	 */
	@Deprecated
	private List<SBMLQualSpecies> convertQualSpecies(ListOf<QualitativeSpecies> listOfQualitativeSpecies, Map<String, SBMLCompartment> compartmentLookupMap) {
		List<SBMLQualSpecies> sbmlQualSpeciesList = new ArrayList<>();
		for (QualitativeSpecies qualSpecies : listOfQualitativeSpecies) {
			// check if qualSpecies already exists
			SBMLQualSpecies loopQualSpecies = new SBMLQualSpecies();
			setSbaseProperties(qualSpecies, loopQualSpecies);
			setCompartmentalizedSbaseProperties(qualSpecies, loopQualSpecies, compartmentLookupMap);
			setQualSpeciesProperties(qualSpecies, loopQualSpecies);
			sbmlQualSpeciesList.add(loopQualSpecies);
		}
		return sbmlQualSpeciesList;
	}
	
	/**
	 * This function builds SBMLQualSpecies entities from a ListOf ({@link org.sbml.jsbml.ListOf}) jSBML-Extension-Qual QualitativeSpecies ({@link org.sbml.jsbml.ext.qual.QualitativeSpecies})
	 * @param qualSpeciesListOf The ListOf holding the jSBML-Extension-Qual QualitativeSpecies
	 * @param compartmentLookupMap A Map to lookup the correct SBMLCompartment which this CompartmentalizedSBase (from which QualitativeSpecies is derived) is located in
	 * @return HelperQualSpeciesReturn entity holding all QualitativeSpecies created as well as a map for matching converted ids to their new ids, for resolving references in transitions
	 */
	private HelperQualSpeciesReturn buildAndPersistSBMLQualSpecies(ListOf<QualitativeSpecies> qualSpeciesListOf, Map<String, SBMLCompartment> compartmentLookupMap) {
		HelperQualSpeciesReturn helperQualSpeciesReturn = new HelperQualSpeciesReturn();
		Map<String, SBMLQualSpecies> qualSpeciesMap = new HashMap<>();
		List<QualitativeSpecies> groupQualSpeciesList = new ArrayList<>();
		for (QualitativeSpecies qualSpecies : qualSpeciesListOf) {
			if(qualSpecies.getName().equals("Group")) {
				logger.debug("found qual Species group");
				groupQualSpeciesList.add(qualSpecies);// need to do them after all species have been persisted
			} else {
				SBMLQualSpecies existingSpecies = this.sbmlQualSpeciesRepository.findBysBaseName(qualSpecies.getName());
				if (existingSpecies != null) {
					if(!qualSpeciesMap.containsKey(existingSpecies.getsBaseId())) {
						qualSpeciesMap.put(existingSpecies.getsBaseId(), existingSpecies);
					}	
					if(!qualSpecies.getId().substring(5).equals(qualSpecies.getName())) {
						// possibly this is the case when we have an entity duplicated in sbml, but deduplicate it here
						// transitions might reference this duplicate entity and need to be pointed to the original one
						helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), existingSpecies.getsBaseId());
					}
					
				} else {
					SBMLQualSpecies newQualSpecies = new SBMLQualSpecies();
					setSbaseProperties(qualSpecies, newQualSpecies);
					//setCompartmentalizedSbaseProperties(qualSpecies, newQualSpecies, compartmentLookupMap);
					setQualSpeciesProperties(qualSpecies, newQualSpecies);
					newQualSpecies.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBysBaseName(qualSpecies.getName()));
					SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newQualSpecies);
					qualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
				}
			}
		}
		// now build the group nodes
		for (QualitativeSpecies qualSpecies : groupQualSpeciesList) {
			if (qualSpecies.getId().equals("qual_Group_4")) {
				logger.debug("Group 4");
			}
			SBMLQualSpeciesGroup newSBMLQualSpeciesGroup = new SBMLQualSpeciesGroup();
			List<String> groupMemberSymbols = new ArrayList<>();
			XMLNode ulNode = qualSpecies.getNotes().getChildElement("body", "http://www.w3.org/1999/xhtml").getChildElement("p", null).getChildElement("ul", null);
			for (int i = 0; i != ulNode.getChildCount(); i++) {
				if(ulNode.getChild(i).getChildCount() > 0) {
					for (int j = 0; j != ulNode.getChild(i).getChildCount(); j++) {
						String chars = ulNode.getChild(i).getChild(j).getCharacters();
						logger.info(chars);
						groupMemberSymbols.add(chars);
					}
				}
			}
			setSbaseProperties(qualSpecies, newSBMLQualSpeciesGroup);
			//setCompartmentalizedSbaseProperties(qualSpecies, newSBMLQualSpeciesGroup, compartmentLookupMap);
			setQualSpeciesProperties(qualSpecies, newSBMLQualSpeciesGroup);
			String qualSpeciesSbaseName = newSBMLQualSpeciesGroup.getsBaseName();
			for (String symbol : groupMemberSymbols) {
				SBMLQualSpecies existingQualSpecies = this.sbmlQualSpeciesRepository.findBysBaseName(symbol);
				newSBMLQualSpeciesGroup.addQualSpeciesToGroup(existingQualSpecies);
				qualSpeciesSbaseName += "_";
				qualSpeciesSbaseName += symbol;
			}
			SBMLQualSpeciesGroup existingSBMLQualSpeciesGroup = (SBMLQualSpeciesGroup) sbmlQualSpeciesRepository.findBysBaseName(qualSpeciesSbaseName);
			if (existingSBMLQualSpeciesGroup != null) {
				if (!qualSpeciesMap.containsKey(existingSBMLQualSpeciesGroup.getsBaseId())) {
					qualSpeciesMap.put(existingSBMLQualSpeciesGroup.getsBaseId(), existingSBMLQualSpeciesGroup);
				}
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
			} else {
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseName(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseId(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseMetaId("meta_" + qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBysBaseName(qualSpeciesSbaseName));
				SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup);
				qualSpeciesMap.put(persistedNewQualSpecies.getsBaseId(), persistedNewQualSpecies);
			}
		}
		helperQualSpeciesReturn.setSpeciesMap(qualSpeciesMap);
		if(helperQualSpeciesReturn.getsBaseIdMap() == null) {
			Map<String, String> emptySBaseIdMap = new HashMap<>();
			helperQualSpeciesReturn.setsBaseIdMap(emptySBaseIdMap);
		}
		return helperQualSpeciesReturn;
	}
	
	
/******************************************************************************************************************************
 * 													Setter for SBML Core
 ******************************************************************************************************************************/
			
	/**
	 * Set Properties of SBMLCompartment entity (or any entity derived from it)
	 * from a jSBML Compartment entity
	 * @param source The Compartment entity from jSBML ({@link org.sbml.jsbml.Compartment}) from which the attributes are to be taken
	 * @param target The SBMLComartment entity to which the attributes are to be applied to
	 */
	private void setCompartmentProperties(Compartment source, SBMLCompartment target) {
		target.setSpatialDimensions(source.getSpatialDimensions());
		target.setSize(source.getSize());
		target.setConstant(source.getConstant());
		target.setUnits(source.getUnits()); // at some point we want to link to a unit definiton here
	}

	/**
	 * Set Properties of SBMLDocument entity (or any entity derived from it)
	 * from a jSBML SBMLDocument entity
	 * @param source The SBMLDocument entity from jSBML ({@link org.sbml.jsbml.SBMLDocument}) from which the attributes are to be taken
	 * @param target The SBMLDocumentEntity entity to which the attributes are to be applied to
	 * @param filename The original filename where the document was taken from
	 */
	private void setSBMLDocumentProperties(SBMLDocument source, SBMLDocumentEntity target,
			String filename) {
		target.setSbmlFileName(filename);
		target.setSbmlLevel(source.getLevel());
		target.setSbmlVersion(source.getVersion());
		target.setSbmlXmlNamespace(source.getNamespace());
	}

	/**
	 * Set Properties of SBMLModelEntity (or any entity derived from it)
	 * from a jSBML Model Entity
	 * @param model The Model entity from jSBML ({@link org.sbml.jsbml.Model}) from which the attributes are to be taken
	 * @param sbmlDocument The SBMLDocumentEntity entity which encloses this SBMLModel, it will be referenced in {@linkplain sbmlModelEntity}
	 * @param sbmlModelEntity The SBMLModelEntity to which the attributes are to be applied to
	 */
	private void setModelProperties(Model model, SBMLDocumentEntity sbmlDocument, SBMLModelEntity sbmlModelEntity) {
		/**
		 * The following string attributes contain IDRefs to 
		 * UnitDefinitions.
		 * TODO: actually link to the UnitDefinition-Object here.
		 * Since current queries don't use them, we skip them for now
		 * and only save the ids to the graph
		 * Once we load the UnitDefinitions
		 * a) manual linking is possible
		 * b) linking here on creation or on a second pass becomes possible.
		 */
		sbmlModelEntity.setSubstanceUnits(model.getSubstanceUnits());
		sbmlModelEntity.setTimeUnits(model.getTimeUnits());
		sbmlModelEntity.setVolumeUnits(model.getVolumeUnits());
		sbmlModelEntity.setAreaUnits(model.getAreaUnits());
		sbmlModelEntity.setLengthUnits(model.getLengthUnits());
		sbmlModelEntity.setConversionFactor(model.getConversionFactor());
		sbmlModelEntity.setEnclosingSBMLEntity(sbmlDocument);
	}
	
	/**
	 * Set Properties of SBMLSBaseEntity (or any entity derived from it)
	 * from a jSBML SBase entity
	 * @param source The SBase entity from jSBML ({@link org.sbml.jsbml.SBase}) from which the attributes are to be taken
	 * @param target The SBMLSBaseEntity entity to which the attributes are to be applied to
	 */
	private void setSbaseProperties(SBase source, SBMLSBaseEntity target) {
		target.setEntityUUID(UUID.randomUUID().toString());
		target.setsBaseId(source.getId());
		target.setsBaseName(source.getName());
		try {
			target.setsBaseNotes(source.getNotesString());
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.debug("Unable to generate Notes String for Entity " + source.getName());
		}
		target.setsBaseMetaId(source.getMetaId());
		target.setsBaseSboTerm(source.getSBOTermID());
		if(source.getNumCVTerms() > 0) {
			target.setCvTermList(source.getCVTerms());
		}
	}
	
	/**
	 * Set Properties of SBMLCompartmentalizedSBaseEntity entity (or any entity derived from it)
	 * from a jSBML CompartmentalizedSBase entity
	 * @param source The CompartmentalizedSBase entity from jSBML ({@link org.sbml.jsbml.CompartmentalizedSBase}) from which the attributes are to be taken
	 * @param target The SBMLCompartmentalizedSBaseEntity entity to which the attributes are to be applied to
	 * @param compartmentLookupMap A Map to lookup the correct SBMLCompartment which this CompartmentalizedSBase is located in
	 */
	private void setCompartmentalizedSbaseProperties(CompartmentalizedSBase source, SBMLCompartmentalizedSBaseEntity target, Map<String, SBMLCompartment> compartmentLookupMap) {
		target.setCompartmentMandatory(source.isCompartmentMandatory());
		if(compartmentLookupMap.containsKey(source.getCompartment())) {
			target.setCompartment(compartmentLookupMap.get(source.getCompartment()));
		} else {
			logger.debug("No matching compartment found for compartment " + source.getCompartment() + " in " + source.getName());
		}
	}
	
	/**
	 * Set Properties of SBMLSpecies entity (or any entity derived from it)
	 * from a jSBML Species entity
	 * @param source The Species Entity from jSBML ({@link org.sbml.jsbml.Species)} from which the attributes are to be taken
	 * @param target The SBMLSpecies Entity to which the attributes are to be applied
	 */
	private void setSpeciesProperties(Species source, SBMLSpecies target) {
		target.setInitialAmount(source.getInitialAmount());
		target.setInitialConcentration(source.getInitialConcentration());
		target.setBoundaryCondition(source.getBoundaryCondition());
		target.setHasOnlySubstanceUnits(source.hasOnlySubstanceUnits());
		target.setConstant(source.isConstant());	
	}

/******************************************************************************************************************************
 * 													Setter for Extensions
 ******************************************************************************************************************************/
	
/******************************************************************************************************************************
 * 													Setter for Extension QUAL
 ******************************************************************************************************************************/
		
	/**
	 * Set Properties of SBMLQualSpecies entity (or any entity derived from it)
	 * from a jSBML-Qual-Extension QualitativeSpecies entity
	 * @param source The QualitativeSpecies entity from jSBML-Qual-Extension ({@link org.sbml.sbml.ext.qual.QualitativeSpecies}) from which the attributes are to be taken
	 * @param target The SBMLQualSpecies entity to which the attributes are to be applied to
	 */
	private void setQualSpeciesProperties(QualitativeSpecies source, SBMLQualSpecies target) {
		target.setConstant(source.getConstant());
		/** 
		 * initialLevel and maxLevel are not always set, so the jsbml entity has methods for checking if they are
		 * Consider adding those to our Entities as well.
		 */
		
		if (source.isSetInitialLevel()) target.setInitialLevel(source.getInitialLevel());
		if (source.isSetMaxLevel()) target.setMaxLevel(source.getMaxLevel());
	}

	/**
	 * Set Properties of SBMLQualInput entity (or any entity derived from it)
	 * from a jSBML-Qual-Extension Input entity
	 * @param source The Input entity from jSBML-Qual-Extension ({@link org.sbml.ext.qual.Input}) from which the attributes are to be taken
	 * @param target The SBMLInput entity to which the attributes are to be applied to
	 * @param qualSpeciesLookupMap A Map to lookup the SBMLQualSpecies entity this Input entity refers to
	 */
	private void setInputProperties(Input source, SBMLQualInput target,
			Map<String, SBMLQualSpecies> qualSpeciesLookupMap) {
		if (source.isSetSign()) target.setSign(source.getSign());
		if (source.isSetTransitionEffect()) target.setTransitionEffect(source.getTransitionEffect());
		if (source.isSetThresholdLevel()) target.setThresholdLevel(source.getThresholdLevel());
		if (qualSpeciesLookupMap.containsKey(source.getQualitativeSpecies())) {
			target.setQualSpecies(qualSpeciesLookupMap.get(source.getQualitativeSpecies()));
		} else {
			logger.debug("WARING: Input QualSpecies " + source.getQualitativeSpecies() + " not found for " + source.getName());
		}
	}
	
	/**
	 * Set Properties of SBMLQualOutput entity (or any entity derived from it)
	 * from a jSBML-Qual-Extension Output entity
	 * @param source The Output entity from jSBML-Qual-Extension ({@link org.sbml.ext.qual.Output}) from which the attributes are to be taken
	 * @param target The SBMLOutput entity to which the attributes are to be applied to
	 * @param qualSpeciesLookupMap A Map to lookup the SBMLQualSpecies entity this Output entity refers to
	 */
	private void setOutputProperties(Output source, SBMLQualOutput target,
			Map<String, SBMLQualSpecies> qualSpeciesLookupMap) {
		if (source.isSetTransitionEffect()) target.setTransitionEffect(source.getTransitionEffect());
		if (source.isSetOutputLevel()) target.setOutputLevel(source.getOutputLevel());
		if (qualSpeciesLookupMap.containsKey(source.getQualitativeSpecies())) {
			target.setQualSpecies(qualSpeciesLookupMap.get(source.getQualitativeSpecies()));
		} else {
			logger.debug("WARING: Output QualSpecies " + source.getQualitativeSpecies() + " not found for " + source.getName());
		}
	}

}
