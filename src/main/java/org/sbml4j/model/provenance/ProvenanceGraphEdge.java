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
package org.sbml4j.model.provenance;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphEdgeType;

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
