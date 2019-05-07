package org.tts.repository.oldModel;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.old.GraphTransition;

public interface TransitionRepository extends Neo4jRepository<GraphTransition, Long> {

	//GraphTransition getBySbmlIdString(String sbmlIdString);

	GraphTransition getByMetaid(String metaId);

	GraphTransition getByTransitionIdString(String transitionIdString);
	
}
