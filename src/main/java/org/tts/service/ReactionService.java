package org.tts.service;

import java.util.List;

import org.tts.model.GraphReaction;

public interface ReactionService {

	GraphReaction saveOrUpdate(GraphReaction newReaction);
	
	GraphReaction getBySbmlIdString(String sbmlIdString);

	GraphReaction getBySbmlNameString(String sbmlNameString);
	GraphReaction getBySbmlNameString(String sbmlNameString, int depth);

	List<GraphReaction> updateReactionList(List<GraphReaction> listReaction);

}
