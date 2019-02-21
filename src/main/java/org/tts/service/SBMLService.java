package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.GraphBaseEntity;
import org.tts.model.SBMLSBaseEntity;

public interface SBMLService {

	public boolean isValidSBML(File file);
	
	public boolean isSBMLVersionCompatible(File file);
	
	public Model extractSBMLModel(MultipartFile file) throws XMLStreamException, IOException;
	
	public Map<String, Iterable<SBMLSBaseEntity>> extractSBMLEntities(Model model);

	public Map<String, Iterable<Object>> extractAndConnectExternalResources(
			Map<String, Iterable<SBMLSBaseEntity>> allEntities);
	
	public List<GraphBaseEntity> buildAndPersist(Model model, String filename);

	//public List<GraphBaseEntity> persistFastSimple(Model model);
	
	/**
	 * Remove all Entities from underlying repository
	 * @return true if database is empty, false otherwise
	 */
	public boolean clearDatabase();
	
	/**
	 * Query the underlying repository for all entities of minimal type
	 * @return List of all entities found in repository which are or are derived from
	 * {@link org.tts.model.GraphBaseEntity}
	 */
	public List<GraphBaseEntity> getAllEntities();
}
