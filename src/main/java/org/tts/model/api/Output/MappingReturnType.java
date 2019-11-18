package org.tts.model.api.Output;

import java.util.Map;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class MappingReturnType {

	private Map<String, Object> node1Properties;
	
	private Map<String, Object> node2Properties;
	
	private String edge;

	public Map<String, Object> getNode1Properties() {
		return node1Properties;
	}

	public void setNode1Properties(Map<String, Object> node1Properties) {
		this.node1Properties = node1Properties;
	}

	public Map<String, Object> getNode2Properties() {
		return node2Properties;
	}

	public void setNode2properties(Map<String, Object> node2Properties) {
		this.node2Properties = node2Properties;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}
	
}