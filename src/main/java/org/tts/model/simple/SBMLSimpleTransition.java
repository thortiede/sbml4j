package org.tts.model;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class SBMLSimpleTransition extends SBMLSBaseEntity {

	@Relationship(type = "IS_INPUT")
	private SBMLQualSpecies inputSpecies;
	
	@Relationship(type = "IS_OUTPUT")
	private SBMLQualSpecies outputSpecies;

	private String transitionId;
	
	public String getTransitionId() {
		return transitionId;
	}

	public void setTransitionId(String transitionId) {
		this.transitionId = transitionId;
	}

	public SBMLQualSpecies getInputSpecies() {
		return inputSpecies;
	}

	public void setInputSpecies(SBMLQualSpecies inputSpecies) {
		this.inputSpecies = inputSpecies;
	}

	public SBMLQualSpecies getOutputSpecies() {
		return outputSpecies;
	}

	public void setOutputSpecies(SBMLQualSpecies outputSpecies) {
		this.outputSpecies = outputSpecies;
	}
	
}
