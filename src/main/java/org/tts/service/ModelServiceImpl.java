package org.tts.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphCompartment;
import org.tts.model.GraphModel;
import org.tts.model.GraphSBase;
import org.tts.repository.CompartmentRepository;
import org.tts.repository.ModelRepository;
import org.tts.repository.QualitativeSpeciesRepository;
import org.tts.repository.ReactionRepository;
import org.tts.repository.SBaseRepository;
import org.tts.repository.SpeciesRepository;
import org.tts.repository.TransitionRepository;

@Service
public class ModelServiceImpl implements ModelService {

	ModelRepository modelRepository;
	
	
	@Autowired
	public ModelServiceImpl(
			ModelRepository modelRepository) 
	{
		super();
		this.modelRepository = modelRepository;
	}

	@Override
	public GraphModel saveOrUpdate(GraphModel newModel) {
		
		GraphModel modelInDb = modelRepository.getByModelName(newModel.getModelName());
		if (modelInDb != null) {
			newModel.setId(modelInDb.getId());
			newModel.setVersion(modelInDb.getVersion());
		}

		
		return modelRepository.save(newModel, 10);
	}

	@Override
	public GraphModel getByModelName(String modelName) {
		return modelRepository.getByModelName(modelName);
	}
	
	
	

}
