package org.tts.repository.flat;

import java.util.List;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.tts.model.api.Output.ApocPathReturnType;
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
			//+ "type(relationships(apocPath)) as relationshipType,"
			//+ "start(relationships(apocPath)) as startNode,"
			//+ "end(relationships(apocPath)) as endNode"
			)
	Iterable<ApocPathReturnType> runApocPathExpandFor(String startNodeEntityUUID, String apocRelationshipString, String apocNodeString, int minDepth, int maxDepth);

	@Query(value="MATCH (m:MappingNode)-[w:Warehouse]->(fs:FlatSpecies) "
			+ "WHERE m.entityUUID = $networkEntityUUID AND w.warehouseGraphEdgeType = \"CONTAINS\" AND fs.symbol IN $startNodeSymbols "
			+ "call apoc.path.expand(fs, {2}, {3}, {4}, {5}) yield path as pp return nodes(pp) as pathNodes, relationships(pp) as pathEdges;")
	Iterable<ApocPathReturnType> findMultiNodeApocPathInNetworkFromNodeSymbols(String networkEntityUUID, List<String> startNodeSymbols, String relationTypesApocString, String nodeFilterString,
			int minPathLength, int maxPathLength);
	
	@Query(value="MATCH (fs1:FlatSpecies) where fs1.entityUUID = $startNodeEntityUUID with fs1 MATCH (fs2:FlatSpecies) where fs2.entityUUID = $endNodeEntityUUID call apoc.algo.dijkstraWithDefaultWeight(fs1, fs2, {2}, {3}, {4}) yield path as pp, weight as w return nodes(pp) as pathNodes, relationships(pp) as pathEdges;")
	Iterable<ApocPathReturnType> apocDijkstraWithDefaultWeight(String startNodeEntityUUID, String endNodeEntityUUID, String relationTypesApocString, String propertyName, float defaultWeight);
	
}
