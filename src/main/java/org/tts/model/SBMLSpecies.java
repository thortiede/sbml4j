package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLSpecies extends SBMLCompartmentalizedSBaseEntity {

	private double initialAmount;
	
	private double initialConcentration;
	
	/**
	 * The attribute substanceUnits can either point to a predifined unit
	 * from the Enum Unit.Kind (recommended are 
	 * mole, item, dimensionless, kilogram, gram)
	 * or derived units from those.
	 * To be able to model those different elements in the graph, it is recommended
	 * to have standard SBMLUnitDefiniton Entities in the graph
	 * for all elements in the Enum Unit.Kind. 
	 * Then the attribute {@code substanceUnits} can always refer to a node
	 * of type SBMLUnitDefinition whether it is a perdefined or composite
	 * unit definition
	 */
	@Relationship(type="HAS", direction = Relationship.OUTGOING)
	private SBMLUnitDefinition substanceUnits;
	
	private boolean hasOnlySubstanceUnits;
	
	private boolean boundaryCondition;
	
	private boolean constant;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private SBMLParameter conversionFactor;

	public double getInitialAmount() {
		return initialAmount;
	}

	public void setInitialAmount(double initialAmount) {
		this.initialAmount = initialAmount;
	}

	public double getInitialConcentration() {
		return initialConcentration;
	}

	public void setInitialConcentration(double initialConcentration) {
		this.initialConcentration = initialConcentration;
	}

	public SBMLUnitDefinition getSubstanceUnits() {
		return substanceUnits;
	}

	public void setSubstanceUnits(SBMLUnitDefinition substanceUnits) {
		this.substanceUnits = substanceUnits;
	}

	public boolean isHasOnlySubstanceUnits() {
		return hasOnlySubstanceUnits;
	}

	public void setHasOnlySubstanceUnits(boolean hasOnlySubstanceUnits) {
		this.hasOnlySubstanceUnits = hasOnlySubstanceUnits;
	}

	public boolean isBoundaryCondition() {
		return boundaryCondition;
	}

	public void setBoundaryCondition(boolean boundaryCondition) {
		this.boundaryCondition = boundaryCondition;
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public SBMLParameter getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(SBMLParameter conversionFactor) {
		this.conversionFactor = conversionFactor;
	}
	
}
