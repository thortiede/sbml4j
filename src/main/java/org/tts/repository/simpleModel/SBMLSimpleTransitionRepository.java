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
package org.tts.repository.simpleModel;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.simple.SBMLSimpleTransition;

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
}
