/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.model.common;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.provenance.ProvenanceEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class ExternalResourceEntity extends ProvenanceEntity {

	private String uri;
	
	private String databaseFromUri;
	
	private String longIdentifierFromUri;
	
	private String shortIdentifierFromUri;
	
	private String name;
	
	private String[] secondaryNames;
	
	private ExternalResourceType type;
	
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getSecondaryNames() {
		return secondaryNames;
	}

	public void setSecondaryNames(String[] secondaryNames) {
		this.secondaryNames = secondaryNames;
	}
	
	public ExternalResourceType getType() {
		return type;
	}

	public void setType(ExternalResourceType type) {
		this.type = type;
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
