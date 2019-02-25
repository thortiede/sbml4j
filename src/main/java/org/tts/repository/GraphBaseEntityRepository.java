package org.tts.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphBaseEntity;
import org.tts.model.NodeNodeEdge;

public interface GraphBaseEntityRepository extends Neo4jRepository<GraphBaseEntity, Long> {

	GraphBaseEntity findByEntityUUID(String entityUUID);

	boolean existsByEntityUUID(String entityUUID);

	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
					"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
					"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
					"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
					"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
					"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
					"RETURN e1.name AS node1, "+
						"t.sBaseSboTerm AS edge, "+
						"e2.name AS node2")
	Iterable<NodeNodeEdge> getInteractionCustomNoGroup(String inputExternalRel, String outputExternalRel);

	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
			"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
			"-[:HAS_GROUP_MEMBER]-(g:SBMLQualSpeciesGroup)" +
			"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
			"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
			"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
			"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
			"RETURN e1.name AS node1, "+
				"t.sBaseSboTerm AS edge, "+
				"e2.name AS node2")
	Iterable<NodeNodeEdge> getInteractionCustomGroupOnInput(String inputExternalRel, String outputExternalRel);
	
	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
			"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
			"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
			"(g:SBMLQualSpeciesGroup)-[:HAS_GROUP_MEMBER]-" +
			"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
			"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
			"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
			"RETURN e1.name AS node1, "+
				"t.sBaseSboTerm AS edge, "+
				"e2.name AS node2")
	Iterable<NodeNodeEdge> getInteractionCustomGroupOnOutput(String inputExternalRel, String outputExternalRel);
	
	@Query(value = "MATCH (e1:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-" +
			"(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)" +
			"-[:HAS_GROUP_MEMBER]-(g:SBMLQualSpeciesGroup)" +
			"-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-" +
			"(g:SBMLQualSpeciesGroup)-[:HAS_GROUP_MEMBER]-" +
			"(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies)" +
			"-[:BQ {qualifier: {1}}]-(e2:ExternalResourceEntity) " +
			"WHERE e1.type = \"kegg.genes\" AND e2.type = \"kegg.genes\""+
			"RETURN e1.name AS node1, "+
				"t.sBaseSboTerm AS edge, "+
				"e2.name AS node2")
	Iterable<NodeNodeEdge> getInteractionCustomGroupOnBoth(String inputExternalRel, String outputExternalRel);
	
}
