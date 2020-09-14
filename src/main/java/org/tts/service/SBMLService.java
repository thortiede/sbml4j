/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
 */

package org.tts.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.springframework.web.multipart.MultipartFile;
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
	 * @return List of GraphBaseEntity that got persisted (or were already in the database) and are part of the model
	 */
	public List<ProvenanceEntity> buildAndPersist(Model model, FileNode sbmlFileNode, ProvenanceGraphActivityNode activityNode);	
}
