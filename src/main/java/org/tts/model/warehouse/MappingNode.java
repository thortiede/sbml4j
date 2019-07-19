package org.tts.model.warehouse;

import java.util.Set;

import org.tts.model.common.GraphEnum.NetworkMappingType;

public class MappingNode extends WarehouseGraphNode {

	private NetworkMappingType mappingType;
	
	private String mappingName;
	
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

	private Set<String> mappingNodeTypes;
	
	private Set<String> mappingRelationTypes;
	

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
	
}
