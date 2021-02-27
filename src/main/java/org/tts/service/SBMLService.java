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
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.springframework.web.multipart.MultipartFile;
import org.tts.Exception.ModelPersistenceException;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.warehouse.FileNode;

public interface SBMLService {

	public boolean isValidSBML(File file);
	
	public boolean isSBMLVersionCompatible(File file);
	
	public Model extractSBMLModel(MultipartFile file) throws XMLStreamException, IOException;
	/**
	 * Main entry Method coming from a controller to persist a sbmlModel
	 * 
	 * @param model The jSBML Model ({@link org.sbml.jsbml.Model}) to persist
	 * @param sbmlFileNode The name of the file this model originates from
	 * @return Map of entityUUID of a ProvenanceEntity to the ProvenanceEntity that got persisted (or were already in the database) and are part of the model
	 */
	public Map<String, ProvenanceEntity> buildAndPersist(Model model, FileNode sbmlFileNode, ProvenanceGraphActivityNode activityNode) throws ModelPersistenceException;	
	
	
	public Map<String, ProvenanceEntity> buildModel(Model model);
	
}
