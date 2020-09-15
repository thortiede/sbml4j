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

package org.tts.service.warehouse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.Organism;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
import org.tts.repository.warehouse.FileNodeRepository;
import org.tts.service.GraphBaseEntityService;

/**
 * Service to handle creation and querying of <a href="#{@link}">{@link FileNode}</a>
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class FileNodeService {
	
	@Autowired
	FileNodeRepository fileNodeRepository;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	/**
	 * Create a <a href="#{@link}">{@link FileNode}</a> for the input parameters.
	 * Checks for the existence of the <a href="#{@link}">{@link FileNode}</a>  and returns it if it exists
	 * 
	 * @param fileNodeType The <a href="#{@link}">{@link FileNodeType}</a>
	 * @param org The <a href="#{@link}">{@link Organism}</a> associated with this <a href="#{@link}">{@link DatabaseNode}</a>
	 * @param filename The filename of the <a href="#{@link}">{@link FileNode}</a> to check
	 * @return the <a href="#{@link}">{@link FileNode}</a> created in the database
	 */
	public FileNode createFileNode(FileNodeType fileNodeType, Organism org, String filename) {
		if (this.fileNodeExists(fileNodeType, org, filename)) {
			return this.getFileNode(fileNodeType, org, filename);
		}
		FileNode newFileNode = new FileNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newFileNode);
		newFileNode.setFileNodeType(fileNodeType);
		newFileNode.setFilename(filename);
		newFileNode.setOrganism(org);
		return this.fileNodeRepository.save(newFileNode);
	}
	
	/**
	 * Check whether the <a href="#{@link}">{@link FileNode}</a> does exist in the database
	 * 
	 * @param fileNodeType The <a href="#{@link}">{@link FileNodeType}</a>
	 * @param org The <a href="#{@link}">{@link Organism}</a> associated with this <a href="#{@link}">{@link DatabaseNode}</a>
	 * @param filename The filename of the <a href="#{@link}">{@link FileNode}</a> to check
	 * @return true if the <a href="#{@link}">{@link FileNode}</a> exists in the database, false otherwise
	 */
	public boolean fileNodeExists(FileNodeType fileNodeType, Organism org, String filename) {

		if (this.getFileNode(fileNodeType, org, filename) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Get the <a href="#{@link}">{@link FileNode}</a> for the input parameters.
	 * 
	 * @param fileNodeType The <a href="#{@link}">{@link FileNodeType}</a>
	 * @param org The <a href="#{@link}">{@link Organism}</a> associated with this <a href="#{@link}">{@link DatabaseNode}</a>
	 * @param filename The filename of the <a href="#{@link}">{@link FileNode}</a> to check
	 * @return the <a href="#{@link}">{@link FileNode}</a> found in the database, null if none found
	 */
	public FileNode getFileNode(FileNodeType fileNodeType, Organism org, String filename) {
		// TODO: Use all function arguments to check
		List<FileNode> filenodes = this.fileNodeRepository.findByFileNodeTypeAndFilename(fileNodeType,
				filename);
		for (FileNode node : filenodes) {
			if (node.getOrganism().equals(org)) {
				return node;
			}
		}
		return null;
	}

}
