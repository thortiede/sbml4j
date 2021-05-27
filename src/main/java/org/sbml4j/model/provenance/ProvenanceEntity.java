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

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.Properties;
import org.sbml4j.model.common.GraphBaseEntity;


public class ProvenanceEntity extends GraphBaseEntity {

	
	/** add a Label to the labels array in GraphBaseEntity for:
	 	Node Type or Edge Type of ProvenanceEntity Graph
			Nodes: Entity, Activity, Agent
			Edges: used, wasGeneratedBy, wasAssociatedWith, wasAttributedTo, wasDerivedFrom
	**/
	
	@Properties
	private Map<String, Object> provenance = new HashMap<>();

	public Map<String, Object> getProvenance() {
		return provenance;
	}

	public void setProvenance(Map<String, Object> provenance) {
		this.provenance = provenance;
	}
	
	public void addProvenance(String key, Object value) {
		if (!this.provenance.containsKey(key)) {
			this.provenance.put(key, value);
		}
	}
	public void addProvenance(Map<String, Object> provenanceMap) {
		for (String key : provenanceMap.keySet()) {
			addProvenance(key, provenanceMap.get(key));
		}
	}
	
}
