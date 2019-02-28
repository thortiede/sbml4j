package org.tts.service.SimpleModel;

import java.util.List;

import org.tts.model.NodeEdgeList;

public interface NetworkMappingService {

	public NodeEdgeList getProteinInteractionNetwork(List<String> interactionTypes);
	
	public NodeEdgeList getProteinInteractionNetwork();

	public List<String> getTransitionTypes();
	
}
