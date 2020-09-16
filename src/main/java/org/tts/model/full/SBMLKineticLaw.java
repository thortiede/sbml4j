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

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLSBaseEntity;

public class SBMLKineticLaw extends SBMLSBaseEntity {
	
	/**
	 * In SBML the KineticLaw element can have up to one Math element
	 * which consists of a MathML structured content.
	 * At this point we capture this content in a String variable.
	 * In the future this might be expanded to it's own element
	 * in which the references to other objects are resolved
	 */
	private String math;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLLocalParameter> localParameters;

	public String getMath() {
		return math;
	}

	public void setMath(String math) {
		this.math = math;
	}

	public List<SBMLLocalParameter> getLocalParameters() {
		return localParameters;
	}

	public void setLocalParameters(List<SBMLLocalParameter> localParameters) {
		this.localParameters = localParameters;
	}
	
}
