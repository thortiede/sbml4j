package org.tts.service;

import org.tts.model.GraphTransition;

public interface TransitionService {

	GraphTransition saveOrUpdate(GraphTransition newTransition);
	
	GraphTransition getBySbmlIdString(String sbmlIdString);
	
}
