package org.tts.service;

import org.tts.model.GraphReaction;

public interface ReactionService {

	GraphReaction saveOrUpdate(GraphReaction newSpecies);
	
	GraphReaction getBySbmlIdString(String sbmlIdString);
	
}
