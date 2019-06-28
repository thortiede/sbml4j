package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.warehouse.FileNode;
import org.tts.model.common.Organism;

public interface SBMLService {

	public boolean isValidSBML(File file);
	
	public boolean isSBMLVersionCompatible(File file);
	
	public Model extractSBMLModel(MultipartFile file) throws XMLStreamException, IOException;
	
	public Map<String, Iterable<SBMLSBaseEntity>> extractSBMLEntities(Model model);

	public Map<String, Iterable<Object>> extractAndConnectExternalResources(
			Map<String, Iterable<SBMLSBaseEntity>> allEntities);
	
	/**
	 * Main entry Method coming from a controller
	 * @param model The jSBML Model ({@link org.sbml.jsbml.Model}) to persist
	 * @param sbmlFileNode The name of the file this model originates from
	 * @return List of GraphBaseEntity that got persisted (or were already in the database) and are part of the model
	 */
	public List<ProvenanceEntity> buildAndPersist(Model model, FileNode sbmlFileNode, ProvenanceGraphActivityNode activityNode);

	//public List<GraphBaseEntity> persistFastSimple(Model model);
	
	/**
	 * Remove all Entities from underlying repository
	 * @return true if database is empty, false otherwise
	 */
	public boolean clearDatabase();
	
	/**
	 * Query the underlying repository for all entities of minimal type
	 * @return List of all entities found in repository which are or are derived from
	 * {@link org.tts.model.common.GraphBaseEntity}
	 */
	public List<GraphBaseEntity> getAllEntities();

	
}
