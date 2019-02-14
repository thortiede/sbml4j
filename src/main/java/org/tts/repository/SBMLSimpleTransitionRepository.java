package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.SBMLSimpleTransition;

public interface SBMLSimpleTransitionRepository extends Neo4jRepository<SBMLSimpleTransition, Long> {

	SBMLSimpleTransition getByTransitionId(String transitionId, int depth);
	
	
}
