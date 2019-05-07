package org.tts.service.oldModel;

import java.util.List;

import org.tts.model.old.GraphQualitativeSpecies;

public interface QualitativeSpeciesService {

	GraphQualitativeSpecies saveOrUpdate(GraphQualitativeSpecies newSpecies);
	
	GraphQualitativeSpecies getBySbmlIdString(String sbmlIdString);

	List<GraphQualitativeSpecies> updateQualitativeSpeciesList(List<GraphQualitativeSpecies> listQualitativeSpecies);
	
}
