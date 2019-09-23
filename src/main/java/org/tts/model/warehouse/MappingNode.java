package org.tts.model.warehouse;

import java.util.Set;

import org.tts.model.common.GraphEnum.NetworkMappingType;

public class MappingNode extends WarehouseGraphNode {

	private NetworkMappingType mappingType;
	
	private String mappingName;

	private Set<String> mappingNodeTypes;
	
	private Set<String> mappingRelationTypes;
	
	private String baseNetworkEntityUUID;
	
	private String geneSymbol;
	
	private int minSize;
	private int maxSize;
	
	
	public Set<String> getMappingNodeTypes() {
		return mappingNodeTypes;
	}

	public void setMappingNodeTypes(Set<String> mappingNodeTypes) {
		this.mappingNodeTypes = mappingNodeTypes;
	}

	public void addMappingNodeType(String type) {
		this.mappingNodeTypes.add(type);
	}
	
	public Set<String> getMappingRelationTypes() {
		return mappingRelationTypes;
	}

	public void setMappingRelationTypes(Set<String> mappingRelationTypes) {
		this.mappingRelationTypes = mappingRelationTypes;
	}
	
	public void addMappingRelationType(String type) {
		this.mappingRelationTypes.add(type);
	}

	

	public NetworkMappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(NetworkMappingType mappingType) {
		this.mappingType = mappingType;
	}

	public String getMappingName() {
		return mappingName;
	}

	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}

	public String getGeneSymbol() {
		return geneSymbol;
	}

	public void setGeneSymbol(String geneSymbol) {
		this.geneSymbol = geneSymbol;
	}

	public long getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public String getBaseNetworkEntityUUID() {
		return baseNetworkEntityUUID;
	}

	public void setBaseNetworkEntityUUID(String baseNetworkEntityUUID) {
		this.baseNetworkEntityUUID = baseNetworkEntityUUID;
	}
	
}
