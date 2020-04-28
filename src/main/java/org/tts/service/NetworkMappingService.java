package org.tts.service;

import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;

public interface NetworkMappingService {
	
	public MappingNode createMappingFromPathway(PathwayNode pathway, NetworkMappingType type, IDSystem idSystem, ProvenanceGraphActivityNode activityNode, ProvenanceGraphAgentNode agentNode) throws Exception;
	
}
