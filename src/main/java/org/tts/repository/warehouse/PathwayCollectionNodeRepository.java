package org.tts.repository.warehouse;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.PathwayCollectionNode;

public interface PathwayCollectionNodeRepository extends Neo4jRepository<PathwayCollectionNode, Long> {

	public Iterable<PathwayCollectionNode> findAllByPathwayCollectionName(String pathwayCollectionName);
	
}
