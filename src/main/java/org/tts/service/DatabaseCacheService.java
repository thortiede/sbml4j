package org.tts.service;

import java.util.Map;

import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.SBMLQualSpecies;

public interface DatabaseCacheService {

	Iterable<GraphBaseEntity> getAllEntities();
	
	Iterable<SBMLQualSpecies> getAllQualSpecies();
	
	Map<String, SBMLQualSpecies> getSymbolToQualSpeciesMap();
	
	
	
}
