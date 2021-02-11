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
package org.tts.repository.warehouse;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.PathwayCollectionNode;

public interface PathwayCollectionNodeRepository extends Neo4jRepository<PathwayCollectionNode, Long> {

	
	@Query("MATCH "
			+ "(pc:PathwayCollectionNode)"
			+ "-[fo:FOR]->"
			+ "(o:Organism) "
			+ "where pc.pathwayCollectionName = $pathwayCollectionName "
			+ "RETURN pc, fo, o")
	public Iterable<PathwayCollectionNode> findAllByPathwayCollectionName(String pathwayCollectionName);
	
}
