package org.tts.service;

import org.tts.model.GraphTransition;

public interface TransitionService {

	GraphTransition saveOrUpdate(GraphTransition newSpecies);
	
	GraphTransition getBySbmlIdString(String sbmlIdString);
	
}
