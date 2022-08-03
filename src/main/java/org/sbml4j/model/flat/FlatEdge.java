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
package org.sbml4j.model.flat;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.sbml4j.model.contentgraph.ContentGraphEdge;

@RelationshipEntity
public class FlatEdge extends ContentGraphEdge {

	private String symbol;
	
	@StartNode
	private FlatSpecies inputFlatSpecies;
	
	@EndNode
	private FlatSpecies outputFlatSpecies;
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public FlatSpecies getInputFlatSpecies() {
		return inputFlatSpecies;
	}

	public void setInputFlatSpecies(FlatSpecies inputFlatSpecies) {
		this.inputFlatSpecies = inputFlatSpecies;
	}

	public FlatSpecies getOutputFlatSpecies() {
		return outputFlatSpecies;
	}

	public void setOutputFlatSpecies(FlatSpecies outputFlatSpecies) {
		this.outputFlatSpecies = outputFlatSpecies;
	}
	
	public String getTypeString() {
		return "FLATEDGE";
	}
	
	public boolean equals(FlatEdge other) {
		return this.getEntityUUID().equals(other.getEntityUUID());
	}

}
