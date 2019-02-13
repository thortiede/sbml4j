package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.ExternalResourceEntity;

public interface ExternalResourceEntityRepository extends Neo4jRepository<ExternalResourceEntity, Long> {

	public ExternalResourceEntity findByUri(String uri);
	
}
