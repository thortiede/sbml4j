package org.tts.repository.simpleModel;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.simple.SBMLSimpleTransition;

public interface SBMLSimpleTransitionRepository extends Neo4jRepository<SBMLSimpleTransition, Long> {

	SBMLSimpleTransition getByTransitionId(String transitionId, int depth);
	
	@Query(value = "MATCH (t:SBMLSimpleTransition) RETURN DISTINCT t.sBaseSboTerm;")
	Iterable<String> getTransitionTypes();
	
	
}
