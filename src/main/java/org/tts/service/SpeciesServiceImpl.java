package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphSpecies;
import org.tts.repository.SpeciesRepository;

@Service
public class SpeciesServiceImpl implements SpeciesService {

	SpeciesRepository speciesRepository;
	
	@Autowired
	public SpeciesServiceImpl(SpeciesRepository speciesRepository) {
		this.speciesRepository = speciesRepository;
	}

	@Override
	public GraphSpecies saveOrUpdate(GraphSpecies newSpecies) {
		return speciesRepository.save(newSpecies, 1);
	}

	@Override
	public GraphSpecies getBySbmlIdString(String sbmlIdString) {
		return speciesRepository.getBySbmlIdString(sbmlIdString);
	}

}
