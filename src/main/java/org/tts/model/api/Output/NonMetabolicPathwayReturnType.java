package org.tts.model.api.Output;

import org.springframework.data.neo4j.annotation.QueryResult;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.simple.SBMLSimpleTransition;

@QueryResult
public class NonMetabolicPathwayReturnType {
	/*
	 * match (p:PathwayNode)-[:Warehouse {warehouseGraphEdgeType:"CONTAINS"}]-(t:SBMLSimpleTransition) where p.entityUUID="01b50026-4f46-429b-b5e6-5a6fe8b90f2a" and t.entityUUID = "d7d3fd93-7ae6-413e-8ad7-2ac0df2a9b4b" with t match (e1:ExternalResourceEntity)-[:BQ]-(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)-[tr1:IS_INPUT]-(t)-[tr2:IS_OUTPUT]-(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)-[:BQ]-(e2:ExternalResourceEntity) where e1.type = "KEGGGENES" and e2.type = "KEGGGENES" return s1, t, s2
	 * 
	 */
	
	private SBMLSpecies inputSpecies;
	private SBMLSpecies outputSpecies;
	private SBMLSimpleTransition transition;
	
	public SBMLSpecies getInputSpecies() {
		return inputSpecies;
	}
	public void setInputSpecies(SBMLSpecies inputSpecies) {
		this.inputSpecies = inputSpecies;
	}
	public SBMLSpecies getOutputSpecies() {
		return outputSpecies;
	}
	public void setOutputSpecies(SBMLSpecies outputSpecies) {
		this.outputSpecies = outputSpecies;
	}
	public SBMLSimpleTransition getTransition() {
		return transition;
	}
	public void setTransition(SBMLSimpleTransition transition) {
		this.transition = transition;
	} 
	
	
}
