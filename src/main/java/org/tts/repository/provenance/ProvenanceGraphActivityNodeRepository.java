package org.tts.repository.provenance;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.provenance.ProvenanceGraphActivityNode;

public interface ProvenanceGraphActivityNodeRepository extends Neo4jRepository<ProvenanceGraphActivityNode, Long> {

	ProvenanceGraphActivityNode findByGraphActivityTypeAndGraphActivityName(ProvenanceGraphActivityType graphActivityType, String graphActivityName);

}
