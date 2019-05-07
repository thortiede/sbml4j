package org.tts.service.oldModel;

import java.util.List;

import org.tts.model.old.GraphTransition;

public interface TransitionService {

	GraphTransition saveOrUpdate(GraphTransition newTransition);
	
	GraphTransition getByMetaid(String metaid);

	List<GraphTransition> updateTransitionList(List<GraphTransition> listTransition);

	List<GraphTransition> findAll();

	GraphTransition getByTransitionIdString(String transitionIdString);
	
}
