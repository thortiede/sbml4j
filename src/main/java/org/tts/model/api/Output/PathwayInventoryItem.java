package org.tts.model.api.Output;

import java.util.ArrayList;
import java.util.List;



public class PathwayInventoryItem extends WarehouseInventoryItem {

	
	private String pathwayId;
	private int numberOfNodes = 0;
	private List<String> nodeTypes = new ArrayList<>();
	private int numberOfTransitions = 0;
	private List<String> transitionTypes = new ArrayList<>();;
	private int numberOfReactions = 0;
	private List<String> compartments = new ArrayList<>();
	private int numberOfCompartments = 0;
	
	
	public String getPathwayId() {
		return pathwayId;
	}
	public void setPathwayId(String pathwayId) {
		this.pathwayId = pathwayId;
	}
	
	
	public int getNumberOfNodes() {
		return numberOfNodes;
	}
	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}
	
	public void increaseNodeCounter() {
		this.setNumberOfNodes(this.getNumberOfNodes() + 1);
	}
	public List<String> getNodeTypes() {
		return nodeTypes;
	}
	public void setNodeTypes(List<String> nodeTypesList) {
		for (String type : nodeTypesList) {
			this.addNodeType(type);
		}
	}
	public void addNodeType(String nodeType) {
		if(!this.nodeTypes.contains(nodeType)) {
			this.nodeTypes.add(nodeType);
		}
	}
	public int getNumberOfTransitions() {
		return numberOfTransitions;
	}
	public void increaseTransitionCounter() {
		this.setNumberOfTransitions(this.getNumberOfTransitions() + 1);
	}
	
	public void setNumberOfTransitions(int numberOfTransitions) {
		this.numberOfTransitions = numberOfTransitions;
	}
	public List<String> getTransitionTypes() {
		return transitionTypes;
	}
	public void setTransitionTypes(List<String> transitionTypesList) {
		for(String type : transitionTypesList) {
			this.addTransitionType(type);
		}
	}
	public void addTransitionType(String transitionType) {
		if(!this.transitionTypes.contains(transitionType)) {
			this.transitionTypes.add(transitionType);
		}
	}
	
	public int getNumberOfReactions() {
		return numberOfReactions;
	}
	public void setNumberOfReactions(int numberOfReactions) {
		this.numberOfReactions = numberOfReactions;
	}
	public void increaseReactionCounter() {
		this.setNumberOfReactions(this.getNumberOfReactions() + 1);
		
	}
	public List<String> getCompartments() {
		return compartments;
	}
	public void setCompartments(List<String> compartments) {
		for (String compartment : compartments) {
			this.addCompartment(compartment);
		}
	}
	public void addCompartment(String compartment) {
		if(!this.compartments.contains(compartment)) {
			this.compartments.add(compartment);
		}
	}
	public int getNumberOfCompartments() {
		return numberOfCompartments;
	}
	public void setNumberOfCompartments(int numberOfCompartments) {
		this.numberOfCompartments = numberOfCompartments;
	}
	public void increaseComparmentCounter() {
		this.setNumberOfCompartments(this.getNumberOfCompartments() + 1);
	}
	
}
