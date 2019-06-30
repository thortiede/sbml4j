package org.tts.repository.warehouse;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.MappingNode;

public interface MappingNodeRepository extends Neo4jRepository<MappingNode, Long> {

}
