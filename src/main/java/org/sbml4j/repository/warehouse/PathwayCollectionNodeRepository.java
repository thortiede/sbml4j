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

import org.sbml4j.model.warehouse.PathwayCollectionNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PathwayCollectionNodeRepository extends Neo4jRepository<PathwayCollectionNode, Long> {

	
	@Query("MATCH p="
			+ "(pc:PathwayCollectionNode)"
			+ "-[fo:FOR]->"
			+ "(o:Organism) "
			+ "where pc.pathwayCollectionName = $pathwayCollectionName "
			+ "RETURN p")
	public Iterable<PathwayCollectionNode> findAllByPathwayCollectionName(String pathwayCollectionName);
	
}
