package org.tts.service;



import org.tts.model.api.Output.NodeEdgeList;

public interface NodeEdgeListService {

	NodeEdgeList getFullNet();

	NodeEdgeList getMetabolicNet();
	
}
