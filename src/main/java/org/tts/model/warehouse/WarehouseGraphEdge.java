package org.tts.model.warehouse;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;

@RelationshipEntity("Warehouse")
public class WarehouseGraphEdge extends ProvenanceEntity {

	@StartNode
	private ProvenanceEntity startNode;
	
	@EndNode
	private ProvenanceEntity endNode;
	
	private WarehouseGraphEdgeType warehouseGraphEdgeType;

	public WarehouseGraphEdgeType getWarehouseGraphEdgeType() {
		return warehouseGraphEdgeType;
	}

	public void setWarehouseGraphEdgeType(WarehouseGraphEdgeType warehouseGraphEdgeType) {
		this.warehouseGraphEdgeType = warehouseGraphEdgeType;
	}

	public ProvenanceEntity getStartNode() {
		return startNode;
	}

	public void setStartNode(ProvenanceEntity warehouseGraphStartNode) {
		this.startNode = warehouseGraphStartNode;
	}

	public ProvenanceEntity getEndNode() {
		return endNode;
	}

	public void setEndNode(ProvenanceEntity warehouseGraphEndNode) {
		this.endNode = warehouseGraphEndNode;
	}
	
	
	
}
