package org.tts.repository.warehouse;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.warehouse.WarehouseGraphNode;

public interface WarehouseGraphNodeRepository extends Neo4jRepository<WarehouseGraphNode, Long> {

	/**
	 * match (w:WarehouseGraphNode)-[e:Warehouse {warehouseGraphEdgeType: "CONTAINS"}]-(:WarehouseGraphNode {entityUUID: "2c841061-fe1c-439b-927a-82ddb06ea604"}) return w;
	 * 
	 * @param contains
	 * @param endNode
	 * @return
	 */
	@Query(value="MATCH"
			+ "(w:WarehouseGraphNode)"
			+ "-[e:Warehouse {warehouseGraphEdgeType: {0}}]->"
			+ "(:WarehouseGraphNode {entityUUID: {1}})"
			+ "RETURN w")
	WarehouseGraphNode findByWarehouseGraphEdgeTypeAndEndNode(WarehouseGraphEdgeType warehouseGraphEdgeType, String EndNodeEntityUUID);

}
