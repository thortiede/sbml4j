package org.tts.repository.flat;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.tts.model.flat.FlatNetworkMapping;

@Repository
public interface FlatNetworkMappingRepository extends Neo4jRepository<FlatNetworkMapping, Long> {

	FlatNetworkMapping findByEntityUUID(String entityUUID);

	FlatNetworkMapping findByEntityUUID(String mappingUuid, @Depth int depth);

}
