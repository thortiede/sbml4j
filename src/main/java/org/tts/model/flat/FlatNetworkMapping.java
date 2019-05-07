package org.tts.model.flat;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.GraphBaseEntity;

public class FlatNetworkMapping extends GraphBaseEntity {

	@Relationship(type = "CONTAINS")
	private List<FlatSpecies> flatSpeciesList;
	
	private String[] relationshipTypes;
	
	private String[] nodeTypes;
	
	private String networkType;

	public List<FlatSpecies> getFlatSpeciesList() {
		return flatSpeciesList;
	}

	public void setFlatSpeciesList(List<FlatSpecies> flatSpeciesList) {
		this.flatSpeciesList = flatSpeciesList;
	}

	public boolean addFlatSpecies(FlatSpecies fs) {
		try {
			if(this.flatSpeciesList == null) {
				this.flatSpeciesList = new ArrayList<>();
			}
			if(!this.flatSpeciesList.contains(fs)) {
				flatSpeciesList.add(fs);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String[] getRelationshipTypes() {
		return relationshipTypes;
	}

	public void setRelationshipTypes(String[] relationshipTypes) {
		this.relationshipTypes = relationshipTypes;
	}
	

	public String[] getNodeTypes() {
		return nodeTypes;
	}

	public void setNodeTypes(String[] nodeTypes) {
		this.nodeTypes = nodeTypes;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
}
