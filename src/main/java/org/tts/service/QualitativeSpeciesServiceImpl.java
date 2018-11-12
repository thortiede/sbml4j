package org.tts.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.GraphQualitativeSpecies;
import org.tts.model.GraphSpecies;
import org.tts.repository.QualitativeSpeciesRepository;
import org.tts.repository.SpeciesRepository;


@Service
public class QualitativeSpeciesServiceImpl implements QualitativeSpeciesService {

	QualitativeSpeciesRepository qualitativeSpeciesRepository;
	SpeciesRepository speciesRepository;
	
	@Autowired
	public QualitativeSpeciesServiceImpl(QualitativeSpeciesRepository qualitativeSpeciesRepository, SpeciesRepository speciesRepository) {
		this.qualitativeSpeciesRepository = qualitativeSpeciesRepository;
		this.speciesRepository = speciesRepository;
	}

	@Transactional
	public GraphQualitativeSpecies saveOrUpdate(GraphQualitativeSpecies newQualitativeSpecies) {
		return qualitativeSpeciesRepository.save(newQualitativeSpecies, 1);	}

	@Override
	public GraphQualitativeSpecies getBySbmlIdString(String sbmlIdString) {
		return qualitativeSpeciesRepository.getBySbmlIdString(sbmlIdString);	}

	@Override
	public List<GraphQualitativeSpecies> updateQualitativeSpeciesList(List<GraphQualitativeSpecies> listQualitativeSpecies) {
		
		List<GraphQualitativeSpecies> newQualitativeSpeciesList = new ArrayList<GraphQualitativeSpecies>();
		for (GraphQualitativeSpecies newQualitativeSpecies : listQualitativeSpecies) {
			GraphQualitativeSpecies existingQualitativeSpecies = getBySbmlIdString(newQualitativeSpecies.getSbmlIdString());
			if(existingQualitativeSpecies != null) {
				newQualitativeSpecies.setId(existingQualitativeSpecies.getId());
				newQualitativeSpecies.setVersion(existingQualitativeSpecies.getVersion());
			}
			GraphSpecies existingIsSpecies = speciesRepository.getBySbmlIdString(newQualitativeSpecies.getSpecies().getSbmlIdString());
			if(existingIsSpecies != null) {
				GraphSpecies newIsSpecies = existingIsSpecies;
				newIsSpecies.setId(existingIsSpecies.getId());
				newIsSpecies.setVersion(existingIsSpecies.getVersion());
				newQualitativeSpecies.setSpecies(newIsSpecies);
			}
			newQualitativeSpeciesList.add(newQualitativeSpecies);
		}
		return newQualitativeSpeciesList;
	}

	
}
