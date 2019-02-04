package org.tts.service;

import java.util.List;

import org.tts.model.GraphTransition;

public interface TransitionService {

	GraphTransition saveOrUpdate(GraphTransition newTransition);
	
	GraphTransition getByMetaid(String metaid);

	List<GraphTransition> updateTransitionList(List<GraphTransition> listTransition);

	List<GraphTransition> findAll();

	GraphTransition getByTransitionIdString(String transitionIdString);
	
}
