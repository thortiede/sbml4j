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

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Labels;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity("PROV_ENTITY")
public class ProvenanceGraphEntityNode extends ProvenanceEntity {
	
	@Labels
	private List<String> labels = new ArrayList<>();
	
	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	
	public boolean addLabel(String newLabel) {
		try {
			if(!labels.contains(newLabel))
				this.labels.add(newLabel);
		} catch (Exception e) {
			// element could not be added
			return false;
		}
		return true;
	}
	
	public boolean removeLabel(String name) {
		try {
			this.labels.remove(name);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
