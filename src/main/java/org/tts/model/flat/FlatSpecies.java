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
package org.tts.model.flat;

import org.tts.model.common.ContentGraphNode;


public class FlatSpecies extends ContentGraphNode {

	private String simpleModelEntityUUID;
	
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
