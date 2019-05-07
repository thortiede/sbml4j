package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.SBMLSBaseEntity;

public interface SBMLSBaseEntityRepository extends Neo4jRepository<SBMLSBaseEntity, Long> {

	SBMLSBaseEntity findBySBaseId(String sBaseId, int depth);
}
