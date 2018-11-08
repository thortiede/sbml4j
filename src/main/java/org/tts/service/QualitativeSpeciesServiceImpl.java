package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphQualitativeSpecies;
import org.tts.repository.QualitativeSpeciesRepository;


@Service
public class QualitativeSpeciesServiceImpl implements QualitativeSpeciesService {

	QualitativeSpeciesRepository qualitativeSpeciesRepository;
	
	@Autowired
	public QualitativeSpeciesServiceImpl(QualitativeSpeciesRepository qualitativeSpeciesRepository) {
		this.qualitativeSpeciesRepository = qualitativeSpeciesRepository;
	}

	@Override
	public GraphQualitativeSpecies saveOrUpdate(GraphQualitativeSpecies newQualitativeSpecies) {
		return qualitativeSpeciesRepository.save(newQualitativeSpecies, 1);	}

	@Override
	public GraphQualitativeSpecies getBySbmlIdString(String sbmlIdString) {
		return qualitativeSpeciesRepository.getBySbmlIdString(sbmlIdString);	}

	
}
