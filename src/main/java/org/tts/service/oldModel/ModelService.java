package org.tts.service.oldModel;

import java.util.List;

import org.tts.model.old.GraphModel;

public interface ModelService {

	GraphModel saveOrUpdate(GraphModel newModel);
	
	GraphModel getByModelName(String modelName);

	List<GraphModel> search(String searchString);

	GraphModel getById(Long id);
	
	List<GraphModel> findAll();
	
}
