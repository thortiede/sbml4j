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
package org.sbml4j.model.full.ext.qual;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.full.ext.SBMLModelExtension;
import org.sbml4j.model.sbml.ext.qual.SBMLQualSpecies;

public class SBMLQualModelExtension extends SBMLModelExtension {
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	List<SBMLQualSpecies> sbmlQualSpecies;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	List<SBMLQualTransition> sbmlQualTransitions;
	
	public List<SBMLQualSpecies> getSbmlQualSpecies() {
		return sbmlQualSpecies;
	}

	public void setSbmlQualSpecies(List<SBMLQualSpecies> sbmlQualSpecies) {
		this.sbmlQualSpecies = sbmlQualSpecies;
	}

	public List<SBMLQualTransition> getSbmlQualTransitions() {
		return sbmlQualTransitions;
	}

	public void setSbmlQualTransitions(List<SBMLQualTransition> sbmlQualTransitions) {
		this.sbmlQualTransitions = sbmlQualTransitions;
	}
}
