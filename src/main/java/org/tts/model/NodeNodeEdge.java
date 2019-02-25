package org.tts.model;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class NodeNodeEdge {

	public String node1;
	public String node2;
	public String edge;
	
	public NodeNodeEdge() {
		
	}
	
	public NodeNodeEdge(String node1, String node2, String edge) {
		this.node1 = node1;
		this.node2 = node2;
		this.edge = edge;
	}

	public String getNode1() {
		return node1;
	}

	public void setNode1(String node1) {
		this.node1 = node1;
	}

	public String getNode2() {
		return node2;
	}

	public void setNode2(String node2) {
		this.node2 = node2;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}
		
}
