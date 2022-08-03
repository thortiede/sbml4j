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
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphAgentType;

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
