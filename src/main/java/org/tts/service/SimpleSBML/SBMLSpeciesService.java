package org.tts.service.SimpleSBML;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.SBMLSpecies;
import org.tts.repository.common.SBMLSpeciesRepository;

@Service
public class SBMLSpeciesService {
	
	@Autowired
	SBMLSpeciesRepository sbmlSpeciesRepository;
	
	
	
	public SBMLSpecies findBysBaseName(String sBaseName) {
		return this.sbmlSpeciesRepository.findBysBaseName(sBaseName);
		
	}



	public Iterable<SBMLSpecies> findByBQConnectionTo(String name, String databaseFromUri) {
		return this.sbmlSpeciesRepository.findByBQConnectionTo(name, databaseFromUri);
	}
}
