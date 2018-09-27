package org.tts.repository;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphReaction;

public interface ReactionRepository extends Neo4jRepository<GraphReaction, Long> {

	GraphReaction getBySbmlIdString(String sbmlIdString);

}
