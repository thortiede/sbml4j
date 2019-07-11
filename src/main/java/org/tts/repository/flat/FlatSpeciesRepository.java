package org.tts.repository.flat;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.flat.FlatSpecies;

public interface FlatSpeciesRepository extends Neo4jRepository<FlatSpecies, Long> {

	FlatSpecies findByEntityUUID(String entityUUID);

	
	@Query(value= "MATCH "
			+ "(m:MappingNode {entityUUID: {0}})"
			+ "-[:Warehouse {warehouseGraphEdgeType: \"CONTAINS\"}]-"
			+ "(fs:FlatSpecies)-[r]-(fs2:FlatSpecies)"
			+ "-[:Warehouse {warehouseGraphEdgeType: \"CONTAINS\"}]-"
			+ "(m:MappingNode {entityUUID: {0}})"
			+ "RETURN fs, r, fs2")
	List<FlatSpecies> findAllNetworkNodes(String entityUUID);


	@Query(value="MATCH (fs:FlatSpecies {entityUUID : {0}}) call apoc.path.expand(fs, {1}, \"+FlatSpecies\", {2}, {3}) yield path as pp return pp;")
	List<FlatSpecies> findNetworkContext(String startNodeUUID, String relationTypesApocString,
			int minPathLength, int maxPathLength);

	

}
