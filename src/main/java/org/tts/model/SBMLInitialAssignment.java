package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLInitialAssignment extends SBMLSBaseEntity {

	/**
	 * Relationship to an entity that receives the initialAssignment defined
	 * in the Math Element
	 * Entities from the core model that can be target of an InitialAssignment are:
	 * Compartment, Species, SpeciesReference, Parameter (global)
	 * Some Entities from Extensions may also be target of InitialAssignments
	 * The target needs to be of type SBMLSBaseEntity
	 * 
	 * The SBML attribute {@code symbol} of InitialAssignment holds the SID for
	 * the targeted entity.
	 */
	@Relationship(type = "TARGETS", direction = Relationship.OUTGOING)
	private SBMLSBaseEntity targetSBaseEntity; 
	
	
	/**
	 * In SBML the InitialAssignment element can have up to one Math element
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
