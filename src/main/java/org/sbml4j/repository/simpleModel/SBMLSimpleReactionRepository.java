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

import org.sbml4j.model.api.Output.MetabolicPathwayReturnType;
import org.sbml4j.model.simple.SBMLSimpleReaction;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;


public interface SBMLSimpleReactionRepository extends Neo4jRepository<SBMLSimpleReaction, Long> {

	// @unused
	SBMLSimpleReaction findBysBaseName(String sBaseName, int depth);
	
	@Query("MATCH "
	+ "(r)"
	+ "-[rel:IS_PRODUCT|IS_REACTANT|IS_CATALYST]->"
	+ "(s:SBMLSpecies) "
	+ "WHERE s.entityUUID = $speciesEntityUUID "
	+ "RETURN s as species, "
	+ "type(rel) as typeOfRelation, "
	+ "r as reaction")
	Iterable<MetabolicPathwayReturnType> findAllReactionsForSpecies(String speciesEntityUUID);
	
	@Query("MATCH "
			+ "(r:SBMLSimpleReaction)"
			+ "-[rel:IS_PRODUCT|IS_REACTANT|IS_CATALYST]->"
			+ "(s:SBMLSpecies) "
			+ "WHERE r.entityUUID = $reactionEntityUUID "
			+ "RETURN s as species, "
			+ "type(rel) as typeOfRelation, "
			+ "r as reaction")
	Iterable<MetabolicPathwayReturnType> findAllSpeciesForReaction(String reactionEntityUUID);
}
