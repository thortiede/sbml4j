package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;


public class SBMLSBaseExtensionQual extends SBMLSBaseEntity implements SBMLSBaseExtension, SBMLModelExtension {

	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	List<SBMLQualSpecies> sbmlQualSpecies;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	List<SBMLQualTransition> sbmlQualTransitions;

	@Relationship(type = "IS_EXTENSION_OF", direction = Relationship.OUTGOING)
	public SBMLModelEntity parentModel;
	
	private String namespaceURI;
	
	private String shortLabel = "qual";
	
	private boolean required = false;
	 
	@Override
	public boolean extendsModel() {
		return true;
	}

	@Override
	public boolean extendsSBML() {
		return false;
	}

	@Override
	public boolean isRequired() {
		return this.required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public String getShortLabel() {
		return this.shortLabel;
	}
	
	@Override
	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;		
	}

	@Override
	public String getNamespaceURI() {
		return this.namespaceURI;
	}
	
	@Override
	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	@Override
	public SBMLModelEntity getParentModel() {
		return this.parentModel;
	}
	
	public void setParentModel(SBMLModelEntity parent) {
		this.parentModel = parent;
	}

}
