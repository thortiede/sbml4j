package org.tts.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class ExternalResourceEntity extends GraphBaseEntity {

	private String uri;
	
	private String databaseFromUri;
	
	private String longIdentifierFromUri;
	
	private String shortIdentifierFromUri;
	
	@Relationship(type = "BQ", direction = Relationship.INCOMING)
	private SBMLSBaseEntity relatedSBMLSBaseEntity;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDatabaseFromUri() {
		return databaseFromUri;
	}

	public void setDatabaseFromUri(String databaseFromUri) {
		this.databaseFromUri = databaseFromUri;
	}

	public String getLongIdentifierFromUri() {
		return longIdentifierFromUri;
	}

	public void setLongIdentifierFromUri(String longIdentifierFromUri) {
		this.longIdentifierFromUri = longIdentifierFromUri;
	}

	public String getShortIdentifierFromUri() {
		return shortIdentifierFromUri;
	}

	public void setShortIdentifierFromUri(String shortIdentifierFromUri) {
		this.shortIdentifierFromUri = shortIdentifierFromUri;
	}

	@JsonIgnore
	public SBMLSBaseEntity getRelatedSBMLSBaseEntity() {
		return relatedSBMLSBaseEntity;
	}

	public void setRelatedSBMLSBaseEntity(SBMLSBaseEntity relatedSBMLSBaseEntity) {
		this.relatedSBMLSBaseEntity = relatedSBMLSBaseEntity;
	}
}
