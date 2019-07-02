package org.tts.repository.common;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.ContentGraphNode;

public interface ContentGraphNodeRepository extends Neo4jRepository<ContentGraphNode, Long> {

	@Query(value = "MATCH "
			+ "(p:PathwayNode {entityUUID: {0}}"
			+ "-[:Warehouse {WarehouseGraphEdgeType: \"CONTAINS\"}->"
			+ "(c:ContentGraphNode) "
			+ "RETURN c")	
	public Iterable<ContentGraphNode> findAllByPathwayNodeEntityUUID(String pathwayNodeEntityUUID);
	
}
