package org.tts.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphBaseEntity;
import org.tts.model.NodeNodeEdge;
import org.tts.model.SBMLCompartment;

public interface GraphBaseEntityRepository extends Neo4jRepository<GraphBaseEntity, Long> {

	GraphBaseEntity findByEntityUUID(String entityUUID);

	boolean existsByEntityUUID(String entityUUID);

	@Query(value = "match (e2:ExternalResourceEntity)-[:BQ {qualifier: {0}}]-(s2:SBMLSpecies)-[:IS]-(q2:SBMLQualSpecies)-[:IS_INPUT]-(t:SBMLSimpleTransition)-[:IS_OUTPUT]-(q:SBMLQualSpecies)-[:IS]-(s:SBMLSpecies)-[:BQ {qualifier: {1}}]-(e:ExternalResourceEntity) where e.type = \"kegg.genes\" and e2.type = \"kegg.genes\" return e2.name as node1, t.sBaseSboTerm as edge, e.name as node2")
	Iterable<NodeNodeEdge> getInteractionCustom1(String inputExternalRel, String outputExternalRel);

}
