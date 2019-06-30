package org.tts.model.warehouse;

import org.tts.model.common.GraphEnum.NetworkMappingType;

public class MappingNode extends WarehouseGraphNode {

	private NetworkMappingType mappingType;
	
	private String mappingName;

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
