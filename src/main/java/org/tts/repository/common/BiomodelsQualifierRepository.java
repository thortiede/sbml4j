package org.tts.repository.common;

import java.util.List;

import org.sbml.jsbml.CVTerm;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.BiomodelsQualifier;

public interface BiomodelsQualifierRepository extends Neo4jRepository<BiomodelsQualifier, Long> {

	List<BiomodelsQualifier> findByTypeAndQualifier(CVTerm.Type type, CVTerm.Qualifier qualifier);
	
	@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "-[bq:BQ]-"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE "
			+ "s.entityUUID = $sbmlSpeciesEntityUUID "
			+ "RETURN bq, e")
	List<BiomodelsQualifier> findAllForSBMLSpecies(String sbmlSpeciesEntityUUID);
	
}
