/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.model.sbml.ext.qual;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.sbml.SBMLCompartmentalizedSBaseEntity;
import org.sbml4j.model.sbml.SBMLSpecies;

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
