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
import org.tts.model.BiomodelsQualifier;
import org.tts.model.ExternalResourceEntity;
import org.tts.model.GraphBaseEntity;
import org.tts.model.HelperQualSpeciesReturn;
import org.tts.model.SBMLCompartment;
import org.tts.model.SBMLCompartmentalizedSBaseEntity;
import org.tts.model.SBMLDocumentEntity;
import org.tts.model.SBMLModelEntity;
import org.tts.model.SBMLModelExtension;
import org.tts.model.SBMLQualInput;
import org.tts.model.SBMLQualModelExtension;
import org.tts.model.SBMLQualOutput;
import org.tts.model.SBMLQualSpecies;
import org.tts.model.SBMLQualSpeciesGroup;
import org.tts.model.SBMLQualTransition;
import org.tts.model.SBMLSBaseEntity;
import org.tts.model.SBMLSBaseExtension;
import org.tts.model.SBMLSimpleTransition;
import org.tts.model.SBMLSpecies;
import org.tts.model.SBMLSpeciesGroup;
import org.tts.repository.BiomodelsQualifierRepository;
import org.tts.repository.ExternalResourceEntityRepository;
import org.tts.repository.GraphBaseEntityRepository;
import org.tts.repository.SBMLCompartmentalizedSBaseEntityRepository;
import org.tts.repository.SBMLQualSpeciesRepository;
import org.tts.repository.SBMLSBaseEntityRepository;
import org.tts.repository.SBMLSimpleTransitionRepository;

@Service
public class SBMLServiceImpl implements SBMLService {

	GraphBaseEntityRepository graphBaseRepository;
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	SBMLCompartmentalizedSBaseEntityRepository sbmlCompartmentalizedSBaseEntityRepository;
	ExternalResourceEntityRepository externalResourceEntityRepository;
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	SBMLQualSpeciesRepository sbmlQualSpeciesRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	SBMLSpeciesRepository sbmlSpeciesRepository;
	
