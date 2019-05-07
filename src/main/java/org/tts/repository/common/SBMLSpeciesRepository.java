package org.tts.repository.common;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.tts.model.common.SBMLSpecies;

@Repository
public interface SBMLSpeciesRepository extends Neo4jRepository<SBMLSpecies, Long> {
	public SBMLSpecies findBySBaseName(String sBaseName);

	public SBMLSpecies findBySBaseId(String sBaseId);
	
	@Query(value = "MATCH (t:SBMLSpecies) RETURN DISTINCT t.sBaseSboTerm;")
	public Iterable<String> getNodeTypes();

}
