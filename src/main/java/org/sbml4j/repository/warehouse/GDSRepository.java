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
package org.sbml4j.repository.warehouse;

import java.util.List;

import org.sbml4j.model.common.GraphBaseEntity;
import org.sbml4j.model.flat.FlatEdge;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface GDSRepository extends Neo4jRepository<GraphBaseEntity, Long> {

	
	
	/*call gds.graph.create.cypher('test_gds2', 
	'match (m:MappingNode)-[w:Warehouse]->(f:FlatSpecies) 
	where m.entityUUID = "7727e5a2-1b51-4e8d-9aa1-c20353afc170" 
	  and w.warehouseGraphEdgeType = "CONTAINS" *
	  return id(f) as id', 'match (m:MappingNode)-[w:Warehouse]->(f:FlatSpecies)-[r:PRODUCTOF|REACTANTOF|CATALYSES]-
	  (f2:FlatSpecies)<-[w2:Warehouse]-(m:MappingNode) 
	    where m.entityUUID = "7727e5a2-1b51-4e8d-9aa1-c20353afc170" 
	      and w.warehouseGraphEdgeType = "CONTAINS" and w2.warehouseGraphEdgeType = "CONTAINS" 
	   return id(f) as source, id(f2) as target') YIELD graphName, nodeCount, relationshipCount, createMillis;
	
	*/
	/*
	 * 
	 */
	
	@Query("CALL gds.graph.create.cypher"
			+ "("
			+ "'$graphName', "
			+ "'MATCH (m:MappingNode)-[w:Warehouse]->(f:FlatSpecies) "
			+ "  WHERE m.entityUUID = $baseMappingUUID "
			+ "    AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "  RETURN id(f) as id', "
			+ "'MATCH 	(m:MappingNode)-[w:Warehouse]->(f:FlatSpecies)-[r:$relationshipString]-"
			+ "			(f2:FlatSpecies)<-[w2:Warehouse]-(m:MappingNode) "
			+ "	 WHERE m.entityUUID = $baseMappingUUID2 "
			+ "    AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "    AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "  RETURN id(f) as source), id(f2) as target'"
			+ ") "
			+ "YIELD graphName, nodeCount, relationshipCount, createMillis "
			+ "RETURN nodeCount")
	public int createGdsGraphForMapping(String graphName, String baseMappingUUID, String relationshipString);
	
	
	@Query("CALL gds.graph.create.cypher("
			+ "$graphNameQuery, "
			+ "$nodeQuery, "
			+ "$relationshipQuery) "
			+ "YIELD graphName, nodeCount, relationshipCount, createMillis "
			+ "RETURN nodeCount")
	public int createGdsGraphForMappingUsingQueries(String graphNameQuery, String nodeQuery, String relationshipQuery);
	
	/*
	 * CALL gds.graph.exists('my-store-graph') YIELD exists;
	 */
	
	@Query("CALL "
			+ "gds.graph.exists($graphName) "
			+ "YIELD exists "
			+ "RETURN exists")
	public boolean gdsGraphExists(String graphName);
	
	
	/*
	 * match (f:FlatSpecies), (e:FlatSpecies) 
	 *   where f.entityUUID = "ca81f030-d2c8-4c04-80f9-3205a8559ccb" 
	 *     and e.entityUUID = "cb8cc197-973b-418a-9268-cac964002f87" 
	 * with id(f) as startNode, 
	 *      [id(e)] as targetNodes 
	 *  call gds.alpha.bfs.stream('test_gds_undir', 
	 *  							{startNode:startNode, targetNodes:targetNodes}
	 *  						 )
	 *   YIELD path return nodes(path), relationships(path)
	 */
	
	@Query("MATCH "
			+ "(f:FlatSpecies), (x:FlatSpecies) "
			+ "WHERE f.entityUUID = $startNodeUUID "
			+ "AND x.entityUUID in $targetNodeUUIDs "
			+ "WITH id(f) as startNode, "
			+ "[id(x)] as targetNodes "
			+ "CALL gds.alpha.bfs.stream"
			+ "("
			+ "$graphName, "
			+ "{startNode:startNode, targetNodes:targetNodes}"
			+ ") "
			+ "YIELD path as gdsPath"
			+ "relationships(gdsPath) as pathEdges")
	public Iterable<FlatEdge> runBFSonGdsGraph(String graphName, String startNodeUUID, List<String> targetNodeUUIDs);
	
	
	
	
	
}
