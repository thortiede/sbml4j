package org.tts.service;

import java.io.ByteArrayOutputStream;

import org.tts.model.api.Output.ApocPathReturnType;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;

public interface GraphMLService {

	@Deprecated
	String getGraphMLString(NodeEdgeList nodeEdgeList);

	@Deprecated
	ByteArrayOutputStream getGraphMLByteArrayOutputStream(NodeEdgeList nodeEdgeList,
			ByteArrayOutputStream byteArrayOutputStream);

	ByteArrayOutputStream getGraphMLForApocPathReturn(Iterable<ApocPathReturnType> apocPathReturn, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(Iterable<FlatEdge> flatEdges,
			Iterable<FlatSpecies> unconnectedSpecies, boolean directed);

}