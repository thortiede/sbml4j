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
package org.sbml4j.repository.sbml.ext.sbml4j;

import org.sbml4j.model.sbml.ext.sbml4j.NameNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface NameNodeRepository extends Neo4jRepository<NameNode, Long> {

	public NameNode findByName(String name);

	@Query("MATCH "
			+ "(e:ExternalResourceEntity)"
			+ "-[KNOWNAS]->"
			+ "(n:NameNode) "
			+ "WHERE e.entityUUID = $externalResourceEntityUUID "
			+ "RETURN n")
	public Iterable<NameNode> findAllByExternalResource(String externalResourceEntityUUID);
}
