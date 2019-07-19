package org.tts.model.provenance;

import org.neo4j.ogm.annotation.NodeEntity;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;

@NodeEntity("PROV_ACTIVITY")
public class ProvenanceGraphActivityNode extends ProvenanceEntity{

	private ProvenanceGraphActivityType graphActivityType;
	
	private String graphActivityName;

	public ProvenanceGraphActivityType getGraphActivityType() {
		return graphActivityType;
	}

	public void setGraphActivityType(ProvenanceGraphActivityType graphActivityType) {
		this.graphActivityType = graphActivityType;
	}

	public String getGraphActivityName() {
		return graphActivityName;
	}

	public void setGraphActivityName(String graphActivityName) {
		this.graphActivityName = graphActivityName;
	}
}
