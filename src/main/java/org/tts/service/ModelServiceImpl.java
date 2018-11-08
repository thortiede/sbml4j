package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphModel;
import org.tts.repository.ModelRepository;

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

		
		return modelRepository.save(newModel);
	}

	@Override
	public GraphModel getByModelName(String modelName) {
		return modelRepository.getByModelName(modelName);
	}
	
	
	

}
