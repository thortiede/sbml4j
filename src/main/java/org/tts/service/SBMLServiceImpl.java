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
import org.tts.model.SBMLDocumentEntity;
import org.tts.model.SBMLModelEntity;
import org.tts.model.SBMLSBaseEntity;

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
	public Model extractSBMLModel(File file) throws XMLStreamException, IOException {
		SBMLDocument doc = SBMLReader.read(file);
		return doc.getModel();
		
	}

	@Override
	public Map<String, Iterable<SBMLSBaseEntity>> extractSBMLEntities(Model model) {
		
		Map<String, Iterable<SBMLSBaseEntity>> allModelEntities = new HashMap<>();
		
		SBMLDocumentEntity sbmlDocument = new SBMLDocumentEntity();
		
		setSbaseProperties(model.getSBMLDocument(), sbmlDocument);
		sbmlDocument.setSbmlFileName("Not accessible in here");
		// set all sbmlDocument specific attributes here
		
		List<SBMLSBaseEntity> sbmlDocumentList = new ArrayList<>();
		sbmlDocumentList.add(sbmlDocument);
		allModelEntities.put("SBMLDocumentEntity", sbmlDocumentList);
		
		SBMLModelEntity sbmlModelEntity = new SBMLModelEntity();
		setSbaseProperties(model, sbmlModelEntity);
		sbmlModelEntity.setAreaUnits(model.getAreaUnits());
		sbmlModelEntity.setConversionFactor(model.getConversionFactor());
		sbmlModelEntity.setEnclosingSBMLEntity(sbmlDocument);
		// set more model specific values
		
		// then make List
		List<SBMLSBaseEntity> sbmlModelList = new ArrayList<>();
		sbmlModelList.add(sbmlModelEntity);
		allModelEntities.put("SBMLModelEntity", sbmlModelList);
		
		
		
		return null;
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
