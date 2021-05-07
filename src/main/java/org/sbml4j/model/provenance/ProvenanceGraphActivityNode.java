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

import org.neo4j.ogm.annotation.NodeEntity;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphActivityType;

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
