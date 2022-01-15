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
package org.sbml4j.model.queryResult;

import org.sbml4j.model.common.SBMLSpecies;
import org.sbml4j.model.simple.SBMLSimpleReaction;
import org.springframework.data.neo4j.annotation.QueryResult;

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
