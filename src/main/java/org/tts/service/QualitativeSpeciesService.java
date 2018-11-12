package org.tts.service;

import java.util.List;

import org.tts.model.GraphQualitativeSpecies;

public interface QualitativeSpeciesService {

	GraphQualitativeSpecies saveOrUpdate(GraphQualitativeSpecies newSpecies);
	
	GraphQualitativeSpecies getBySbmlIdString(String sbmlIdString);

	List<GraphQualitativeSpecies> updateQualitativeSpeciesList(List<GraphQualitativeSpecies> listQualitativeSpecies);
	
}
