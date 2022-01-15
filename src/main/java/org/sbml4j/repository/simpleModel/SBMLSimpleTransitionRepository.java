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
package org.sbml4j.repository.simpleModel;

import java.util.List;

import org.sbml4j.model.queryResult.NonMetabolicPathwayReturnType;
import org.sbml4j.model.sbml.simple.ext.qual.SBMLSimpleTransition;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SBMLSimpleTransitionRepository extends Neo4jRepository<SBMLSimpleTransition, Long> {

	SBMLSimpleTransition getByTransitionId(String transitionId, int depth);
	
	@Query(value = "MATCH (t:SBMLSimpleTransition) RETURN DISTINCT t.sBaseSboTerm;")
	Iterable<String> getTransitionTypes();
	
	@Query(value="MATCH "
			+ "(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)-[tr1:IS_INPUT]-"
			+ "(t)"
			+ "-[tr2:IS_OUTPUT ]-(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies) "
			+ "WHERE s1.entityUUID = $speciesEntityUUID "
			+ "OR s2.entityUUID = $speciesEntityUUID "
			+ "RETURN "
			+ "s1 as inputSpecies,"
			+ "t as transition, "
			+ "s2 as outputSpecies"
			)
	Iterable<NonMetabolicPathwayReturnType> getTransitionsForSpecies(String speciesEntityUUID);

	
	@Query(value = "MATCH "
			+ "(p:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(t:SBMLSimpleTransition) "
			+ "where p.entityUUID = $pathwayEntityUUID "
			+ "with t "
			+ "MATCH "
			+ "(qi:SBMLQualSpecies)"
			+ "<-[in:IS_INPUT]-"
			+ "(t)"
			+ "-[out:IS_OUTPUT]->"
			+ "(qo:SBMLQualSpecies) "
			+ "RETURN qi, in,  t, out, qo")
	Iterable<SBMLSimpleTransition> findAllInPathway(String pathwayEntityUUID);

	@Query(value = "MATCH "
			+ "(p:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(t:SBMLSimpleTransition) "
			+ "where p.entityUUID = $pathwayEntityUUID "
			+ "and t.sBaseSboTerm in $transitionSBOTerms "
			+ "with t "
			+ "MATCH "
			+ "(ei:ExternalResourceEntity)"
			+ "<-[bi:BQ]-"
			+ "(qi:SBMLQualSpecies)"
			+ "<-[in:IS_INPUT]-"
			+ "(t)"
			+ "-[out:IS_OUTPUT]->"
			+ "(qo:SBMLQualSpecies)"
			+ "-[bo:BQ]->"
			+ "(eo:ExternalResourceEntity) "
			+ "WHERE bi.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND bi.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "AND qi.sBaseSboTerm in $nodeSBOTerms "
			+ "AND qo.sBaseSboTerm in $nodeSBOTerms "
			+ "AND bo.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND bo.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "RETURN ei, bi, qi, in,  t, out, qo, bo, eo")
	@Deprecated
	/**
	 * Method to fetch all transitions in a pathway.
	 * @deprecated Assembles result by hand, which is slow. Use findMatchingPathsInPathway instead, which uses a path to return the result and is much faster.
	 * @param pathwayEntityUUID
	 * @param transitionSBOTerms
	 * @param nodeSBOTerms
	 * @return
	 */
	Iterable<SBMLSimpleTransition> findMatchingInPathway(String pathwayEntityUUID, List<String> transitionSBOTerms,
			List<String> nodeSBOTerms);
	
	@Query(value = "MATCH "
			+ "(pw:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(t:SBMLSimpleTransition) "
			+ "where pw.entityUUID = $pathwayEntityUUID "
			+ "and t.sBaseSboTerm in $transitionSBOTerms "
			+ "with t "
			+ "MATCH p="
			+ "(t)"
			+ "-[tr:IS_OUTPUT|IS_INPUT]->"
			+ "(q:SBMLQualSpecies)"
			+ "-[bq:BQ]->"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE bq.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND bq.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "AND q.sBaseSboTerm in $nodeSBOTerms "
			+ "RETURN p")
	Iterable<SBMLSimpleTransition> findMatchingPathsInPathway(String pathwayEntityUUID, List<String> transitionSBOTerms,
			List<String> nodeSBOTerms);
}
