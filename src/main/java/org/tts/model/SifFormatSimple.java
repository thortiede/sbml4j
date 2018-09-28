package org.tts.model;

import java.util.List;

public class SifFormatSimple {

	private String sourceNode;
	private String relationshipType;
	private List<String> targetNodes;
	
	
	
	
	
	public SifFormatSimple(String sourceNode, String relationshiptype, List<String> targetNodes) {
		super();
		this.sourceNode = sourceNode;
		this.relationshipType = relationshiptype;
		this.targetNodes = targetNodes;
	}


	public String toString() {
		return String.join(" ", sourceNode, relationshipType, String.join(" ", targetNodes));
	}



	public String getSourceNode() {
		return sourceNode;
	}





	public void setSourceNode(String sourceNode) {
		this.sourceNode = sourceNode;
	}





	public String getRelationshiptype() {
		return relationshipType;
	}





	public void setRelationshiptype(String relationshipType) {
		this.relationshipType = relationshipType;
	}





	public List<String> getTargetNodes() {
		return targetNodes;
	}





	public void setTargetNodes(List<String> targetNodes) {
		this.targetNodes = targetNodes;
	}
	
	public String getTargetNode(int index) {
		try {
			return this.getTargetNodes().get(index);
		} catch (IndexOutOfBoundsException e) {
			
			e.printStackTrace();
			return "";
		}
	}
	
	public boolean addTargetNode(String targetNode) {
		return this.targetNodes.add(targetNode);
	}
	
	
	
}
