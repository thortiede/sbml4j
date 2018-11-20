package org.tts.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;


@NodeEntity
public class GraphSBase {

	@Id @GeneratedValue
	private Long id = null;

	@Version
	private Long version;
	
	protected String sbmlIdString;
	
	protected String sbmlNameString;

	
	@Relationship(type = "IN_MODEL", direction = Relationship.OUTGOING)
	protected List<GraphModel> modelList;
	
	
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
	@JsonIgnore
	public List<GraphModel> getModels() {
		return modelList;
	}

	public void setModel(GraphModel model) {
		if(modelList == null) {
			modelList = new ArrayList<GraphModel>();
		}
		if(!modelList.contains(model))
		{
			this.modelList.add(model);
	
		}
	}
	
}
