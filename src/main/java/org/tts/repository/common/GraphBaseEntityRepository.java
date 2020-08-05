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

package org.tts.repository.common;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.GraphBaseEntity;

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
