package org.tts.model.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;


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

	@Relationship(type="IN", direction=Relationship.OUTGOING)
	private List<Organism> organisms;
	
	@Labels
	private List<String> labels = new ArrayList<>();
	
	@Properties
	private Map<String, Object> annotation = new HashMap<>();
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getEntityUUID() {
		return entityUUID;
	}

	public void setEntityUUID(String entityUUID) {
		this.entityUUID = entityUUID;
	}
	
	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	
	public boolean addLabel(String newLabel) {
		try {
			this.labels.add(newLabel);
		} catch (Exception e) {
			// element could not be added
			return false;
		}
		return true;
	}
	
	public boolean removeLabel(String name) {
		try {
			this.labels.remove(name);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	@JsonIgnore
	public Map<String, Object> getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Map<String, Object> annotation) {
		this.annotation = annotation;
	}
	
}
