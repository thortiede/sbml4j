package org.tts.model.full;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLSBaseEntity;

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
