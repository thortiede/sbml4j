package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import org.tts.model.GraphSBase;

public interface SBaseRepository extends Neo4jRepository<GraphSBase, Long> {

	
	GraphSBase getBySbmlIdString(String sbmlIdString);
	
	
}
