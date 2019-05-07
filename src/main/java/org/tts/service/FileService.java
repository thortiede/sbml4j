package org.tts.service;

import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.SifFile;

public interface FileService {
	
	SifFile getSifFromNodeEdgeList(NodeEdgeList nodeEdgeList);
	
}
