package org.tts.repository.common;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.SBMLFile;

public interface SBMLFileNodeRepository extends Neo4jRepository<SBMLFile, Long> {

	public SBMLFile findByFilename(String filename);
	
}
