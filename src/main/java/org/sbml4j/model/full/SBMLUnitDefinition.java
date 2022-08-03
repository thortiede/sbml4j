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

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.sbml.SBMLSBaseEntity;

public class SBMLUnitDefinition extends SBMLSBaseEntity {

	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	List<SBMLUnit> units;

	public List<SBMLUnit> getUnits() {
		return units;
	}

	public void setUnits(List<SBMLUnit> units) {
		this.units = units;
	}
	
}
