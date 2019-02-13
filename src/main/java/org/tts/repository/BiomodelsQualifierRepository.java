package org.tts.repository;

import java.util.List;

import org.sbml.jsbml.CVTerm;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.BiomodelsQualifier;

public interface BiomodelsQualifierRepository extends Neo4jRepository<BiomodelsQualifier, Long> {

	List<BiomodelsQualifier> findByTypeAndQualifier(CVTerm.Type type, CVTerm.Qualifier qualifier);
}
