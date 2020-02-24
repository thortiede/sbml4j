package org.tts.model.api.Output;

import java.util.List;

import org.springframework.data.neo4j.annotation.QueryResult;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;

@QueryResult
public class ApocPathReturnType {

	private List<FlatSpecies> pathNodes;
	
	private List<FlatEdge> pathEdges;

	public List<FlatSpecies> getPathNodes() {
		return pathNodes;
	}

	public void setPathNodes(List<FlatSpecies> pathNodes) {
		this.pathNodes = pathNodes;
	}

	public List<FlatEdge> getPathEdges() {
		return pathEdges;
	}

	public void setPathEdges(List<FlatEdge> pathEdges) {
		this.pathEdges = pathEdges;
	}
}
