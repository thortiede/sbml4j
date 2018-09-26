package org.tts.repository;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphTransition;

public interface TransitionRepository extends Neo4jRepository<GraphTransition, Long> {

	GraphTransition getBySbmlIdString(String sbmlIdString);

}
