package org.tts.model;

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
