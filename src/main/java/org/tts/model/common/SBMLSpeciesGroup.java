package org.tts.model.common;

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
