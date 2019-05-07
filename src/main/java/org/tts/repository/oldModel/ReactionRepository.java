package org.tts.repository.oldModel;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.old.GraphReaction;

public interface ReactionRepository extends Neo4jRepository<GraphReaction, Long> {

	GraphReaction getBySbmlIdString(String sbmlIdString);

	GraphReaction getBySbmlNameString(String sbmlNameString);
	
	GraphReaction getBySbmlNameString(String sbmlNameString, int depth);

}
