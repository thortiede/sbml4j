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
package org.tts.model.full;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLSBaseEntity;

/**
 * The SBMLParamter class models parameters from the SBML Standard as nodes
 * 
 * For parameters the inherited attribute {@code sbaseId} from SBMLSBaseEntity
 * is mandatory.
 * @author Thorsten Tiede
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
