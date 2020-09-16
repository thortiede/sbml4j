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

public class PathwayCollectionNode extends WarehouseGraphNode {

	private String pathwayCollectionName;
	
	private String pathwayCollectionDescription;

	public String getPathwayCollectionName() {
		return pathwayCollectionName;
	}

	public void setPathwayCollectionName(String pathwayCollectionName) {
		this.pathwayCollectionName = pathwayCollectionName;
	}

	public String getPathwayCollectionDescription() {
		return pathwayCollectionDescription;
	}

	public void setPathwayCollectionDescription(String pathwayCollectionDescription) {
		this.pathwayCollectionDescription = pathwayCollectionDescription;
	}
}
