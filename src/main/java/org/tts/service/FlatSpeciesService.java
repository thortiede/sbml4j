package org.tts.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.flat.FlatSpecies;
import org.tts.repository.flat.FlatSpeciesRepository;

@Service
public class FlatSpeciesService {
	
	@Autowired
	FlatSpeciesRepository flatSpeciesRepository;
	
	public Iterable<FlatSpecies> persistListOfFlatSpecies(List<FlatSpecies> flatSpeciesList) {
		return this.flatSpeciesRepository.save(flatSpeciesList, 1);
	}
	
	public FlatSpecies persistFlatSpecies(FlatSpecies species) {
		return this.flatSpeciesRepository.save(species, 1);
	}
}
