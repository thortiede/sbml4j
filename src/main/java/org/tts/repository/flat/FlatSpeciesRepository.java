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

	//match (fs:FlatSpecies) where fs.entityUUID = "5ad2ad42-f546-43be-a7cc-5f5225e309cf" call apoc.path.subgraphNodes(fs,{maxlevel:3, labelFilter:'FlatSpecies', relationshipFilter:'inhibition|stimulation'}) yield node as n return n;
	
	@Query(value="MATCH (fs:FlatSpecies {entityUUID : {0}}) call apoc.path.expand(fs, {1}, \"+FlatSpecies\", {2}, {3}) yield path as pp return nodes(pp), relationships(pp);")
	//@Query(value="MATCH (fs:FlatSpecies {entityUUID : {0}}) call apoc.path.subgraphNodes(fs,{maxLevel:{3}, relationshipFilter:{1}, labelFilter:\"+FlatSpecies\"}) yield node as n return n;")
	List<FlatSpecies> findNetworkContext(String startNodeUUID, String relationTypesApocString,
			int minPathLength, int maxPathLength);

	@Query(value="MATCH (fs:FlatSpecies {entityUUID : {0}}) call apoc.path.expand(fs, {1}, {2}, {3}, {4}) yield path as pp return nodes(pp), relationships(pp);")
	List<FlatSpecies> findNetworkContext(String startNodeUUID, String relationTypesApocString, String nodeFilterString,
			int minPathLength, int maxPathLength);

	@Query(value="MATCH "
			+ "(m:MappingNode{entityUUID: {0}})"
			+ "-[:Warehouse {warehouseGraphEdgeType: \"CONTAINS\"}]-"
			+ "(fs:FlatSpecies) "
			+ "WHERE fs.symbol=$symbol "
			+ "RETURN fs.entityUUID")
	String findStartNodeEntityUUID(String networkUUID, String symbol);

}
