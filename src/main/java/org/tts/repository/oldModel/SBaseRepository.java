package org.tts.repository.oldModel;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.old.GraphSBase;

public interface SBaseRepository extends Neo4jRepository<GraphSBase, Long> {

	
	GraphSBase getBySbmlIdString(String sbmlIdString);
	
	
}
