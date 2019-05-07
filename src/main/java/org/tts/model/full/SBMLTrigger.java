package org.tts.model;

public class SBMLTrigger extends SBMLSBaseEntity {

	private boolean initialValue;
	
	private boolean persitent;
	
	/**
	 * In SBML the Trigger element can have up to one Math element
	 * which consists of a MathML structured content.
	 * At this point we capture this content in a String variable.
	 * In the future this might be expanded to it's own element
	 * in which the references to other objects are resolved
	 */
	private String math;

	public boolean isInitialValue() {
		return initialValue;
	}

	public void setInitialValue(boolean initialValue) {
		this.initialValue = initialValue;
	}

	public boolean isPersitent() {
		return persitent;
	}

	public void setPersitent(boolean persitent) {
		this.persitent = persitent;
	}

	public String getMath() {
		return math;
	}

	public void setMath(String math) {
		this.math = math;
	}
}
