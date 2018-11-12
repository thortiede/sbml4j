package org.tts.service;

import java.util.List;

import org.tts.model.GraphSpecies;

public interface SpeciesService {

	GraphSpecies saveOrUpdate(GraphSpecies newSpecies);
	
	GraphSpecies getBySbmlIdString(String sbmlIdString);

	List<GraphSpecies> updateSpeciesList(List<GraphSpecies> listSpecies);
	
}
