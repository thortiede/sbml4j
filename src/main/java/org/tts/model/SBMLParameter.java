package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;

/**
 * The SBMLParamter class models parameters from the SBML Standard as nodes
 * 
 * For parameters the inherited attribute {@code sbaseId} from SBMLSBaseEntity
 * is mandatory.
 * @author ttiede
 *
 */
public class SBMLParameter extends SBMLSBaseEntity {

	private double value;
	
	private boolean constant;
	
	@Relationship(type = "DEFINED_BY", direction = Relationship.OUTGOING)
	private SBMLUnitDefinition unitDefintion;

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public SBMLUnitDefinition getUnitDefintion() {
		return unitDefintion;
	}

	public void setUnitDefintion(SBMLUnitDefinition unitDefintion) {
		this.unitDefintion = unitDefintion;
	}
	
}
