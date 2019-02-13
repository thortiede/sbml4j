package org.tts.model;

import java.util.ArrayList;
import java.util.List;

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
	private List<BiomodelsQualifier> relatedSBMLSBaseEntities;

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
	public List<BiomodelsQualifier> getRelatedSBMLSBaseEntities() {
		return relatedSBMLSBaseEntities;
	}

	public void setRelatedSBMLSBaseEntities(List<BiomodelsQualifier> relatedSBMLSBaseEntities) {
		this.relatedSBMLSBaseEntities = relatedSBMLSBaseEntities;
	}
	
	public void addRelatedSBMLSBaseEntity(BiomodelsQualifier relatedSBMLSBaseEntity) {
		if (this.relatedSBMLSBaseEntities == null) {
			this.relatedSBMLSBaseEntities = new ArrayList<>();
		}
		this.relatedSBMLSBaseEntities.add(relatedSBMLSBaseEntity);
	}
	public void addRelatedSBMLSBaseEntities(List<BiomodelsQualifier> relatedSBMLSBaseEntities) {
		if (this.relatedSBMLSBaseEntities == null) {
			this.relatedSBMLSBaseEntities = new ArrayList<>();
		}
		for(BiomodelsQualifier relatedSBMLSBaseEntity : relatedSBMLSBaseEntities) {
			this.relatedSBMLSBaseEntities.add(relatedSBMLSBaseEntity);
		}
		
	}
	
}
