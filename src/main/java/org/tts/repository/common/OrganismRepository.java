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

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.Organism;
import org.tts.model.warehouse.WarehouseGraphNode;

/**
 * Repository for loading and persisting <a href="#{@link}">{@link Organism}</a>s
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
public interface OrganismRepository extends Neo4jRepository<Organism, Long> {

	/**
	 * Find an <a href="#{@link}">{@link Organism}</a> by its orgCode attribute
	 * 
	 * @param orgCode The three letter organism code to find
	 * @return The <a href="#{@link}">{@link Organism}</a> entity for the orgCode
	 */
	Organism findByOrgCode(String orgCode);

	/**
	 * Find the <a href="#{@link}">{@link Organism}</a> that is attached to a <a href="#{@link}">{@link WarehouseGraphNode}</a> via the FOR relationship
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link w:WarehouseGraphNode}</a>
	 * @return The <a href="#{@link}">{@link Organism}</a> found using the query
	 */
	@Query(value="MATCH"
			+ "(w:WarehouseGraphNode)"
			+ "-[:FOR]->"
			+ "(o:Organism) "
			+ "WHERE w.entityUUID = $entityUUID "
			+ "RETURN o")
	Organism findOrganismForWarehouseGraphNode(String entityUUID);

}
