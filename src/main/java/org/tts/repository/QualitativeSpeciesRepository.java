package org.tts.repository;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphQualitativeSpecies;

public interface QualitativeSpeciesRepository extends Neo4jRepository<GraphQualitativeSpecies, Long> {

	GraphQualitativeSpecies getBySbmlIdString(String sbmlIdString);

}
