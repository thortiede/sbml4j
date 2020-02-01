package org.tts.model.api.Output;

import org.springframework.data.neo4j.annotation.QueryResult;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.simple.SBMLSimpleReaction;

@QueryResult
public class MetabolicPathwayReturnType {

	/* match (p:PathwayNode)-[:Warehouse {warehouseGraphEdgeType:"CONTAINS"}]->(s:SBMLSimpleReaction) 
	 * where p.entityUUID = "966b64d1-1e5e-4cc1-aac0-d24bbd58142e" 
	 * with s 
	 * match (spec:SBMLSpecies)<-[r:IS_PRODUCT|IS_REACTANT|IS_CATALYST]-(s) 
	 * return spec, type(r), s */
	
	
	private SBMLSpecies species;
	private String typeOfRelation;
	private SBMLSimpleReaction reaction;

	public SBMLSpecies getSpecies() {
		return species;
	}
	public void setSpecies(SBMLSpecies species) {
		this.species = species;
	}
	public String getTypeOfRelation() {
		return typeOfRelation;
	}
	public void setTypeOfRelation(String typeOfRelation) {
		this.typeOfRelation = typeOfRelation;
	}
	public SBMLSimpleReaction getReaction() {
		return reaction;
	}
	public void setReaction(SBMLSimpleReaction reaction) {
		this.reaction = reaction;
	}
}
