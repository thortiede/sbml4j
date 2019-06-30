package org.tts.model.api.Output;

import org.springframework.hateoas.ResourceSupport;

public class PathwayInventoryItem extends ResourceSupport {

	private String entityUUID;
	private String pathwayId;
	private String pathwayName;
	private String pathwaySource;
	private String pathwaySourceVersion;
	private int numberOfNodes;
	private String[] nodeTypes;
	private int numberOfTransitions;
	private String[] transitionTypes;
	private int numberOfReactions;
	
	public String getEntityUUID() {
		return entityUUID;
	}
	public void setEntityUUID(String entityUUID) {
		this.entityUUID = entityUUID;
	}
	public String getPathwayId() {
		return pathwayId;
	}
	public void setPathwayId(String pathwayId) {
		this.pathwayId = pathwayId;
	}
	public String getPathwayName() {
		return pathwayName;
	}
	public void setPathwayName(String pathwayName) {
		this.pathwayName = pathwayName;
	}
	public String getPathwaySource() {
		return pathwaySource;
	}
	public void setPathwaySource(String pathwaySource) {
		this.pathwaySource = pathwaySource;
	}
	public String getPathwaySourceVersion() {
		return pathwaySourceVersion;
	}
	public void setPathwaySourceVersion(String pathwaySourceVersion) {
		this.pathwaySourceVersion = pathwaySourceVersion;
	}
	public int getNumberOfNodes() {
		return numberOfNodes;
	}
	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}
	public String[] getNodeTypes() {
		return nodeTypes;
	}
	public void setNodeTypes(String[] nodeTypes) {
		this.nodeTypes = nodeTypes;
	}
	public int getNumberOfTransitions() {
		return numberOfTransitions;
	}
	public void setNumberOfTransitions(int numberOfTransitions) {
		this.numberOfTransitions = numberOfTransitions;
	}
	public String[] getTransitionTypes() {
		return transitionTypes;
	}
	public void setTransitionTypes(String[] transitionTypes) {
		this.transitionTypes = transitionTypes;
	}
	public int getNumberOfReactions() {
		return numberOfReactions;
	}
	public void setNumberOfReactions(int numberOfReactions) {
		this.numberOfReactions = numberOfReactions;
	}
	
	
}
