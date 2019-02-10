package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.SBMLSBaseEntity;

public interface SBMLService {

	public boolean isValidSBML(File file);
	
	public boolean isSBMLVersionCompatible(File file);
	
	public Model extractSBMLModel(MultipartFile file) throws XMLStreamException, IOException;
	
	public Map<String, Iterable<SBMLSBaseEntity>> extractSBMLEntities(Model model);

	public Map<String, Iterable<Object>> extractAndConnectExternalResources(
			Map<String, Iterable<SBMLSBaseEntity>> allEntities);
}
