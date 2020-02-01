package org.tts.repository.warehouse;

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

}
