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
package org.sbml4j.repository.common;

import org.sbml4j.model.common.GraphBaseEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Repository for handling <a href="#{@link}">{@link GraphBaseEntity}</a>
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
public interface GraphBaseEntityRepository extends Neo4jRepository<GraphBaseEntity, Long> {

	/**
	 * Find a <a href="#{@link}">{@link GraphBaseEntity}</a> by its entityUUID attribute
	 * 
	 * @param entityUUID the entityUUID to find
	 * @return <a href="#{@link}">{@link GraphBaseEntity}</a> found for the entityUUID attribute
	 */
	GraphBaseEntity findByEntityUUID(String entityUUID);
	

}
