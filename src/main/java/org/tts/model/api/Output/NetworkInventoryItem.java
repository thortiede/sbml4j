package org.tts.model.api.Output;

import java.util.HashSet;
import java.util.Set;

import org.tts.model.common.GraphEnum.NetworkMappingType;

public class NetworkInventoryItem extends WarehouseInventoryItem {

	private int numberOfNodes;
	private int numberOfRelations;
	private Set<String> nodeTypes;
	private Set<String> relationTypes;
	private NetworkMappingType networkMappingType;
	
	public NetworkInventoryItem() {
		super();
	}
	protected NetworkInventoryItem(NetworkInventoryItem item) {
		super((WarehouseInventoryItem)item);
		this.add(item.getLinks());
		this.numberOfNodes = item.getNumberOfNodes();
		this.numberOfRelations = item.getNumberOfRelations();
		this.nodeTypes = item.getNodeTypes();
		this.relationTypes = item.getRelationTypes();
		this.networkMappingType = item.getNetworkMappingType();
	}
	public int getNumberOfNodes() {
		return numberOfNodes;
	}
	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}
	public int getNumberOfRelations() {
		return numberOfRelations;
	}
	public void setNumberOfRelations(int numberOfRelations) {
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
	public NetworkMappingType getNetworkMappingType() {
		return networkMappingType;
	}
	public void setNetworkMappingType(NetworkMappingType networkMappingType) {
		this.networkMappingType = networkMappingType;
	}
}
