package org.tts.repository.simpleModel;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.simple.SBMLSimpleReaction;


public interface SBMLSimpleReactionRepository extends Neo4jRepository<SBMLSimpleReaction, Long> {

	SBMLSimpleReaction findBySBaseName(String sBaseName, int depth);
	
}
