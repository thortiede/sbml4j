package org.tts.repository.flat;


import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.flat.FlatEdge;

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
	Iterable<FlatEdge> getNetworkContentsFromUUID(String entityUUID);
	
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
