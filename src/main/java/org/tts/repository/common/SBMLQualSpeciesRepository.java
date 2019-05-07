package org.tts.repository.common;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.tts.model.common.SBMLQualSpecies;

@Repository
public interface SBMLQualSpeciesRepository extends Neo4jRepository<SBMLQualSpecies, Long> {

	SBMLQualSpecies findBySBaseName(String sBaseName);

	SBMLQualSpecies findBySBaseId(String sBaseId);

}
