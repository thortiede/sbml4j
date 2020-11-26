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
package org.tts.model.common;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLQualSpeciesGroup extends SBMLQualSpecies {
	@Relationship(type = "HAS_GROUP_MEMBER")
	List<SBMLQualSpecies> qualSpeciesInGroup;

	public List<SBMLQualSpecies> getQualSpeciesInGroup() {
		return qualSpeciesInGroup;
	}

	public void setQualSpeciesInGroup(List<SBMLQualSpecies> qualSpeciesInGroup) {
		this.qualSpeciesInGroup = qualSpeciesInGroup;
	}

	public void addQualSpeciesToGroup(SBMLQualSpecies qualSpecies) {
		if (this.qualSpeciesInGroup == null) {
			this.qualSpeciesInGroup = new ArrayList<>();
		}
		this.qualSpeciesInGroup.add(qualSpecies);
	}
}
