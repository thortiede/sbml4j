package org.tts.model;

import org.sbml.jsbml.Unit;

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
