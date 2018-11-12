package org.tts.service;

import org.tts.model.GraphModel;

public interface ModelService {

	GraphModel saveOrUpdate(GraphModel newModel);
	
	GraphModel getByModelName(String modelName);
}
