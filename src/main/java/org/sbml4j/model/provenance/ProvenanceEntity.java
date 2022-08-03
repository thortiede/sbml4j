/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.model.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.base.GraphBaseEntity;


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
	
	
	@Relationship(type="PROV_SUBELEMENT", direction = Relationship.OUTGOING)
	private List<ProvenanceMetaDataNode> provenanceAnnotationSubelements;

	/**
	 * @return the provenanceAnnotationSubelements
	 */
	public List<ProvenanceMetaDataNode> getProvenanceAnnotationSubelements() {
		return provenanceAnnotationSubelements;
	}

	/**
	 * @param provenanceAnnotationSubelements the provenanceAnnotationSubelements to set
	 */
	public void setProvenanceAnnotationSubelements(List<ProvenanceMetaDataNode> provenanceAnnotationSubelements) {
		this.provenanceAnnotationSubelements = provenanceAnnotationSubelements;
	}
	
	public ProvenanceEntity addProvenanceAnnotationSubelement(ProvenanceMetaDataNode subelement) {
		if (this.provenanceAnnotationSubelements == null) {
			this.provenanceAnnotationSubelements = new ArrayList<>();
		}
		this.provenanceAnnotationSubelements.add(subelement);
		return this;
	}
	
	
}
