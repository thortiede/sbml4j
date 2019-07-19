package org.tts.model.common;

import org.tts.model.warehouse.WarehouseGraphNode;

public class Organism extends WarehouseGraphNode { 
	// not sure this should be derived from WarehouseGraphNode or rather ProvenanceEntity
	// it does not need the connection to the organism as it is the organism
	private String orgCode;
	private String tNumber;
	
	private String longName;
	private String shortName;
	
	private int taxonomyId;

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String gettNumber() {
		return tNumber;
	}

	public void settNumber(String tNumber) {
		this.tNumber = tNumber;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public int getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}
	
}
