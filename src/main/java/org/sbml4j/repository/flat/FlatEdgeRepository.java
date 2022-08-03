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
package org.sbml4j.repository.flat;


import java.util.List;

import org.sbml4j.model.flat.FlatEdge;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface FlatEdgeRepository extends Neo4jRepository<FlatEdge, Long> {

	FlatEdge findByEntityUUID(String entityUUID);

	@Query("match (m:MappingNode) "
			+ "WHERE m.entityUUID = $entityUUID "
			+ "WITH m MATCH "
			+ "(m)"
			+ "-[w1:Warehouse]->"
			+ "(s:FlatSpecies)-[r]->(s2:FlatSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(m) "
			+ "WHERE w1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN s, r, s2")
	List<FlatEdge> getNetworkContentsFromUUID(String entityUUID);
	
	
	@Query("match (m:MappingNode)"
			+ "-[w1:Warehouse]->"
			+ "(s:FlatSpecies) "
			+ "WHERE m.entityUUID = $entityUUID "
			+ "AND w1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "WITH s match "
			+ "(s)-[r]->(s) "
			+ "RETURN s, r")
	List<FlatEdge> getSelfRelationsFromUUID(String entityUUID);
	
	@Query("match (m:MappingNode) "
			+ "WHERE m.entityUUID = $networkEntityUUID "
			+ "WITH m MATCH "
			+ "(m)"
			+ "-[w1:Warehouse]->"
			+ "(s:FlatSpecies)-[r]->(s2:FlatSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(m) "
			+ "WHERE w1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND s.entityUUID IN $nodeUUIDs "
			+ "AND s2.entityUUID IN $nodeUUIDs "
			+ "RETURN s, r, s2")
	Iterable<FlatEdge> getGeneSet(String networkEntityUUID, List<String> nodeUUIDs);
	
	
	@Query("match (m:MappingNode) "
			+ "WHERE m.entityUUID = $networkEntityUUID "
			+ "WITH m MATCH "
			+ "(m)"
			+ "-[w1:Warehouse]->"
			+ "(s:FlatSpecies)-[r]->(s2:FlatSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(m) "
			+ "WHERE w1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND s.simpleModelEntityUUID IN $simpleModelUUIDs "
			+ "AND s2.simpleModelEntityUUID IN $simpleModelUUIDs "
			+ "RETURN s, r, s2")
	Iterable<FlatEdge> getGeneSetFromSBaseUUIDs(String networkEntityUUID, List<String> simpleModelUUIDs);
}
