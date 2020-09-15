package org.tts.service;

import java.io.ByteArrayOutputStream;

import org.tts.model.api.Output.ApocPathReturnType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;

public interface GraphMLService {

	
	ByteArrayOutputStream getGraphMLForApocPathReturn(Iterable<ApocPathReturnType> apocPathReturn, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(Iterable<FlatEdge> flatEdges,
			Iterable<FlatSpecies> unconnectedSpecies, boolean directed);

}