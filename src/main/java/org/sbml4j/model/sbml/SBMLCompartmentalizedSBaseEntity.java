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
package org.sbml4j.model.sbml;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.common.SBMLSBaseEntity;

/**
 * SBaseEntity with compartment information
 * This SBaseEntity is located in the set compartment
 * 
 * There are Entities that have to have a compartment set (i.e. Species from the core package),
 * and Entities for which it is optional (i.e. Reaction from core package)
 * @author Thorsten Tiede
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
