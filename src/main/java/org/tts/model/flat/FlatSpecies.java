package org.tts.model.flat;

import org.tts.model.common.ContentGraphNode;


public class FlatSpecies extends ContentGraphNode {

	private String simpleModelEntityUUID; // maybe use ProvenanceEdge wasDerivedFrom to link back to the Entity in the simpleModel?
	
	private String symbol;
	
	private String sboTerm;
	
	 public String getSimpleModelEntityUUID() {
		return simpleModelEntityUUID;
	}

	public void setSimpleModelEntityUUID(String simpleModelEntityUUID) {
		this.simpleModelEntityUUID = simpleModelEntityUUID;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSboTerm() {
		return sboTerm;
	}

	public void setSboTerm(String sboTerm) {
		this.sboTerm = sboTerm;
	}	 
}
