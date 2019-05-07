package org.tts.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

	@Transactional
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

	@Override
	public List<GraphModel> search(String searchString) {
		Iterable<GraphModel> allModelNodes = modelRepository.findAll(0);
		List<GraphModel> returnModels = new ArrayList<GraphModel>();
		if(searchString.equals("")) {
			// no search string, return all models found
			allModelNodes.forEach(returnModels::add);
		} else {
			allModelNodes.forEach(model -> {
					if(model.getModelName().contains(searchString)) {
						returnModels.add(model);
					}
				});
		}
		if(returnModels.size() > 0) {
			return returnModels;
		} else {
			return null;
		}

		
	}

	@Override
	public GraphModel getById(Long id) {
		return modelRepository.findById(id).orElse(null);
	}

	@Override
	public List<GraphModel> findAll() {
		List<GraphModel> allModels = new ArrayList<GraphModel>();
		modelRepository.findAll(1).forEach(allModels::add);
		return allModels;
	}
	
	
	

}
