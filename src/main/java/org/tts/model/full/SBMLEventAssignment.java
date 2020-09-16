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
package org.tts.model.full;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLSBaseEntity;

public class SBMLEventAssignment extends SBMLSBaseEntity {
	/**
	 * Relationship to an entity that receives the Rule defined
	 * in the Math Element
	 * Entities from the core model that can be target of an InitialAssignment are:
	 * Compartment, Species, SpeciesReference, Parameter (global)
	 * Some Entities from Extensions may also be target of InitialAssignments
	 * The target needs to be of type SBMLSBaseEntity
	 * 
	 * The SBML attribute {@code variable} of EventAssignment holds the SID for
	 * the targeted entity.
	 */
	@Relationship(type = "TARGETS", direction = Relationship.OUTGOING)
	private SBMLSBaseEntity targetSBaseEntity;

	/**
	 * In SBML the EventAssignment element can have up to one Math element
	 * which consists of a MathML structured content.
	 * At this point we capture this content in a String variable.
	 * In the future this might be expanded to it's own element
	 * in which the references to other objects are resolved
	 */
	private String math;
	
	public String getMath() {
		return math;
	}

	public void setMath(String math) {
		this.math = math;
	}
	
}
