package org.tts.service;

import org.tts.model.NodeEdgeList;
import org.tts.model.SifFile;

public interface FileService {
	
	SifFile getSifFromNodeEdgeList(NodeEdgeList nodeEdgeList);
	
}
