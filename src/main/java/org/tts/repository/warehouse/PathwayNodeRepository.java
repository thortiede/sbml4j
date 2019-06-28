package org.tts.repository.warehouse;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.PathwayNode;

public interface PathwayNodeRepository extends Neo4jRepository<PathwayNode, Long> {

}
