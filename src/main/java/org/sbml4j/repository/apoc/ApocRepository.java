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
package org.sbml4j.repository.apoc;

import org.sbml4j.model.api.Output.ApocPathReturnType;
import org.sbml4j.model.common.GraphBaseEntity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
public interface ApocRepository extends Neo4jRepository<GraphBaseEntity, Long> {
	/**
	 * Run the path.expand algorithm from the APOC plugin
	 * @param startNodeEntityUUID entityUUID of the startNode for path.expand
	 * @param apocRelationshipString the relationshipString for path.expand
	 * @param apocNodeString the nodeString for path.expand 
	 * @param minDepth the minDepth for path.expand
	 * @param maxDepth the maxDepth for path.expand
	 * @return Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a>
	 */
	@Query("MATCH "
			+ "(start:FlatSpecies) "
			+ "WHERE start.entityUUID = $startNodeEntityUUID "
			+ "WITH start "
			+ "CALL apoc.path.expand("
			+ "start, "
			+ "$apocRelationshipString, "
			+ "$apocNodeString, "
			+ "$minDepth, "
			+ "$maxDepth "
			+ ") "
			+ "YIELD "
			+ "path as apocPath "
			+ "RETURN nodes(apocPath) as pathNodes, relationships(apocPath) as pathEdges;"
			)
	Iterable<ApocPathReturnType> runApocPathExpandFor(String startNodeEntityUUID, String apocRelationshipString, String apocNodeString, int minDepth, int maxDepth);
	
	/**
	 * Run the DijkstraWithDefaultWeight algorithm from the APOC plugin
	 * @param startNodeEntityUUID The entityUUID of the startNode of the algorithm
	 * @param endNodeEntityUUID The entityUUID of the endNode of the algorithm
	 * @param relationTypesApocString apocRelationshipString the relationshipString of the algorithm
	 * @param propertyName The propertyName on the relationships to use as weight
	 * @param defaultWeight The default weight to use of the property is not present
	 * @return Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a>
	 */
	@Query(value="MATCH (fs1:FlatSpecies) "
			+ "WHERE fs1.entityUUID = $startNodeEntityUUID "
			+ "WITH fs1 "
			+ "MATCH (fs2:FlatSpecies) "
			+ "WHERE fs2.entityUUID = $endNodeEntityUUID "
			+ "CALL apoc.algo.dijkstraWithDefaultWeight("
			+ "fs1, "
			+ "fs2, "
			+ "$relationTypesApocString, "
			+ "$propertyName, "
			+ "$defaultWeight) "
			+ "YIELD "
			+ "path as pp, "
			+ "weight as w "
			+ "RETURN nodes(pp) as pathNodes, "
			+ "relationships(pp) as pathEdges;")
	Iterable<ApocPathReturnType> apocDijkstraWithDefaultWeight(String startNodeEntityUUID, String endNodeEntityUUID, 
			String relationTypesApocString, String propertyName, float defaultWeight);
	
}
