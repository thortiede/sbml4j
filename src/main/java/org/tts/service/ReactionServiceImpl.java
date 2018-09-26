package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphReaction;
import org.tts.repository.ReactionRepository;

@Service
public class ReactionServiceImpl implements ReactionService {

	ReactionRepository reactionRepository;
	
	@Autowired
	public ReactionServiceImpl(ReactionRepository reactionRepository) {
		this.reactionRepository = reactionRepository;
	}

	@Override
	public GraphReaction saveOrUpdate(GraphReaction newReaction) {
		return reactionRepository.save(newReaction, 1);
	}

	@Override
	public GraphReaction getBySbmlIdString(String sbmlIdString) {
		return reactionRepository.getBySbmlIdString(sbmlIdString);
	}

}
