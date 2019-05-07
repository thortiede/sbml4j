package org.tts.service;

import org.tts.model.api.Output.NodeEdgeList;

public interface GraphMLService {

	String getGraphMLString(NodeEdgeList nodeEdgeList);

}