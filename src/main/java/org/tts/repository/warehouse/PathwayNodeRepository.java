package org.tts.repository.warehouse;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.PathwayNode;

public interface PathwayNodeRepository extends Neo4jRepository<PathwayNode, Long> {

	
	/**
	 * match (p:PathwayNode)-[:PROV {provenanceGraphEdgeType: "wasAttributedTo"}]-(:PROV_AGENT {graphAgentName: "Thor"}) return p
	 * @param username
	 * @return
	 */
	@Query(value = "MATCH "
			+ "(p:PathwayNode)"
			+ "-[:PROV {provenanceGraphEdgeType: \"wasAttributedTo\"}]-"
			+ "(:PROV_AGENT {graphAgentName: {0}})"
			+ "RETURN p")
	Iterable<PathwayNode> findAllPathwaysAttributedToUser(String username);

	
	@Query(value="MATCH "
			+ "(p:PathwayNode {entityUUID: {1}})"
			+ "-[:PROV {provenanceGraphEdgeType: \"wasAttributedTo\"}]-"
			+ "(:PROV_AGENT {graphAgentName: {0}})"
			+ "RETURN p")
	PathwayNode findPathwayAttributedToUser(String username, String entityUUID);


	PathwayNode findByEntityUUID(String entityUUID);


	@Query("MATCH "
			+ "(p:PathwayNode)-[w:Warehouse]->(s:SBMLSpecies) "
			+ "WHERE p.entityUUID = $pathwayNodeEntityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN DISTINCT s.sBaseSboTerm")
	Iterable<String> getAllDistinctSpeciesSboTermsOfPathway(String pathwayNodeEntityUUID);


	@Query("MATCH "
			+ "(p:PathwayNode)-[w:Warehouse]->(t:SBMLSimpleTransition) "
			+ "WHERE p.entityUUID = $pathwayNodeEntityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN DISTINCT t.sBaseSboTerm")
	Iterable<String> getAllDistinctTransitionSboTermsOfPathway(String pathwayNodeEntityUUID);


	@Query("MATCH "
			+ "(g1:GraphBaseEntity)"
			+ "-[w1:Warehouse]->"
			+ "(s1:SBMLSpecies)"
			+ "<-[wc1:Warehouse]-"
			+ "(p:PathwayNode)"
			+ "-[wc2:Warehouse]->"
			+ "(s2:SBMLSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(g2:GraphBaseEntity), "
			+ "(pc:PathwayCollectionNode) "
			+ "WHERE g1.entityUUID = $entity1UUID "
			+ "AND g2.entityUUID = $entity2UUID "
			+ "AND w1.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND w2.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND wc1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND wc2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND NOT (p)-[:PROV {provenanceGraphEdgeType:\"wasDerivedFrom\"}]->(pc:PathwayCollectionNode) "
			+ "RETURN p")
	List<PathwayNode> findAllBySharedDerivedFromWarehouseEdge(String entity1UUID, String entity2UUID);
	
	/*@Query("MATCH "
			+ "(g1:GraphBaseEntity)"
			+ "-[w1:Warehouse]->"
			+ "(s1:SBMLSpecies)"
			+ "<-[wc1:Warehouse]-"
			+ "(p:PathwayNode)"
			+ "-[wc2:Warehouse]->"
			+ "(s2:SBMLSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(g2:GraphBaseEntity) "
			+ "WHERE g1.entityUUID = $entity1UUID "
			+ "AND g2.entityUUID = $entity2UUID "
			+ "AND w1.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND w2.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND wc1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND wc2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND NOT p.entityUUID IN $exclusionPathwayEntityUUIDs "
			+ "RETURN p")
	List<PathwayNode> findAllBySharedDerivedFromWarehouseEdgeWithExlusion(String entity1UUID, String entity2UUID, List<String> exclusionPathwayEntityUUIDs);

	@Query("MATCH "
			+ "(p:PathwayNode)"
			+ "-[pr:PROV]->"
			+ "(pc:PathwayCollectionNode) "
			+ "WHERE pr.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "RETURN p.entityUUID")
	List<String> findAllPathwaysDirectlyDerivedFromCollections();
*/
}
