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
package org.sbml4j.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml4j.Exception.ModelPersistenceException;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.warehouse.FileNode;
import org.sbml4j.model.warehouse.PathwayNode;
import org.springframework.web.multipart.MultipartFile;

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
	public void buildAndPersist(Model model, FileNode sbmlFileNode, ProvenanceGraphActivityNode activityNode, PathwayNode pathwayNode) throws ModelPersistenceException; //Map<String, ProvenanceEntity>
}
