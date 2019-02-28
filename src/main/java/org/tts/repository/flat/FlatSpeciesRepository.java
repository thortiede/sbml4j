package org.tts.repository.flat;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.flat.FlatSpecies;

public interface FlatSpeciesRepository extends Neo4jRepository<FlatSpecies, Long> {

	FlatSpecies findBySBaseName(String sBaseName);

}
