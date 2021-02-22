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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Transient;
import org.tts.model.common.ContentGraphNode;


public class FlatSpecies extends ContentGraphNode {

	private String simpleModelEntityUUID;
	
	private String symbol;
	
	private String secondaryNames;
	
	@Transient
	private Set<String> secondaryNamesSet;
	
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
	
	public String getSecondaryNames() {
		return secondaryNames;
	}

	public void setSecondaryNames(String secondaryNames) {
		this.secondaryNames = secondaryNames;
		this.secondaryNamesSet = new HashSet<>();
		for (String name : secondaryNames.split(", ")) {
			secondaryNamesSet.add(name);
		}
	}

	public boolean addSecondaryName(String secondaryName) {
		if (this.secondaryNamesSet == null) {
			this.secondaryNamesSet = new HashSet<>();
		}
		if (this.secondaryNames == null) {
			this.secondaryNames = secondaryName;
			this.secondaryNamesSet.add(secondaryName);
			return true;
		} else {
			for (String name : this.secondaryNames.split(",")) {
				this.secondaryNamesSet.add(name.strip());
			}
			if (this.secondaryNamesSet.add(secondaryName.strip())) {
				if (!this.secondaryNames.isBlank()) {
					this.secondaryNames += ", ";
				}
				this.secondaryNames += secondaryName.strip();
				return true;
			} else {
				return false;
			}
		}
	}
	
	public String getSboTerm() {
		return sboTerm;
	}

	public void setSboTerm(String sboTerm) {
		this.sboTerm = sboTerm;
	}	 
}
