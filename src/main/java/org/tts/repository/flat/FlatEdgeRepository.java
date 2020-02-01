package org.tts.repository.flat;


import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.flat.FlatEdge;

public interface FlatEdgeRepository extends Neo4jRepository<FlatEdge, Long> {

	FlatEdge findByEntityUUID(String entityUUID);

	
}
