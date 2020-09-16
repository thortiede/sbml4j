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

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.DatabaseNode;

public interface DatabaseNodeRepository extends Neo4jRepository<DatabaseNode, Long> {

	List<DatabaseNode> findBySourceAndSourceVersion(String source, String sourceVersion);

	DatabaseNode findByEntityUUID(String entityUUID);

}
