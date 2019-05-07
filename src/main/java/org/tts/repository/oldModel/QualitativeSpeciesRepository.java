package org.tts.repository.oldModel;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.old.GraphQualitativeSpecies;

public interface QualitativeSpeciesRepository extends Neo4jRepository<GraphQualitativeSpecies, Long> {

	GraphQualitativeSpecies getBySbmlIdString(String sbmlIdString);

}
