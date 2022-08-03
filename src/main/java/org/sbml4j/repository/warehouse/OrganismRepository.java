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

import org.sbml4j.model.warehouse.Organism;
import org.sbml4j.model.warehouse.WarehouseGraphNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

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
