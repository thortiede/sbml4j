package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentalizedSBase;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;
import org.sbml.jsbml.ext.qual.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.SBMLCompartment;
import org.tts.model.SBMLCompartmentalizedSBaseEntity;
import org.tts.model.SBMLDocumentEntity;
import org.tts.model.SBMLModelEntity;
import org.tts.model.SBMLQualInput;
import org.tts.model.SBMLQualOutput;
import org.tts.model.SBMLQualSpecies;
import org.tts.model.SBMLQualTransition;
import org.tts.model.SBMLSBaseEntity;
import org.tts.model.SBMLSBaseExtensionQual;

@Service
public class SBMLServiceImpl implements SBMLService {

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
			setSbaseProperties(compartment, sbmlCompartment);
			setCompartmentProperties(compartment, sbmlCompartment);
			sbmlCompartmentList.add(sbmlCompartment);
		}

		Map<String, SBMLCompartment> compartmentLookupMap = new HashMap<>();
		for (SBMLSBaseEntity  compartment: sbmlCompartmentList) {
			compartmentLookupMap.put(compartment.getSbaseId(), (SBMLCompartment) compartment);
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
				SBMLSBaseExtensionQual sbmlSBaseExtensionQual = new SBMLSBaseExtensionQual();
				sbmlSBaseExtensionQual.setEntityUUID(UUID.randomUUID().toString());
				sbmlSBaseExtensionQual.setShortLabel(qualModelPlugin.getPackageName());
				sbmlSBaseExtensionQual.setNamespaceURI(qualModelPlugin.getElementNamespace());
				sbmlSBaseExtensionQual.setParentModel(sbmlModelEntity);
				
				List<SBMLQualSpecies> sbmlQualSpeciesList = convertQualSpecies(qualModelPlugin.getListOfQualitativeSpecies(), compartmentLookupMap);
				Map<String, SBMLQualSpecies> qualSpeciesLookupMap = new HashMap<>();
				for (SBMLQualSpecies qualSpecies : sbmlQualSpeciesList) {
					qualSpeciesLookupMap.put(qualSpecies.getSbaseId(), qualSpecies);
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
		// also link to contained species here (or probably later)
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
		target.setSbaseId(source.getId());
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
		
	}

}
