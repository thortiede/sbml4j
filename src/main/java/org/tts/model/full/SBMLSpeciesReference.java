package org.tts.model.full;

public class SBMLSpeciesReference extends SBMLSimpleSpeciesReference {

	private double stoichiometry;
	
	private boolean constant;

	public double getStoichiometry() {
		return stoichiometry;
	}

	public void setStoichiometry(double stoichiometry) {
		this.stoichiometry = stoichiometry;
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}
}
