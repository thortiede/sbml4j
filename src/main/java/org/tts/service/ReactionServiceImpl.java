package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.GraphReaction;
import org.tts.repository.ReactionRepository;

@Service
public class ReactionServiceImpl implements ReactionService {

	ReactionRepository reactionRepository;
	
	@Autowired
	public ReactionServiceImpl(ReactionRepository reactionRepository) {
		this.reactionRepository = reactionRepository;
	}

	@Transactional
	public GraphReaction saveOrUpdate(GraphReaction newReaction) {
		return reactionRepository.save(newReaction, 1); // TODO: Which depth here? Does this cause optimistic locking execption?
	}

	@Override
	public GraphReaction getBySbmlIdString(String sbmlIdString) {
		return reactionRepository.getBySbmlIdString(sbmlIdString);
	}

	@Override
	public GraphReaction getBySbmlNameString(String sbmlNameString) {
		return reactionRepository.getBySbmlNameString(sbmlNameString);
	}

}
