package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Class SBMLCompartment
 * Modeled after the Compartment class from the SBML Standard level-3 version-2
 * and modified to allow storage in a Graph database
 * 
 * This class is derived from {@link org.tts.model.SBMLSBaseEntity}
 * The inherited attribute {@code sbaseId} is required for this class
 * 
 * The boolean attribute {@code constant} is required by the compartment entity in SBML
 * All other attributes are optional.
 * @author ttiede
 *
 */
public class SBMLCompartment extends SBMLSBaseEntity {

	private double spatialDimensions;
	
	private double size;
	
	private boolean constant;
	
	/**
	 * Units references a unitDefinition object
	 * Once we build those, we need to change this to a Relationship
	 */
	private String units;
	
	public double getSpatialDimensions() {
		return spatialDimensions;
	}

	public void setSpatialDimensions(double spatialDimensions) {
		this.spatialDimensions = spatialDimensions;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}	
}
