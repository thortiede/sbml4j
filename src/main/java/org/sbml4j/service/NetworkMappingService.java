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
package org.sbml4j.service;

import org.sbml4j.Exception.NetworkAlreadyExistsException;
import org.sbml4j.model.common.GraphEnum.NetworkMappingType;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.model.warehouse.PathwayNode;

public interface NetworkMappingService {
	
	public MappingNode createMappingFromPathway(PathwayNode pathway, 
												NetworkMappingType type,
												ProvenanceGraphActivityNode activityNode,
												ProvenanceGraphAgentNode agentNode,
												String newMappingName) throws NetworkAlreadyExistsException, Exception;
	
}
