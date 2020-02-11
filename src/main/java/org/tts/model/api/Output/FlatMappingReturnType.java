package org.tts.model.api.Output;

import org.springframework.data.neo4j.annotation.QueryResult;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;

@QueryResult
public class FlatMappingReturnType {

	private String relationshipType;
	private FlatEdge relationship;
	private FlatSpecies startNode;
	private FlatSpecies endNode;
	
	public String getRelationshipType() {
		return relationshipType;
	}
	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}
	public FlatEdge getRelationship() {
		return relationship;
	}
	public void setRelationship(FlatEdge relationship) {
		this.relationship = relationship;
	}
	public FlatSpecies getStartNode() {
		return startNode;
	}
	public void setStartNode(FlatSpecies startNode) {
		this.startNode = startNode;
	}
	public FlatSpecies getEndNode() {
		return endNode;
	}
	public void setEndNode(FlatSpecies endNode) {
		this.endNode = endNode;
	}
	
}
