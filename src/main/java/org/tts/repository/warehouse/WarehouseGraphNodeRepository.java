package org.tts.repository.warehouse;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.WarehouseGraphNode;

public interface WarehouseGraphNodeRepository extends Neo4jRepository<WarehouseGraphNode, Long> {

}
