package org.tts.service;

import java.util.List;

import org.tts.model.GraphModel;

public interface ModelService {

	GraphModel saveOrUpdate(GraphModel newModel);
	
	GraphModel getByModelName(String modelName);

	List<GraphModel> search(String searchString);

	GraphModel getById(Long id);
}
