package org.tts.model.warehouse;

import java.util.Map;

import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.Organism;
import org.tts.model.provenance.ProvenanceGraphEntityNode;

public class WarehouseGraphNode extends ProvenanceGraphEntityNode {

	
	@Properties
	private Map<String, Object> warehouse;
	
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
