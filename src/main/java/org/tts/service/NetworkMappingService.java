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
package org.tts.service;

import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;

public interface NetworkMappingService {
	
	public MappingNode createMappingFromPathway(PathwayNode pathway, NetworkMappingType type, ProvenanceGraphActivityNode activityNode, ProvenanceGraphAgentNode agentNode) throws Exception;
	
}
