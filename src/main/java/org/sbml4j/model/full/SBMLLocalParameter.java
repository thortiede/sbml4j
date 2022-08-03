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
package org.sbml4j.model.full;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.sbml.SBMLSBaseEntity;

public class SBMLLocalParameter extends SBMLSBaseEntity {

	private double value;
	
	@Relationship(type = "DEFINED_BY", direction = Relationship.OUTGOING)
	private SBMLUnitDefinition units;

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public SBMLUnitDefinition getUnits() {
		return units;
	}

	public void setUnits(SBMLUnitDefinition units) {
		this.units = units;
	}
}
