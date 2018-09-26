package org.tts.service;

import org.tts.model.GraphQualitativeSpecies;

public interface QualitativeSpeciesService {

	GraphQualitativeSpecies saveOrUpdate(GraphQualitativeSpecies newSpecies);
	
	GraphQualitativeSpecies getBySbmlIdString(String sbmlIdString);
	
}
