package org.tts.model;


import org.neo4j.ogm.annotation.Relationship;


public class SBMLModelExtension extends SBMLSBaseExtension {

	@Relationship(type = "IS_EXTENSION_OF", direction = Relationship.OUTGOING)
	public SBMLModelEntity parentModel;

	public SBMLModelEntity getParentModel() {
		return parentModel;
	}
	
	public void setParentModel(SBMLModelEntity parentModel) {
		this.parentModel = parentModel;
	}
}
