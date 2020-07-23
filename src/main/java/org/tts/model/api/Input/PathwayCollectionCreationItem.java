package org.tts.model.api.Input;

import java.util.List;

public class PathwayCollectionCreationItem {

	private String name;
	
	private String description;
	
	private List<String> sourcePathwayEntityUUIDs;
	
	private String databaseEntityUUID;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getSourcePathwayEntityUUIDs() {
		return sourcePathwayEntityUUIDs;
	}

	public void setSourcePathwayEntityUUIDs(List<String> sourcePathwayEntityUUIDs) {
		this.sourcePathwayEntityUUIDs = sourcePathwayEntityUUIDs;
	}

	public String getDatabaseEntityUUID() {
		return databaseEntityUUID;
	}

	public void setDatabaseEntityUUID(String databaseEntityUUID) {
		this.databaseEntityUUID = databaseEntityUUID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
