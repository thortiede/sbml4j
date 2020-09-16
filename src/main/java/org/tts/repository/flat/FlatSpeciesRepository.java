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
package org.tts.repository.flat;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.flat.FlatSpecies;

public interface FlatSpeciesRepository extends Neo4jRepository<FlatSpecies, Long> {

	FlatSpecies findByEntityUUID(String entityUUID);

	
	@Query(value= "MATCH "
			+ "(m:MappingNode)"
			+ "-[w1:Warehouse]-"
			+ "(fs:FlatSpecies)-[r]-(fs2:FlatSpecies)"
			+ "-[w2:Warehouse]-"
			+ "(m:MappingNode) "
			+ "WHERE m.entityUUID = $entityUUID "
			+ "AND w1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN fs, r, fs2")
	List<FlatSpecies> findAllNetworkNodes(String entityUUID);

	//match (fs:FlatSpecies) where fs.entityUUID = "5ad2ad42-f546-43be-a7cc-5f5225e309cf" call apoc.path.subgraphNodes(fs,{maxlevel:3, labelFilter:'FlatSpecies', relationshipFilter:'inhibition|stimulation'}) yield node as n return n;
	/*
	@Query(value="MATCH (fs:FlatSpecies {entityUUID : {0}}) call apoc.path.expand(fs, {1}, \"+FlatSpecies\", {2}, {3}) yield path as pp return nodes(pp), relationships(pp);")
	//@Query(value="MATCH (fs:FlatSpecies {entityUUID : {0}}) call apoc.path.subgraphNodes(fs,{maxLevel:{3}, relationshipFilter:{1}, labelFilter:\"+FlatSpecies\"}) yield node as n return n;")
	List<FlatSpecies> findNetworkContext(String startNodeUUID, String relationTypesApocString,
			int minPathLength, int maxPathLength);

	@Query(value="MATCH (fs:FlatSpecies {entityUUID : {0}}) call apoc.path.expand(fs, {1}, {2}, {3}, {4}) yield path as pp return nodes(pp), relationships(pp);")
	List<FlatSpecies> findNetworkContext(String startNodeUUID, String relationTypesApocString, String nodeFilterString,
			int minPathLength, int maxPathLength);

	@Query(value="MATCH (fs:FlatSpecies) where fs.entityUUID IN $startNodeSymbols call apoc.path.expand(fs, {1}, {2}, {3}, {4}) yield path as pp return nodes(pp), relationships(pp);")
	List<FlatSpecies> findMultiGeneSubnet(List<String> startNodeSymbols, String relationTypesApocString, String nodeFilterString,
			int minPathLength, int maxPathLength);
	
	@Query(value="MATCH (fs1:FlatSpecies) where fs1.entityUUID = $startNodeEntityUUID with fs1 MATCH (fs2:FlatSpecies) where fs2.entityUUID = $endNodeEntityUUID call apoc.algo.dijkstraWithDefaultWeight(fs1, fs2, {2}, {3}, {4}) yield path as pp, weight as w return nodes(pp), relationships(pp);")
	List<FlatSpecies> apocDijkstraWithDefaultWeight(String startNodeEntityUUID, String endNodeEntityUUID, String relationTypesApocString, String propertyName, float defaultWeight);
	*/
	
	@Query(value="MATCH "
			+ "(m:MappingNode)"
			+ "-[w:Warehouse]-"
			+ "(fs:FlatSpecies) "
			+ "WHERE m.entityUUID = $networkUUID "
			+ "AND fs.symbol = $symbol "
			+ "AND w.warehouseGraphEdgeType=\"CONTAINS\" "
			+ "RETURN fs.entityUUID")
	String findEntityUUIDForSymbolInNetwork(String networkUUID, String symbol);

	@Query(value="MATCH "
			+ "(m:MappingNode)"
			+ "-[w:Warehouse]-"
			+ "(fs:FlatSpecies) "
			+ "WHERE m.entityUUID = $networkUUID "
			+ "AND fs.symbol IN $nodeSymbols  "
			+ "AND w.warehouseGraphEdgeType=\"CONTAINS\" "
			+ "RETURN fs")
	Iterable<FlatSpecies> getNetworkNodes(String networkUUID, List<String> nodeSymbols);
	
	/* potentially viable query to find unconnected nodes in a network
	 * might also need an arrow from flatSpecies to FlatSpecies relation
	 * and then also the back direction
	 */
	@Query(value="MATCH "
			+ "(m:MappingNode)"
			+ "-[w:Warehouse]-"
			+ "(fs:FlatSpecies) "
			+ "WHERE m.entityUUID = $networkUUID "
			+ "AND fs.symbol IN $nodeSymbols  "
			+ "AND w.warehouseGraphEdgeType=\"CONTAINS\" "
			+ "AND NOT EXISTS {"
			+ "(fs)-[]-(fs2:FlatSpecies)<-[w2:Warehouse]-(m) "
			+ "WHERE w2.warehouseGraphEdgeType=\"CONTAINS\" } "
			+ "RETURN fs")
	Iterable<FlatSpecies> getUnconnectedNetworkNodes(String networkUUID, List<String> nodeSymbols);
	
	
//match (s:SBMLSpecies {entityUUID:"b0d96c9e-24ce-4833-810d-f0868044c439"})<-[p:PROV*1.. {provenanceGraphEdgeType:"wasDerivedFrom"}]-(f:FlatSpecies {symbol:"PPP3"})<-[w:Warehouse {warehouseGraphEdgeType:"CONTAINS"}]-(m:MappingNode {entityUUID:"410ecb59-2ec5-456b-9812-c97faef4c781"}) return s, p, f, m
	
	@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "<-[wd:Warehouse]-"
			+ "(f:FlatSpecies)"
			+ "<-[w:Warehouse]-"
			+ "(m:MappingNode) "
			+ "WHERE s.entityUUID = $simpleModelEntityUUID "
			+ "AND wd.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			//+ "AND p.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND m.entityUUID = $networkEntityUUID "
			+ "RETURN f")
	FlatSpecies findBySimpleModelEntityUUIDInNetwork(String simpleModelEntityUUID, String networkEntityUUID);
	
	@Query("MATCH "
			+ "(f:FlatSpecies)"
			+ "<-[w:Warehouse]-"
			+ "(m:MappingNode) "
			+ "WHERE w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND m.entityUUID = $networkEntityUUID "
			+ "AND f.simpleModelEntityUUID = $simpleModelEntityUUID "
			+ "RETURN f")
	FlatSpecies findBySimpleModelEntityUUID(String simpleModelEntityUUID, String networkEntityUUID);
	
}
