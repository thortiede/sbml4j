package org.tts.service;

import java.util.Map;

import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.SBMLSBaseEntity;

public interface SBMLPersistenceService {

	public Iterable<SBMLSBaseEntity> saveAll(Map<String, Iterable<SBMLSBaseEntity>> entityMap);

	public GraphBaseEntity save(GraphBaseEntity o);

	public boolean checkIfExists(GraphBaseEntity o);

	public GraphBaseEntity getByEntityUUID(String entityUUID);


	//public Map<String, Iterable<SBMLSBaseEntity>> findExisting(Map<String, Iterable<SBMLSBaseEntity>> allEntities);
	
}
