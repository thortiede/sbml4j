package org.tts.repository.warehouse;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.WarehouseGraphEdge;

public interface WarehouseGraphEdgeRepository extends Neo4jRepository<WarehouseGraphEdge, Long> {

}
