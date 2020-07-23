package org.tts.model.api.Output;

import org.springframework.hateoas.RepresentationModel;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WarehouseInventoryItem extends RepresentationModel<WarehouseInventoryItem> {
	private String entityUUID;
	private boolean isActive;
	private String name;
	private String organismCode;
	private String source;
	private String sourceVersion;
	private WarehouseGraphNodeType warehouseGraphNodeType;

	public WarehouseInventoryItem() {
		super();
	}
	public WarehouseInventoryItem(WarehouseInventoryItem item) {
		super();
		this.entityUUID = item.getEntityUUID();
		this.name = item.getName();
		this.organismCode = item.getOrganismCode();
		this.source = item.getSource();
		this.sourceVersion = item.getSourceVersion();
		this.warehouseGraphNodeType = item.getWarehouseGraphNodeType();
	}
	@JsonProperty("UUID")
	public String getEntityUUID() {
		return entityUUID;
	}
	public void setEntityUUID(String entityUUID) {
		this.entityUUID = entityUUID;
	}
	//@JsonIgnore
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrganismCode() {
		return organismCode;
	}
	public void setOrganismCode(String organismCode) {
		this.organismCode = organismCode;
	}
	@JsonIgnore
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	@JsonIgnore
	public String getSourceVersion() {
		return sourceVersion;
	}
	public void setSourceVersion(String sourceVersion) {
		this.sourceVersion = sourceVersion;
	}
	@JsonIgnore
	public WarehouseGraphNodeType getWarehouseGraphNodeType() {
		return warehouseGraphNodeType;
	}
	public void setWarehouseGraphNodeType(WarehouseGraphNodeType warehouseGraphNodeType) {
		this.warehouseGraphNodeType = warehouseGraphNodeType;
	}
	@JsonIgnore
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
}
