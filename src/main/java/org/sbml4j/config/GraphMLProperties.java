/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.graphml")
public class GraphMLProperties {
	private String speciesSymbolKey;
	
	private String relationshipSymbolKey;

	private String sboStringKey;
	
	public String getSpeciesSymbolKey() {
		return speciesSymbolKey;
	}

	public void setSpeciesSymbolKey(String speciesSymbolKey) {
		this.speciesSymbolKey = speciesSymbolKey;
	}

	public String getSboStringKey() {
		return sboStringKey;
	}

	public void setSboStringKey(String sboStringKey) {
		this.sboStringKey = sboStringKey;
	}

	public String getRelationshipSymbolKey() {
		return relationshipSymbolKey;
	}

	public void setRelationshipSymbolKey(String relationshipSymbolKey) {
		this.relationshipSymbolKey = relationshipSymbolKey;
	}

	

}
