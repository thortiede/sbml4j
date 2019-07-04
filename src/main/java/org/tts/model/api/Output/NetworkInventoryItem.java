package org.tts.model.api.Output;

import java.util.HashSet;
import java.util.Set;

public class NetworkInventoryItem extends WarehouseInventoryItem {

	private String numberOfNodes;
	private String numberOfRelations;
	private Set<String> nodeTypes;
	private Set<String> relationTypes;
	
	public String getNumberOfNodes() {
		return numberOfNodes;
	}
	public void setNumberOfNodes(String numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}
	public String getNumberOfRelations() {
		return numberOfRelations;
	}
	public void setNumberOfRelations(String numberOfRelations) {
		this.numberOfRelations = numberOfRelations;
	}
	public Set<String> getNodeTypes() {
		return nodeTypes;
	}
	public void setNodeTypes(Set<String> nodeTypes) {
		this.nodeTypes = nodeTypes;
	}
	public void addNodeType(String type) {
		if (this.nodeTypes == null) {
			this.nodeTypes = new HashSet<>();
		}
		this.nodeTypes.add(type);
	}
	public Set<String> getRelationTypes() {
		return relationTypes;
	}
	public void setRelationTypes(Set<String> relationTypes) {
		this.relationTypes = relationTypes;
	}
	
	public void addRelationType(String type) {
		if (this.relationTypes == null) {
			this.relationTypes = new HashSet<>();
		}
		this.relationTypes.add(type);
	}
}
