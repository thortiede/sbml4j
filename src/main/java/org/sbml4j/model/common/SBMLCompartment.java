/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.model.common;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.warehouse.DatabaseNode;

/**
 * Class SBMLCompartment
 * Modeled after the Compartment class from the SBML Standard level-3 version-2
 * and modified to allow storage in a Graph database
 * 
 * This class is derived from {@link org.tts.model.common.SBMLSBaseEntity}
 * The inherited attribute {@code sbaseId} is required for this class
 * 
 * The boolean attribute {@code constant} is required by the compartment entity in SBML
 * All other attributes are optional.
 * @author Thorsten Tiede
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
	
	@Relationship(type="OF", direction = Relationship.OUTGOING)
	private DatabaseNode database;
	
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

	public DatabaseNode getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseNode database) {
		this.database = database;
	}	
}
