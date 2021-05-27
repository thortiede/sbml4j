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
package org.sbml4j.model.common;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLSpeciesGroup extends SBMLSpecies {

	@Relationship(type = "HAS_GROUP_MEMBER")
	List<SBMLSpecies> speciesInGroup;

	public List<SBMLSpecies> getSpeciesInGroup() {
		return speciesInGroup;
	}

	public void setSpeciesInGroup(List<SBMLSpecies> speciesInGroup) {
		this.speciesInGroup = speciesInGroup;
	}
	
	public void addSpeciesToGroup(SBMLSpecies species) {
		if (this.speciesInGroup == null) {
			this.speciesInGroup = new ArrayList<>();
		}
		this.speciesInGroup.add(species);
	}
}
