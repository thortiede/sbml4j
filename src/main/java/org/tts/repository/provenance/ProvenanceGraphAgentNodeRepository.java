package org.tts.repository.provenance;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.provenance.ProvenanceGraphAgentNode;

public interface ProvenanceGraphAgentNodeRepository extends Neo4jRepository<ProvenanceGraphAgentNode, Long> {

	public ProvenanceGraphAgentNode findByGraphAgentTypeAndGraphAgentName(ProvenanceGraphAgentType graphAgentType, String graphAgentName);
	
}
