package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.Sign;

public class SBMLQualInput extends SBMLSBaseEntity {

	private Sign sign;
	
	private InputTransitionEffect transitionEffect;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private SBMLQualSpecies qualSpecies;
	
	/**
	 * Non-negative integer to set the threshold level of the qualSpecies in this Input
	 */
	private int thresholdLevel;

	public Sign getSign() {
		return sign;
	}

	public void setSign(Sign sign) {
		this.sign = sign;
	}

	public InputTransitionEffect getTransitionEffect() {
		return transitionEffect;
	}

	public void setTransitionEffect(InputTransitionEffect transitionEffect) {
		this.transitionEffect = transitionEffect;
	}

	public SBMLQualSpecies getQualSpecies() {
		return qualSpecies;
	}

	public void setQualSpecies(SBMLQualSpecies qualSpecies) {
		this.qualSpecies = qualSpecies;
	}

	public int getThresholdLevel() {
		return thresholdLevel;
	}

	public void setThresholdLevel(int thresholdLevel) {
		this.thresholdLevel = thresholdLevel;
	}
	
}
