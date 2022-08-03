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

import org.sbml4j.service.json.ProvenanceMetaDataNodeSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ProvenanceMetaDataNodeSerializer.class)
public class ProvenanceMetaDataNode extends ProvenanceEntity {

	private String provenanceName;

	public String getProvenanceName() {
		return provenanceName;
	}

	public void setProvenanceName(String provenanceName) {
		this.provenanceName = provenanceName;
	}
	
	
}