	@Autowired
	public SBMLServiceImpl(	GraphBaseEntityRepository graphBaseRepository, 
							SBMLSBaseEntityRepository sbmlSBaseEntityRepository,
							ExternalResourceEntityRepository externalResourceEntityRepository,
							BiomodelsQualifierRepository biomodelsQualifierRepository,
							SBMLCompartmentalizedSBaseEntityRepository sbmlCompartmentalizedSBaseEntityRepository,
							SBMLQualSpeciesRepository sbmlQualSpeciesRepository,
							SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository,
							SBMLSpeciesRepository sbmlSpeciesRepository) {
		super();
		this.graphBaseRepository = graphBaseRepository;
		this.sbmlSBaseEntityRepository = sbmlSBaseEntityRepository;
		this.externalResourceEntityRepository = externalResourceEntityRepository;
		this.biomodelsQualifierRepository = biomodelsQualifierRepository;
		this.sbmlCompartmentalizedSBaseEntityRepository = sbmlCompartmentalizedSBaseEntityRepository;
		this.sbmlQualSpeciesRepository = sbmlQualSpeciesRepository;
		this.sbmlSimpleTransitionRepository = sbmlSimpleTransitionRepository;
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
	public List<GraphBaseEntity> getAllEntities(){
		return (List<GraphBaseEntity>) this.graphBaseRepository.findAll();
	}
	
	@Override
	public boolean clearDatabase() {
		this.graphBaseRepository.deleteAll();
		if (((List<GraphBaseEntity>) this.graphBaseRepository.findAll()).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	private List<SBMLSpecies> buildAndPersistSBMLSpecies(ListOf<Species> speciesListOf, Map<String, SBMLCompartment> compartmentLookupMap) {
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
				} else {
					SBMLSpecies newSpecies = new SBMLSpecies();
					setSbaseProperties(species, newSpecies);
					//setCompartmentalizedSbaseProperties(species, newSpecies, compartmentLookupMap);
					setSpeciesProperties(species, newSpecies);
					SBMLSpecies persistedNewSpecies = this.sbmlSpeciesRepository.save(newSpecies);
					speciesList.add(persistedNewSpecies);
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
						logger.info(chars);
						groupMemberSymbols.add(chars);
					}
				}
			}
			setSbaseProperties(species, newSBMLSpeciesGroup);
			//setCompartmentalizedSbaseProperties(species, newSBMLSpeciesGroup, compartmentLookupMap);
			setSpeciesProperties(species, newSBMLSpeciesGroup);
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
			} else {
				newSBMLSpeciesGroup.setsBaseName(speciesSbaseName);
				newSBMLSpeciesGroup.setSBaseId(speciesSbaseName);
				newSBMLSpeciesGroup.setsBaseMetaId("meta_" + speciesSbaseName);
				SBMLSpecies persistedNewSpeciesGroup = this.sbmlSpeciesRepository.save(newSBMLSpeciesGroup);
				speciesList.add(persistedNewSpeciesGroup);
			}
		}
		return speciesList;
	}
	
	private HelperQualSpeciesReturn buildAndPersistSBMLQualSpecies(ListOf<QualitativeSpecies> qualSpeciesListOf, Map<String, SBMLCompartment> compartmentLookupMap) {
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
					if(!qualSpeciesMap.containsKey(existingSpecies.getSBaseId())) {
						qualSpeciesMap.put(existingSpecies.getSBaseId(), existingSpecies);
					}	
					if(!qualSpecies.getId().substring(5).equals(qualSpecies.getName())) {
						// possibly this is the case when we have an entity duplicated in sbml, but deduplicate it here
						// transitions might reference this duplicate entity and need to be pointed to the original one
						helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), existingSpecies.getSBaseId());
					}
					
				} else {
					SBMLQualSpecies newQualSpecies = new SBMLQualSpecies();
					setSbaseProperties(qualSpecies, newQualSpecies);
					//setCompartmentalizedSbaseProperties(qualSpecies, newQualSpecies, compartmentLookupMap);
					setQualSpeciesProperties(qualSpecies, newQualSpecies);
					newQualSpecies.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpecies.getName()));
					SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newQualSpecies);
					qualSpeciesMap.put(persistedNewQualSpecies.getSBaseId(), persistedNewQualSpecies);
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
				SBMLQualSpecies existingQualSpecies = this.sbmlQualSpeciesRepository.findBySBaseName(symbol);
				newSBMLQualSpeciesGroup.addQualSpeciesToGroup(existingQualSpecies);
				qualSpeciesSbaseName += "_";
				qualSpeciesSbaseName += symbol;
			}
			SBMLQualSpeciesGroup existingSBMLQualSpeciesGroup = (SBMLQualSpeciesGroup) sbmlQualSpeciesRepository.findBySBaseName(qualSpeciesSbaseName);
			if (existingSBMLQualSpeciesGroup != null) {
				if (!qualSpeciesMap.containsKey(existingSBMLQualSpeciesGroup.getSBaseId())) {
					qualSpeciesMap.put(existingSBMLQualSpeciesGroup.getSBaseId(), existingSBMLQualSpeciesGroup);
				}
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
			} else {
				helperQualSpeciesReturn.addsBasePair(qualSpecies.getId(), qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseName(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setSBaseId(qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setsBaseMetaId("meta_" + qualSpeciesSbaseName);
				newSBMLQualSpeciesGroup.setCorrespondingSpecies(this.sbmlSpeciesRepository.findBySBaseName(qualSpeciesSbaseName));
				SBMLQualSpecies persistedNewQualSpecies = this.sbmlQualSpeciesRepository.save(newSBMLQualSpeciesGroup);
				qualSpeciesMap.put(persistedNewQualSpecies.getSBaseId(), persistedNewQualSpecies);
			}
		}
		helperQualSpeciesReturn.setSpeciesMap(qualSpeciesMap);
		if(helperQualSpeciesReturn.getsBaseIdMap() == null) {
			Map<String, String> emptySBaseIdMap = new HashMap<>();
			helperQualSpeciesReturn.setsBaseIdMap(emptySBaseIdMap);
		}
		return helperQualSpeciesReturn;
	}
	
	@Override
	public List<GraphBaseEntity> persistFastSimple(Model model) {
		// compartment
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model);
		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLCompartment compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getSBaseId(), compartment);
		}
		List<SBMLSpecies> persistedSBMLSpecies = buildAndPersistSBMLSpecies(model.getListOfSpecies(), compartmentLookupMap);

		QualModelPlugin qualModelPlugin = (QualModelPlugin) model.getExtension("qual");
		HelperQualSpeciesReturn qualSpeciesHelper = buildAndPersistSBMLQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap);
		
		Map<String, SBMLQualSpecies> persistedQualSpeciesList = qualSpeciesHelper.getSpeciesMap();
		Map<String, String> qualSBaseLookupMap = qualSpeciesHelper.getsBaseIdMap();
		List<SBMLSimpleTransition> persistedTransitionList	= buildAndPersistTransitions(qualSBaseLookupMap, qualModelPlugin.getListOfTransitions());
		
		List<GraphBaseEntity> returnList= new ArrayList<>();
		persistedSBMLSpecies.forEach(species->{
			returnList.add(species);
		});
		persistedQualSpeciesList.forEach((k, qualSpecies)-> {
			returnList.add(qualSpecies);
		});
		persistedTransitionList.forEach(transition-> {
			returnList.add(transition);
		});
		return returnList;
	}

	private List<SBMLSimpleTransition> buildAndPersistTransitions(Map<String, String> qualSBaseLookupMap,
			 ListOf<Transition> transitionListOf) {
		List<SBMLSimpleTransition> transitionList = new ArrayList<>();
		for (Transition transition : transitionListOf) {
			SBMLQualSpecies tmp = null;
			String newTransitionId = "";
			if(qualSBaseLookupMap.containsKey(transition.getListOfInputs().get(0).getQualitativeSpecies())) {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(qualSBaseLookupMap.get(transition.getListOfInputs().get(0).getQualitativeSpecies()));
			} else {
				tmp = this.sbmlQualSpeciesRepository.findBySBaseId(transition.getListOfInputs().get(0).getQualitativeSpecies());
			}
			if(tmp == null) {
				logger.debug("What is going on?");
			}
			newTransitionId += tmp.getsBaseName();
			newTransitionId += "-";
			newTransitionId += transition.getSBOTermID();
			newTransitionId += "-";
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
				}
			} else {
				SBMLSimpleTransition newSimpleTransition = new SBMLSimpleTransition();
				setSbaseProperties(transition, newSimpleTransition);
				newSimpleTransition.setTransitionId(newTransitionId);
				newSimpleTransition.setInputSpecies(this.sbmlQualSpeciesRepository.findBySBaseId(inputName));
				newSimpleTransition.setOutputSpecies(this.sbmlQualSpeciesRepository.findBySBaseId(outputName));
				SBMLSimpleTransition persistedNewSimpleTransition = this.sbmlSimpleTransitionRepository.save(newSimpleTransition);
				transitionList.add(persistedNewSimpleTransition);
			}
		}
		return transitionList;
	}
	
	
	private void setSpeciesProperties(Species source, SBMLSpecies target) {
		target.setInitialAmount(source.getInitialAmount());
		target.setInitialConcentration(source.getInitialConcentration());
		target.setBoundaryCondition(source.getBoundaryCondition());
		target.setHasOnlySubstanceUnits(source.hasOnlySubstanceUnits());
		target.setConstant(source.isConstant());	
	}

	@Override
	public List<GraphBaseEntity> buildAndPersist(Model model, String filename) {
		
		
		SBMLDocumentEntity sbmlDocument = getSBMLDocument(model, filename);
		// first Units, Parameters, initialAssignments, rules, constraints, events
		// then Species and reactions
		
		// they all need to be linked in the model, before the model can get linked itself
		SBMLModelEntity sbmlModelEntity = getSBMLModelEntity(model, sbmlDocument);
		List<SBMLCompartment> sbmlCompartmentList = getCompartmentList(model);
		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLCompartment compartment : sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getSBaseId(), compartment);
		}
		
		// build the extensions to link them
		// TODO: extract document extensions
		
		// extract model Extensions
		List<SBMLModelExtension> sBaseExtensionList = getModelExtensions(model, compartmentLookupMap);
		
		
		
		// Fill list which compartmentalizedEntities are in each compartment
		// set parentModel of all ModelExtensions after Model is finished
		// set model property of SBMLDocumentEntity after model is finished
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
				SBMLQualModelExtension sbmlSBaseExtensionQual = new SBMLQualModelExtension();
				sbmlSBaseExtensionQual.setEntityUUID(UUID.randomUUID().toString());
				sbmlSBaseExtensionQual.setShortLabel(qualModelPlugin.getPackageName());
				sbmlSBaseExtensionQual.setNamespaceURI(qualModelPlugin.getElementNamespace());
				sbmlSBaseExtensionQual.setRequired(false);
				//sbmlSBaseExtensionQual.setParentModel(sbmlModelEntity);
				
				List<SBMLQualSpecies> sbmlQualSpeciesList = convertQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap);
				Map<String, SBMLQualSpecies> qualSpeciesLookupMap = new HashMap<>();
				for (SBMLQualSpecies qualSpecies : sbmlQualSpeciesList) {
					qualSpeciesLookupMap.put(qualSpecies.getSBaseId(), qualSpecies);
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
		SBMLDocumentEntity sbmlDocument = new SBMLDocumentEntity();
		setSbaseProperties(model.getSBMLDocument(), sbmlDocument);
		setSBMLDocumentProperties(model.getSBMLDocument(), sbmlDocument, filename);
		return sbmlDocument;
	}
	
	private List<SBMLCompartment> getCompartmentList(Model model) {
		List<SBMLCompartment> sbmlCompartmentList = new ArrayList<>();
		for(Compartment compartment : model.getListOfCompartments()) {
			SBMLCompartment sbmlCompartment = new SBMLCompartment();
			// check if a compartment with that name (and those settings does already exist)
			//spatialDimensions, size, constant, sBaseName, sBaseId
			try {
				SBMLCompartment existingCompartment = (SBMLCompartment) this.sbmlSBaseEntityRepository.findBySBaseId(compartment.getId(), 2);
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
		return sbmlCompartmentList;
	}
	
	@Deprecated
	@Override
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
				SBMLCompartment existingCompartment = (SBMLCompartment) this.sbmlSBaseEntityRepository.findBySBaseId(compartment.getId(), 2);
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
			compartmentLookupMap.put(compartment.getSBaseId(), (SBMLCompartment) compartment);
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
					qualSpeciesLookupMap.put(qualSpecies.getSBaseId(), qualSpecies);
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
	@Override
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
							SBMLQualSpecies existingQualSpecies = (SBMLQualSpecies) sbmlCompartmentalizedSBaseEntityRepository.findBySBaseId(qualSpecies.getSBaseId(), 2);
							if(existingQualSpecies != null) {
								// for now we do it the old way and only check for this attribute. might add more complex check later
								qualSpecies.setId(existingQualSpecies.getId());
								qualSpecies.setVersion(existingQualSpecies.getVersion());
								qualSpecies.setEntityUUID(existingQualSpecies.getEntityUUID());
							}
							if (qualSpecies.getCvTermList() != null && qualSpecies.getCvTermList().size() > 0) {
								for (CVTerm cvTerm : qualSpecies.getCvTermList()) {
									//createExternalResources(cvTerm, qualSpecies);
									for (String resource : cvTerm.getResources()) {
										boolean existsExternalResourceEntity = false;
										ExternalResourceEntity newExternalResourceEntity;
										ExternalResourceEntity existingExternalResourceEntity = externalResourceEntityLookupMap.get(resource);
										if(existingExternalResourceEntity != null) {
											newExternalResourceEntity = existingExternalResourceEntity;
											existsExternalResourceEntity = true;
										} else {	
											existingExternalResourceEntity = this.externalResourceEntityRepository.findByUri(resource);
											if(existingExternalResourceEntity != null) {
												newExternalResourceEntity = existingExternalResourceEntity;
												existsExternalResourceEntity = true;
											} else {
												newExternalResourceEntity = new ExternalResourceEntity();
												newExternalResourceEntity.setEntityUUID(UUID.randomUUID().toString());
												newExternalResourceEntity.setUri(resource);
											}
										}
										BiomodelsQualifier newBiomodelsQualifier = new BiomodelsQualifier();;
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
										externalResources.add(newExternalResourceEntity);
										externalResourceEntityLookupMap.put(newExternalResourceEntity.getUri(), newExternalResourceEntity);
										biologicalModifierRelationships.add(newBiomodelsQualifier);
										//qualSpecies.addExternalResource(newBiomodelsQualifier);
									}
								}
								
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

	private void setCompartmentProperties(Compartment source, SBMLCompartment target) {
		target.setSpatialDimensions(source.getSpatialDimensions());
		target.setSize(source.getSize());
		target.setConstant(source.getConstant());
		target.setUnits(source.getUnits()); // at some point we want to link to a unit definiton here
	}

	private List<SBMLQualSpecies> convertQualSpecies(ListOf<QualitativeSpecies> listOfQualitativeSpecies, Map<String, SBMLCompartment> compartmentLookupMap) {
		List<SBMLQualSpecies> sbmlQualSpeciesList = new ArrayList<>();
		for (QualitativeSpecies qualSpecies : listOfQualitativeSpecies) {
			SBMLQualSpecies loopQualSpecies = new SBMLQualSpecies();
			setSbaseProperties(qualSpecies, loopQualSpecies);
			setCompartmentalizedSbaseProperties(qualSpecies, loopQualSpecies, compartmentLookupMap);
			setQualSpeciesProperties(qualSpecies, loopQualSpecies);
			sbmlQualSpeciesList.add(loopQualSpecies);
		}
		return sbmlQualSpeciesList;
	}



	private void setCompartmentalizedSbaseProperties(CompartmentalizedSBase source, SBMLCompartmentalizedSBaseEntity target, Map<String, SBMLCompartment> compartmentLookupMap) {
		target.setCompartmentMandatory(source.isCompartmentMandatory());
		if(compartmentLookupMap.containsKey(source.getCompartment())) {
			target.setCompartment(compartmentLookupMap.get(source.getCompartment()));
		} else {
			logger.debug("No matching compartment found for compartment " + source.getCompartment() + " in " + source.getName());
		}
	}

	private void setQualSpeciesProperties(QualitativeSpecies source, SBMLQualSpecies target) {
		target.setConstant(source.getConstant());
		/** 
		 * initialLevel and maxLevel are not always set, so the jsbml entity has methods for checking if they are
		 * Consider adding those to our Entities as well.
		 */
		
		if (source.isSetInitialLevel()) target.setInitialLevel(source.getInitialLevel());
		if (source.isSetMaxLevel()) target.setMaxLevel(source.getMaxLevel());
	}

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
	
	private void setSbaseProperties(SBase source, SBMLSBaseEntity target) {
		target.setEntityUUID(UUID.randomUUID().toString());
		target.setSBaseId(source.getId());
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

	private void setSBMLDocumentProperties(SBMLDocument source, SBMLDocumentEntity target,
			String filename) {
		target.setSbmlFileName(filename);
		target.setSbmlLevel(source.getLevel());
		target.setSbmlVersion(source.getVersion());
		target.setSbmlXmlNamespace(source.getNamespace());
	}

	

}
