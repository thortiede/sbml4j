package org.tts.model.common;

import org.neo4j.ogm.annotation.Relationship;

/**
 * SBaseEntity with compartment information
 * This SBaseEntity is located in the set compartment
 * 
 * There are Entities that have to have a compartment set (i.e. Species from the core package),
 * and Entities for which it is optional (i.e. Reaction from core package)
 * @author ttiede
 *
 */
public class SBMLCompartmentalizedSBaseEntity extends SBMLSBaseEntity {

	@Relationship(type = "CONTAINED_IN", direction = Relationship.OUTGOING)
	private SBMLCompartment compartment;
	
	private boolean compartmentMandatory;

	public SBMLCompartment getCompartment() {
		return compartment;
	}

	public void setCompartment(SBMLCompartment compartment) {
		this.compartment = compartment;
	}

	public boolean isCompartmentMandatory() {
		return compartmentMandatory;
	}

	public void setCompartmentMandatory(boolean compartmentMandatory) {
		this.compartmentMandatory = compartmentMandatory;
	}
	
}
