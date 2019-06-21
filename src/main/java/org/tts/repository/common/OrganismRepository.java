package org.tts.repository.common;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.Organism;

public interface OrganismRepository extends Neo4jRepository<Organism, Long> {

	Organism findByOrgCode(String orgCode);

}
