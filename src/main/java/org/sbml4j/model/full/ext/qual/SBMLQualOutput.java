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
import org.sbml.jsbml.ext.qual.OutputTransitionEffect;
import org.sbml4j.model.common.SBMLQualSpecies;
import org.sbml4j.model.sbml.SBMLSBaseEntity;

public class SBMLQualOutput extends SBMLSBaseEntity {
		
	private OutputTransitionEffect transitionEffect;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private SBMLQualSpecies qualSpecies;
	
	/**
	 * Non-negative integer to set the output level of the qualSpecies in this Output
	 */
	private int outputLevel;

	public OutputTransitionEffect getTransitionEffect() {
		return transitionEffect;
	}

	public void setTransitionEffect(OutputTransitionEffect transitionEffect) {
		this.transitionEffect = transitionEffect;
	}

	public SBMLQualSpecies getQualSpecies() {
		return qualSpecies;
	}

	public void setQualSpecies(SBMLQualSpecies qualSpecies) {
		this.qualSpecies = qualSpecies;
	}

	public int getOutputLevel() {
		return outputLevel;
	}

	public void setOutputLevel(int outputLevel) {
		this.outputLevel = outputLevel;
	}
}
