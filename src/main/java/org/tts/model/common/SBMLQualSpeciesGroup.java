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
