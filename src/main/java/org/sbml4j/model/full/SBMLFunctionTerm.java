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
package org.sbml4j.model.full;

import org.sbml4j.model.common.SBMLSBaseEntity;

public class SBMLFunctionTerm extends SBMLSBaseEntity {
	
	private boolean isDefaultTerm;
	
	private int resultLevel;
	
	/**
	 * In SBML the FunctionTerm element has a Math element
	 * which consists of a MathML structured content.
	 * It describes a boolean function to determine whether this
	 * term is selected and thus the resultLevel used.
	 * At this point we capture this content in a String variable.
	 * In the future this might be expanded to it's own element
	 * in which the references to other objects are resolved
	 */
	private String math;

	public boolean isDefaultTerm() {
		return isDefaultTerm;
	}

	public void setDefaultTerm(boolean isDefaultTerm) {
		this.isDefaultTerm = isDefaultTerm;
	}

	public int getResultLevel() {
		return resultLevel;
	}

	public void setResultLevel(int resultLevel) {
		this.resultLevel = resultLevel;
	}

	public String getMath() {
		return math;
	}

	public void setMath(String math) {
		this.math = math;
	}
}
