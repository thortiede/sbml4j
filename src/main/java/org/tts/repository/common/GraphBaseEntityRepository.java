package org.tts.repository.common;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.api.Output.NodeNodeEdge;
import org.tts.model.common.GraphBaseEntity;

public interface GraphBaseEntityRepository extends Neo4jRepository<GraphBaseEntity, Long> {

	GraphBaseEntity findByEntityUUID(String entityUUID);

	boolean existsByEntityUUID(String entityUUID);

	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
					"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
					"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
					"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
					"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
					"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
					"RETURN  e1.name AS node1, "+
							"e1.entityUUID AS node1UUID, "+
							"t.sBaseSboTerm AS edge, "+
							"e2.name AS node2, "+
							"e2.entityUUID AS node2UUID")
	Iterable<NodeNodeEdge> getInteractionCustomNoGroup(String inputExternalRel, String outputExternalRel);

	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
			"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
			"-[:HAS_GROUP_MEMBER]-(g:SBMLQualSpeciesGroup)" +
			"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
			"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
			"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
			"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
			"RETURN  e1.name AS node1, "+
					"e1.entityUUID AS node1UUID, "+
					"t.sBaseSboTerm AS edge, "+
					"e2.name AS node2, "+
					"e2.entityUUID AS node2UUID")
	Iterable<NodeNodeEdge> getInteractionCustomGroupOnInput(String inputExternalRel, String outputExternalRel);
	
	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
			"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
			"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
			"(g:SBMLQualSpeciesGroup)-[:HAS_GROUP_MEMBER]-" +
			"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
			"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
			"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
			"RETURN  e1.name AS node1, "+
					"e1.entityUUID AS node1UUID, "+
					"t.sBaseSboTerm AS edge, "+
					"e2.name AS node2, "+
					"e2.entityUUID AS node2UUID")
	Iterable<NodeNodeEdge> getInteractionCustomGroupOnOutput(String inputExternalRel, String outputExternalRel);
	
	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
			"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
			"-[:HAS_GROUP_MEMBER]-(g:SBMLQualSpeciesGroup)" +
			"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
			"(g:SBMLQualSpeciesGroup)-[:HAS_GROUP_MEMBER]-" +
			"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
			"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
			"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
			"RETURN  e1.name AS node1, "+
					"e1.entityUUID AS node1UUID, "+
					"t.sBaseSboTerm AS edge, "+
					"e2.name AS node2, "+
					"e2.entityUUID AS node2UUID")
	Iterable<NodeNodeEdge> getInteractionCustomGroupOnBoth(String inputExternalRel, String outputExternalRel);
	
	
	/**
	 * match (p:PathwayNode {pathwayIdString: "path_hsa05100"})-[:Warehouse {warehouseGraphEdgeType: "CONTAINS"}]-(c:SBMLSimpleTransition) with c match x= (c)-[tr:IS_INPUT | IS_OUTPUT ]-(qin:SBMLQualSpecies)-[:IS]-(s:SBMLSpecies)-[bq:BQ {qualifier:"BQB_HAS_VERSION"}]-(e:ExternalResourceEntity {type: "kegg.genes"}) return e.name, c.sBaseSboTerm, c.entityUUID, type(tr)
	 * 
	 * match (p:PathwayNode {pathwayIdString: "path_hsa05100"})
	 * 			-[:Warehouse {warehouseGraphEdgeType: "CONTAINS"}]-
	 *       (c:SBMLSimpleTransition) 
	 *  with c 
	 * match x= (c)
	 * 				-[tr:IS_INPUT | IS_OUTPUT ]-
	 *          (qin:SBMLQualSpecies)
	 *          	-[:IS]-
	 *          (s:SBMLSpecies)
	 *          	-[bq:BQ {qualifier:"BQB_HAS_VERSION"}]-
	 *          (e:ExternalResourceEntity {type: "kegg.genes"}) 
	 * return 	e.name, 
	 * 			c.sBaseSboTerm, 
	 * 			c.entityUUID, 
	 * 			type(tr)
	 */
	
	/**
	 * match (p:PathwayNode {pathwayIdString: "path_hsa05100"})-[:Warehouse {warehouseGraphEdgeType: "CONTAINS"}]-(c:SBMLSimpleTransition) with c match x= (e1:ExternalResourceEntity {type: "kegg.genes"})-[bq1:BQ]-(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)-[tr1:IS_INPUT]-(c)-[tr2:IS_OUTPUT ]-(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)-[bq2:BQ]-(e2:ExternalResourceEntity {type: "kegg.genes"}) where bq1.qualifier IN ["BQB_HAS_VERSION", "BQB_IS"] and bq2.qualifier IN ["BQB_HAS_VERSION", "BQB_IS"] return e1.name, bq1.qualifier, s1.sBaseName, type(tr1), c.sBaseSboTerm, c.entityUUID, type(tr2), s2.sBaseName, bq2.qualifier, e2.name
	 * 
	 * 	match 	(p:PathwayNode {pathwayIdString: "path_hsa05100"})
	 * 				-[:Warehouse {warehouseGraphEdgeType: "CONTAINS"}]-
	 * 			(c:SBMLSimpleTransition) 
	 *   with 	c 
	 *  match   x = 
	 *  		(e1:ExternalResourceEntity {type: "kegg.genes"})
	 *  			-[bq1:BQ]-
	 *  		(s1:SBMLSpecies)
	 *  			-[:IS]-
	 *  		(q1:SBMLQualSpecies)
	 *  			-[tr1:IS_INPUT]-
	 *  		(c)
	 *  			-[tr2:IS_OUTPUT ]-
	 *  		(q2:SBMLQualSpecies)
	 *  			-[:IS]-
	 *  		(s2:SBMLSpecies)
	 *  			-[bq2:BQ]-
	 *  		(e2:ExternalResourceEntity {type: "kegg.genes"}) 
	 * 
	 *  where 	bq1.qualifier IN ["BQB_HAS_VERSION", "BQB_IS"] 
	 *    and 	bq2.qualifier IN ["BQB_HAS_VERSION", "BQB_IS"] 
	 * return 	e1.name, 
	 * 			bq1.qualifier, 
	 * 			s1.sBaseName, 
	 * 			type(tr1), 
	 * 			c.sBaseSboTerm, 
	 * 			c.entityUUID, 
	 * 			type(tr2), 
	 * 			s2.sBaseName, 
	 * 			bq2.qualifier, 
	 * 			e2.name
	 */
	
}
