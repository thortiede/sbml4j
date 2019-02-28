package org.tts.model;

public class SBMLConstraint extends SBMLSBaseEntity {

	/**
	 * In SBML the KineticLaw element can have up to one Math element
	 * which consists of a MathML structured content.
	 * At this point we capture this content in a String variable.
	 * In the future this might be expanded to it's own element
	 * in which the references to other objects are resolved
	 */
	private String math;
	
	private String message;

	public String getMath() {
		return math;
	}

	public void setMath(String math) {
		this.math = math;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
