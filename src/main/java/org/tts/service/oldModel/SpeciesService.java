package org.tts.service.oldModel;

import java.util.List;

import org.tts.model.old.GraphSpecies;

public interface SpeciesService {

	GraphSpecies saveOrUpdate(GraphSpecies newSpecies);
	
	GraphSpecies getBySbmlIdString(String sbmlIdString);

	List<GraphSpecies> updateSpeciesList(List<GraphSpecies> listSpecies);
	
}
