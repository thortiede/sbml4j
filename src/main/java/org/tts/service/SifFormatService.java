package org.tts.service;

import org.tts.model.NodeEdgeList;

public interface SifFormatService {

	public NodeEdgeList convertToSif(NodeEdgeList fullNet);
}
