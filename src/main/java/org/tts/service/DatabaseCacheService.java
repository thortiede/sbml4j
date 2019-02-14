package org.tts.service;

import java.util.Map;

import org.tts.model.GraphBaseEntity;
import org.tts.model.SBMLQualSpecies;

public interface DatabaseCacheService {

	Iterable<GraphBaseEntity> getAllEntities();
	
	Iterable<SBMLQualSpecies> getAllQualSpecies();
	
	Map<String, SBMLQualSpecies> getSymbolToQualSpeciesMap();
	
	
	
}
