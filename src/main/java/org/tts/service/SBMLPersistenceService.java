package org.tts.service;

import java.util.Map;

import org.tts.model.GraphBaseEntity;
import org.tts.model.SBMLSBaseEntity;

public interface SBMLPersistenceService {

	public Iterable<SBMLSBaseEntity> saveAll(Map<String, Iterable<SBMLSBaseEntity>> entityMap);

	public GraphBaseEntity save(GraphBaseEntity o);


	//public Map<String, Iterable<SBMLSBaseEntity>> findExisting(Map<String, Iterable<SBMLSBaseEntity>> allEntities);
	
}
