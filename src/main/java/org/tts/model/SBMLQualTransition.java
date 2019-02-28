package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLQualTransition extends SBMLSBaseEntity {

	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLQualInput> inputs;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLQualOutput> outputs;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLFunctionTerm> functionTerms;

	public List<SBMLQualInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<SBMLQualInput> inputs) {
		this.inputs = inputs;
	}

	public List<SBMLQualOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<SBMLQualOutput> outputs) {
		this.outputs = outputs;
	}

	public List<SBMLFunctionTerm> getFunctionTerms() {
		return functionTerms;
	}

	public void setFunctionTerms(List<SBMLFunctionTerm> functionTerms) {
		this.functionTerms = functionTerms;
	}
	
}
