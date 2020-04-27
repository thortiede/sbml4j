package org.tts.model.common;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Properties;
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
	
	/**
	 * The flag to indicate whether this entity is active
	 * i.e. findable by inventory-queries
	 */
	private boolean isActive;
	

	/*@Relationship(type="IN", direction=Relationship.OUTGOING)
	private List<Organism> organisms;
	*/
	
	@Properties
	private Map<String, Object> annotation = new HashMap<>();
	
	@Properties
	private Map<String, String> annotationType = new HashMap<>();
	
	@JsonIgnore
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonIgnore
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
	
	//@JsonIgnore
	public Map<String, Object> getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Map<String, Object> annotation) {
		this.annotation = annotation;
	}
	
	public void addAnnotation(Map<String,Object> annotation) {
		if (this.annotation == null) {
			this.annotation = annotation;
		} else {
			for (String key : annotation.keySet()) {
				this.annotation.put(key, annotation.get(key));
			}
			
		}
	}
	public void addAnnotation(String key, Object value) {
		if (this.annotation == null) {
			this.annotation = new HashMap<>();
		}
		this.annotation.put(key, value);
	}

	public Map<String, String> getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(Map<String, String> annotationType) {
		this.annotationType = annotationType;
	}
	
	public void addAnnotationType(String annotationName, String annotationType) {
		if(this.annotationType == null) {
			this.annotationType = new HashMap<>();
		}
		this.annotationType.put(annotationName, annotationType);
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	
}
