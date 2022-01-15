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
package org.sbml4j.model.full.ext.qual;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml.jsbml.ext.qual.InputTransitionEffect;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml4j.model.common.SBMLQualSpecies;
import org.sbml4j.model.sbml.SBMLSBaseEntity;

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
