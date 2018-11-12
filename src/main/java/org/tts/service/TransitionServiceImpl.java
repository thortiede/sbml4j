package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.GraphTransition;
import org.tts.repository.TransitionRepository;

@Service
public class TransitionServiceImpl implements TransitionService {

	TransitionRepository transitionRepository;
	
	@Autowired
	public TransitionServiceImpl(TransitionRepository transitionRepository) {
		this.transitionRepository = transitionRepository;
	}

	@Transactional
	public GraphTransition saveOrUpdate(GraphTransition newTransition) {
		return transitionRepository.save(newTransition, 2); // TODO: Which depth here? Does this cause optimistic locking execption?
	}

	@Override
	public GraphTransition getBySbmlIdString(String sbmlIdString) {
		return transitionRepository.getBySbmlIdString(sbmlIdString);
	}

}
