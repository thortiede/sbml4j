package org.tts.repository.oldModel;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.old.GraphModel;

public interface ModelRepository extends Neo4jRepository<GraphModel, Long> {

	public GraphModel getByModelName(String modelName);
	
}
