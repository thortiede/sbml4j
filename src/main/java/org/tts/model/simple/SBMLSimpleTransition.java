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
package org.tts.model.simple;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLSBaseEntity;

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
