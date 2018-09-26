package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphModel;
import org.tts.repository.ModelRepository;
import org.tts.repository.SpeciesRepository;

@Service
public class ModelServiceImpl implements ModelService {

	ModelRepository modelRepository;
	//SpeciesRepository speciesRepository;
	
	@Autowired
	public ModelServiceImpl(ModelRepository modelRepository) { //, SpeciesRepository speciesRepository
		this.modelRepository = modelRepository;
		//this.speciesRepository = speciesRepository;
	}

	@Override
	public GraphModel saveOrUpdate(GraphModel newModel) {
		
		return modelRepository.save(newModel, 5);
	}

}
