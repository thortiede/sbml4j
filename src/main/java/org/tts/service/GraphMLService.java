package org.tts.service;

import java.io.ByteArrayOutputStream;

import org.tts.model.api.Output.NodeEdgeList;

public interface GraphMLService {

	String getGraphMLString(NodeEdgeList nodeEdgeList);

	ByteArrayOutputStream getGraphMLByteArrayOutputStream(NodeEdgeList nodeEdgeList,
			ByteArrayOutputStream byteArrayOutputStream);

}