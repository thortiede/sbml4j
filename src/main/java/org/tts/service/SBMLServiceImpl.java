package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.SBMLDocumentEntity;
import org.tts.model.SBMLModelEntity;
import org.tts.model.SBMLSBaseEntity;

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
		
		setSbaseProperties(model.getSBMLDocument(), sbmlDocument);
		sbmlDocument.setSbmlFileName("Not accessible in here");
		// set all sbmlDocument specific attributes here
		
		List<SBMLSBaseEntity> sbmlDocumentList = new ArrayList<>();
		sbmlDocumentList.add(sbmlDocument);
		allModelEntities.put("SBMLDocumentEntity", sbmlDocumentList);
		
		SBMLModelEntity sbmlModelEntity = new SBMLModelEntity();
		setSbaseProperties(model, sbmlModelEntity);
		setModelProperties(model, sbmlDocument, sbmlModelEntity);
		List<SBMLSBaseEntity> sbmlModelList = new ArrayList<>();
		sbmlModelList.add(sbmlModelEntity);
		allModelEntities.put("SBMLModelEntity", sbmlModelList);
		
		
		
		return null;
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
