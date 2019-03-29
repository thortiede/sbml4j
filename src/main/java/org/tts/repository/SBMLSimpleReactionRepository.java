package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.SBMLSimpleReaction;


public interface SBMLSimpleReactionRepository extends Neo4jRepository<SBMLSimpleReaction, Long> {

	SBMLSimpleReaction findBySBaseName(String sBaseName, int depth);
	
}
