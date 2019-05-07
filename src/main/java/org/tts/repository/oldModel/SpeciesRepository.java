package org.tts.repository.oldModel;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.old.GraphSpecies;

public interface SpeciesRepository extends Neo4jRepository<GraphSpecies, Long> {

	GraphSpecies getBySbmlIdString(String sbmlIdString);

}
