package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.SBMLCompartmentalizedSBaseEntity;

public interface SBMLCompartmentalizedSBaseEntityRepository extends Neo4jRepository<SBMLCompartmentalizedSBaseEntity, Long> {

	SBMLCompartmentalizedSBaseEntity findBySBaseId(String sBaseId, int depth);
	
}
