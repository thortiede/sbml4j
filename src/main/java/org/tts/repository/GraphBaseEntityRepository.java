package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphBaseEntity;

public interface GraphBaseEntityRepository extends Neo4jRepository<GraphBaseEntity, Long> {

}
