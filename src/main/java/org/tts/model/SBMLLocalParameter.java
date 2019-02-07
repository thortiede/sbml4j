package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;

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
