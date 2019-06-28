package org.tts.model.provenance;

import org.neo4j.ogm.annotation.NodeEntity;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;

@NodeEntity("PROV_AGENT")
public class ProvenanceGraphAgentNode extends ProvenanceEntity{

	private ProvenanceGraphAgentType graphAgentType;
	
	private String graphAgentName;

	public ProvenanceGraphAgentType getGraphAgentType() {
		return graphAgentType;
	}

	public void setGraphAgentType(ProvenanceGraphAgentType graphAgentType) {
		this.graphAgentType = graphAgentType;
	}

	public String getGraphAgentName() {
		return graphAgentName;
	}

	public void setGraphAgentName(String graphAgentName) {
		this.graphAgentName = graphAgentName;
	}
	
	
}
