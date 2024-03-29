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
