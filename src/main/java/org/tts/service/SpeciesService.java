package org.tts.service;

import org.tts.model.GraphSpecies;

public interface SpeciesService {

	GraphSpecies saveOrUpdate(GraphSpecies newSpecies);
	
	GraphSpecies getBySbmlIdString(String sbmlIdString);
	
}
