/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.repository.warehouse;

import java.util.List;

import org.sbml4j.model.base.GraphEnum.FileNodeType;
import org.sbml4j.model.warehouse.FileNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface FileNodeRepository extends Neo4jRepository<FileNode, Long> {

	public List<FileNode> findByFileNodeTypeAndFilenameAndMd5sum(FileNodeType fileNodeType, String originalFilename, String md5sum);
	
}
