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
package org.sbml4j.service.warehouse;

import java.util.List;

import org.sbml4j.model.base.GraphEnum.FileNodeType;
import org.sbml4j.model.warehouse.DatabaseNode;
import org.sbml4j.model.warehouse.FileNode;
import org.sbml4j.model.warehouse.Organism;
import org.sbml4j.repository.warehouse.FileNodeRepository;
import org.sbml4j.service.GraphBaseEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	 * @param md5sum The MD5 Sum of the File of this <a href="#{@link}">{@link FileNode}</a>
	 * @return the <a href="#{@link}">{@link FileNode}</a> created in the database
	 */
	public FileNode createFileNode(FileNodeType fileNodeType, Organism org, String filename, String md5sum) {
		if (this.fileNodeExists(fileNodeType, org, filename, md5sum)) {
			return this.getFileNode(fileNodeType, org, filename, md5sum);
		}
		FileNode newFileNode = new FileNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newFileNode);
		newFileNode.setFileNodeType(fileNodeType);
		newFileNode.setFilename(filename);
		newFileNode.setOrganism(org);
		newFileNode.setMd5sum(md5sum);
		return this.fileNodeRepository.save(newFileNode);
	}
	
	/**
	 * Check whether the <a href="#{@link}">{@link FileNode}</a> does exist in the database
	 * 
	 * @param fileNodeType The <a href="#{@link}">{@link FileNodeType}</a>
	 * @param org The <a href="#{@link}">{@link Organism}</a> associated with this <a href="#{@link}">{@link DatabaseNode}</a>
	 * @param filename The filename of the <a href="#{@link}">{@link FileNode}</a> to check
	 * @param md5sum The MD5 Sum of the File of this <a href="#{@link}">{@link FileNode}</a>
	 * @return true if the <a href="#{@link}">{@link FileNode}</a> exists in the database, false otherwise
	 */
	public boolean fileNodeExists(FileNodeType fileNodeType, Organism org, String filename, String md5sum) {

		if (this.getFileNode(fileNodeType, org, filename, md5sum) != null) {
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
	 * @param md5sum The MD5 Sum of the File of this <a href="#{@link}">{@link FileNode}</a>
	 * @return the <a href="#{@link}">{@link FileNode}</a> found in the database, null if none found
	 */
	public FileNode getFileNode(FileNodeType fileNodeType, Organism org, String filename, String md5sum) {
		// TODO: Use all function arguments to check
		List<FileNode> filenodes = this.fileNodeRepository.findByFileNodeTypeAndFilenameAndMd5sum(fileNodeType,
				filename, md5sum);
		for (FileNode node : filenodes) {
			if (node.getOrganism().equals(org)) {
				return node;
			}
		}
		return null;
	}

}
