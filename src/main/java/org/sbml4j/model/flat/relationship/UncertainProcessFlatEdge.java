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
package org.sbml4j.model.flat.relationship;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.sbml4j.model.flat.FlatEdge;

@RelationshipEntity(type="UNCERTAINPROCESS")
public class UncertainProcessFlatEdge extends FlatEdge {
	@Override
	public String getTypeString() {
		return "UNCERTAINPROCESS";
	}
}
