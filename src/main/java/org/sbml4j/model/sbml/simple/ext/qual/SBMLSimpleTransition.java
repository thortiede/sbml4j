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
package org.sbml4j.model.sbml.simple.ext.qual;

import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.common.SBMLQualSpecies;
import org.sbml4j.model.common.SBMLSBaseEntity;

@NodeEntity
public class SBMLSimpleTransition extends SBMLSBaseEntity {

	@Relationship(type = "IS_INPUT")
	private List<SBMLQualSpecies> inputSpecies;
	
	@Relationship(type = "IS_OUTPUT")
	private List<SBMLQualSpecies> outputSpecies;

	private String transitionId;
	
	public String getTransitionId() {
		return transitionId;
	}

	public void setTransitionId(String transitionId) {
		this.transitionId = transitionId;
	}

	public List<SBMLQualSpecies> getInputSpecies() {
		return inputSpecies;
	}

	public void setInputSpecies(List<SBMLQualSpecies> inputSpecies) {
		this.inputSpecies = inputSpecies;
	}

	public List<SBMLQualSpecies> getOutputSpecies() {
		return outputSpecies;
	}

	public void setOutputSpecies(List<SBMLQualSpecies> outputSpecies) {
		this.outputSpecies = outputSpecies;
	}
	
}
