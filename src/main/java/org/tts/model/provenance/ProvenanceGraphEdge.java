package org.tts.model.provenance;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;

@RelationshipEntity(type="PROV")
public class ProvenanceGraphEdge extends ProvenanceEntity {

	@StartNode
	private ProvenanceEntity startNode;
	
	@EndNode
	private ProvenanceEntity endNode;
	
	private ProvenanceGraphEdgeType provenanceGraphEdgeType;

	public ProvenanceGraphEdgeType getProvenanceGraphEdgeType() {
		return provenanceGraphEdgeType;
	}

	public void setProvenanceGraphEdgeType(ProvenanceGraphEdgeType provenanceGraphEdgeType) {
		this.provenanceGraphEdgeType = provenanceGraphEdgeType;
	}

	public ProvenanceEntity getStartNode() {
		return startNode;
	}

	public void setStartNode(ProvenanceEntity startNode) {
		this.startNode = startNode;
	}

	public ProvenanceEntity getEndNode() {
		return endNode;
	}

	public void setEndNode(ProvenanceEntity endNode) {
		this.endNode = endNode;
	}
	
	
}
