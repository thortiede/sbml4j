package org.tts.repository.common;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.common.GraphBaseEntity;

public interface GraphBaseEntityRepository extends Neo4jRepository<GraphBaseEntity, Long> {

	GraphBaseEntity findByEntityUUID(String entityUUID);
	
	@Query(value="MATCH "
			+ "(p:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(t:SBMLSimpleTransition) "
			+ "WHERE p.entityUUID = $entityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND t.sBaseSboTerm IN $transitionSBOTerms"
			+ "WITH t "
			+ "MATCH "
			+ "(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)-[tr1:IS_INPUT]-"
			+ "(t)"
			+ "-[tr2:IS_OUTPUT ]-(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies) "
			+ "WHERE s1.sBaseSboTerm IN $nodeSBOTerms) "
			+ "AND s2.sBaseSboTerm IN $nodeSBOTerms) "
			+ "RETURN "
			+ "s1 as inputSpecies,"
			+ "t as transition, "
			+ "s2 as outputSpecies"
			)
	Iterable<NonMetabolicPathwayReturnType> getFlatTransitionsForPathwayUsingSpecies(String entityUUID,
			List<String> transitionSBOTerms, List<String> nodeSBOTerms);
		
	@Query("MATCH "
			+ " (p:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(r:SBMLSimpleReaction) "
			+ "WHERE p.entityUUID = $pathwayEntityUUID "
			+ "AND w.warehouseGraphEdgeType=\"CONTAINS\" "
			+ "WITH r "
			+ "MATCH "
			+ "(r)"
			+ "-[rel:IS_PRODUCT|IS_REACTANT|IS_CATALYST]->"
			+ "(s:SBMLSpecies) "
			+ "WHERE s.sBaseSboTerm IN $nodeSBOTerms "
			+ "RETURN s as species, "
			+ "type(rel) as typeOfRelation, "
			+ "r as reaction")
	Iterable<MetabolicPathwayReturnType> getAllMetabolicPathwayReturnTypes(String pathwayEntityUUID,
			List<String> nodeSBOTerms);
	
}
