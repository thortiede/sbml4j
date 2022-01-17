/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.model.warehouse;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.sbml4j.model.base.GraphEnum.WarehouseGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;

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
