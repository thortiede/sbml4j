package org.tts.repository.flat;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.tts.model.api.Output.FlatMappingReturnType;
import org.tts.model.flat.FlatNetworkMapping;

@Repository
public interface FlatNetworkMappingRepository extends Neo4jRepository<FlatNetworkMapping, Long> {

	FlatNetworkMapping findByEntityUUID(String entityUUID);

	FlatNetworkMapping findByEntityUUID(String mappingUuid, @Depth int depth);

	
	@Query("match (m:MappingNode) "
			+ "WHERE  m.entityUUID = $entityUUID "
			+ "WITH m MATCH "
			+ "(m)"
			+ "-[w1:Warehouse]->"
			+ "(s:FlatSpecies)-[r]->(s2:FlatSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(m) "
			+ "WHERE w1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN type(r) as relationshipType, "
			+ "r as relationship, "
			+ "startNode(r) as startNode,"
			+ "endNode(r) as endNode")
	Iterable<FlatMappingReturnType> getNodesAndRelationshipsForMapping(String entityUUID);
	
}
