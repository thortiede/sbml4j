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
package org.sbml4j.model.full.ext.qual;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.full.SBMLFunctionTerm;
import org.sbml4j.model.sbml.SBMLSBaseEntity;

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
