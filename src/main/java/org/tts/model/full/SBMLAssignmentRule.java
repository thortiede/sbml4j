package org.tts.model.full;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLSBaseEntity;

public class SBMLAssignmentRule extends SBMLRule {

	/**
	 * Relationship to an entity that receives the Rule defined
	 * in the Math Element
	 * Entities from the core model that can be target of an InitialAssignment are:
	 * Compartment, Species, SpeciesReference, Parameter (global)
	 * Some Entities from Extensions may also be target of InitialAssignments
	 * The target needs to be of type SBMLSBaseEntity
	 * 
	 * The SBML attribute {@code variable} if AssignmentRule holds the SID for
	 * the targeted entity.
	 */
	@Relationship(type = "TARGETS", direction = Relationship.OUTGOING)
	private SBMLSBaseEntity targetSBaseEntity;
}
