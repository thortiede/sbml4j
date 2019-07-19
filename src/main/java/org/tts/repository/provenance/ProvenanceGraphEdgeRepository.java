package org.tts.repository.provenance;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.provenance.ProvenanceGraphEdge;

public interface ProvenanceGraphEdgeRepository extends Neo4jRepository<ProvenanceGraphEdge, Long> {

}
