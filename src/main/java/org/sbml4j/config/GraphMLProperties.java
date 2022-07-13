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
