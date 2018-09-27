package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphModel;

public interface ModelRepository extends Neo4jRepository<GraphModel, Long> {

}
