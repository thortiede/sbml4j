package org.tts.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Version;

@NodeEntity
public class GraphSBase {

	@Id @GeneratedValue
	private Long id = null;

	@Version
	private Long version;
	
	protected String sbmlIdString;
	
	protected String sbmlNameString;

	public GraphSBase() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getSbmlIdString() {
		return sbmlIdString;
	}

	public void setSbmlIdString(String sbmlIdString) {
		this.sbmlIdString = sbmlIdString;
	}

	public String getSbmlNameString() {
		return sbmlNameString;
	}

	public void setSbmlNameString(String sbmlNameString) {
		this.sbmlNameString = sbmlNameString;
	}
	
	
}
