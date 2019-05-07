package org.tts.repository.common;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.ExternalResourceEntity;

public interface ExternalResourceEntityRepository extends Neo4jRepository<ExternalResourceEntity, Long> {

	public ExternalResourceEntity findByUri(String uri);
	
}
