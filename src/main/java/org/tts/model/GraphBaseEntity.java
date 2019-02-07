package org.tts.model;

import java.util.UUID;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Version;

@NodeEntity
public class GraphBaseEntity {

	@Id @GeneratedValue
	private Long id = null;

	@Version
	private Long version;
	
	/**
	 * The unique identifier of every entity in this application
	 * Entities shall reference other entities only by this UUID
	 */
	private String entityUUID;

	public String getEntityUUID() {
		return entityUUID;
	}

	public void setEntityUUID(String entityUUID) {
		this.entityUUID = entityUUID;
	}
	
}
