package org.tts.service;

import java.util.Map;

import org.tts.model.SBMLSBaseEntity;

public interface SBMLPersistenceService {

	public Iterable<SBMLSBaseEntity> saveAll(Map<String, Iterable<SBMLSBaseEntity>> entityMap);
	
}
