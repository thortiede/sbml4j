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
package org.sbml4j.model.warehouse;

public class PathwayNode extends WarehouseGraphNode {

	private String pathwayIdString;
	
	private String pathwayNameString;

	public String getPathwayIdString() {
		return pathwayIdString;
	}

	public void setPathwayIdString(String pathwayIdString) {
		this.pathwayIdString = pathwayIdString;
	}

	public String getPathwayNameString() {
		return pathwayNameString;
	}

	public void setPathwayNameString(String pathwayNameString) {
		this.pathwayNameString = pathwayNameString;
	}
	
}
