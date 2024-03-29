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

import org.sbml.jsbml.Unit;
import org.sbml4j.model.sbml.SBMLSBaseEntity;

public class SBMLUnit extends SBMLSBaseEntity {

	private Unit.Kind kind;
	
	private double exponent;
	
	private int scale;
	
	private double multiplier;

	public Unit.Kind getKind() {
		return kind;
	}

	public void setKind(Unit.Kind kind) {
		this.kind = kind;
	}

	public double getExponent() {
		return exponent;
	}

	public void setExponent(double exponent) {
		this.exponent = exponent;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}
}
