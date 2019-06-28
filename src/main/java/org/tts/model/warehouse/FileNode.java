package org.tts.model.common;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label="SBMLFile")
public class SBMLFile extends GraphBaseEntity {

	private String filename;
	
	private String filecontent;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilecontent() {
		return filecontent;
	}

	public void setFilecontent(String filecontent) {
		this.filecontent = filecontent;
	}
	
}
