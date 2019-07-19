package org.tts.model.warehouse;

public class PathwayCollectionNode extends WarehouseGraphNode {

	private String pathwayCollectionName;
	
	private String pathwayCollectionDescription;

	public String getPathwayCollectionName() {
		return pathwayCollectionName;
	}

	public void setPathwayCollectionName(String pathwayCollectionName) {
		this.pathwayCollectionName = pathwayCollectionName;
	}

	public String getPathwayCollectionDescription() {
		return pathwayCollectionDescription;
	}

	public void setPathwayCollectionDescription(String pathwayCollectionDescription) {
		this.pathwayCollectionDescription = pathwayCollectionDescription;
	}
}
