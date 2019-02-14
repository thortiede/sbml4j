package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLQualSpecies extends SBMLCompartmentalizedSBaseEntity {

	/**
	 * Indicates whether the level of this qualitativeSpecies
	 * is fixed or can be varied
	 */
	private boolean constant;
	/**
	 * Non-negative integer that defines the initial level of this qualitativeSpecies
	 */
	private int initialLevel;
	
	/**
	 * Non-negative integer that sets the maximal level of this qualitativeSpecies
	 */
	private int maxLevel;
	
	@Relationship(type = "IS")
	private SBMLSpecies correspondingSpecies;
	
	
	public SBMLSpecies getCorrespondingSpecies() {
		return correspondingSpecies;
	}

	public void setCorrespondingSpecies(SBMLSpecies correspondingSpecies) {
		this.correspondingSpecies = correspondingSpecies;
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public int getInitialLevel() {
		return initialLevel;
	}

	public void setInitialLevel(int initialLevel) {
		this.initialLevel = initialLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

}
