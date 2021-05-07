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
package org.tts.model.warehouse;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.Organism;
import org.tts.model.provenance.ProvenanceGraphEntityNode;

public class WarehouseGraphNode extends ProvenanceGraphEntityNode {

	
	@Properties
	private Map<String, Object> warehouse;
	
	public Map<String, Object> getWarehouse() {
		return this.warehouse;
	}

	public void addWarehouseAnnotation(Map<String, Object> warehouse) {
		if(this.warehouse == null) {
			this.warehouse = new HashMap<>();
		}
		for (String key : warehouse.keySet()) {
			this.warehouse.put(key, warehouse.get(key));
		}
	}
	
	public boolean addWarehouseAnnotation(String warehouseKey, Object warehouseValue) {
		if(this.warehouse == null) {
			this.warehouse = new HashMap<>();
		}
		try {
			this.warehouse.put(warehouseKey, warehouseValue);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Relationship(type="FOR")
	private Organism organism;

	public Organism getOrganism() {
		return organism;
	}

	public void setOrganism(Organism organism) {
		this.organism = organism;
	}
	
	
	// All these nodes should be of ProvenanceType Entity, as we create Agents and Activities separately and
	// WarehouseGraphNodes are always Entities as can be other nodes such as the File Node.
	

/*
	public WarehouseGraphNodeType getWarehouseGraphNodeType() {
		return warehouseGraphNodeType;
	}


	public void setWarehouseGraphNodeType(WarehouseGraphNodeType type) {
		if(this.warehouseGraphNodeType != null) {
			if(!this.warehouseGraphNodeType.equals(type)) {
				// type was already set, is now reset to different type
				// remove old label
				super.removeLabel(type.name());
			}
		}
		this.warehouseGraphNodeType = type;
		super.addLabel(type.name());
	}
	*/
}
