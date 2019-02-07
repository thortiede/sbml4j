package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

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
