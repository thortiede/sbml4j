package org.tts.model;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class NodeNodeEdge {

	public String node1;
	public String node1UUID;
	public String node2;
	public String node2UUID;
	public String edge;
	
	public NodeNodeEdge() {
		
	}
	
	public NodeNodeEdge(String node1, String node1UUID, String node2, String node2UUID, String edge) {
		this.node1 = node1;
		this.node1UUID = node1UUID;
		this.node2 = node2;
		this.node2UUID = node2UUID;
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
	
	public String getNode1UUID() {
		return node1UUID;
	}

	public void setNode1UUID(String node1uuid) {
		node1UUID = node1uuid;
	}

	public String getNode2UUID() {
		return node2UUID;
	}

	public void setNode2UUID(String node2uuid) {
		node2UUID = node2uuid;
	}

	public boolean equals(NodeNodeEdge other) {
		if(			this.node1.equals(other.getNode1()) 
				&&  this.node1UUID.equals(other.getNode1UUID())
				&& 	this.node2.equals(other.getNode2()) 
				&&  this.node2UUID.equals(other.getNode2UUID())
				&& 	this.edge.equals(other.getEdge())) {
			return true;
		} else {
			return false;
		}
	}
		
}
