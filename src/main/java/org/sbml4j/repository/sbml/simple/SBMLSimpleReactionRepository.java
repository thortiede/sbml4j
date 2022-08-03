/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.repository.sbml.simple;

import java.util.List;

import org.sbml4j.model.sbml.SBMLSpecies;
import org.sbml4j.model.sbml.simple.SBMLSimpleReaction;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;


public interface SBMLSimpleReactionRepository extends Neo4jRepository<SBMLSimpleReaction, Long> {

	// @unused
	SBMLSimpleReaction findBysBaseName(String sBaseName, int depth);
	/**
	@Query("MATCH "
	+ "(r)"
	+ "-[rel:IS_PRODUCT|IS_REACTANT|IS_CATALYST]->"
	+ "(s:SBMLSpecies) "
	+ "WHERE s.entityUUID = $speciesEntityUUID "
	+ "WITH r "
	+ "MATCH "
	+ "(r)"
	+ "-[rp]->"
	+ "(p:SBMLSpecies) "
	+ "WHERE type(rp) in [IS_PRODUCT|IS_REACTANT|IS_CATALYST] " 
	+ "RETURN r, rp, p")
	Iterable<SBMLSimpleReaction> findAllReactionsForSpecies(String speciesEntityUUID);
	
	*/
	
	@Query("MATCH "
			+ "(r)"
			+ "-[rel]->"
			+ "(s:SBMLSpecies) "
			+ "WHERE r.entityUUID = $reactionEntityUUID "
			+ "AND type(rel)=$partnerType "
			+ "RETURN s")
	
	Iterable<SBMLSpecies> findAllReactionPartnersOfType(String reactionEntityUUID, String partnerType);
	
	
	@Query("MATCH "
	+ "(r)"
	+ "-[rel:IS_PRODUCT|IS_REACTANT|IS_CATALYST]->"
	+ "(s:SBMLSpecies) "
	+ "WHERE s.entityUUID = $speciesEntityUUID "
	+ "RETURN r")
	Iterable<SBMLSimpleReaction> findAllReactionsForSpecies(String speciesEntityUUID);
	

	@Query("MATCH "
			+ " (p:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(r:SBMLSimpleReaction) "
			+ "WHERE p.entityUUID = $pathwayUUID "
			+ "AND w.warehouseGraphEdgeType=\"CONTAINS\" "
			+ "WITH r "
			+ "MATCH "
			+ "(r)"
			+ "-[rel:IS_PRODUCT|IS_REACTANT|IS_CATALYST]->"
			+ "(s:SBMLSpecies) "
			+ "WHERE s.sBaseSboTerm IN $partnerSBOTerms "
			+ "WITH r, rel, s "
			+ "OPTIONAL MATCH "
			+ "(s)"
			+ "-[b:BQ]->"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE b.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND b.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "WITH r, rel, s, b, e "
			+ "OPTIONAL MATCH "
			+ "(e)"
			+ "-[k:KNOWNAS]->"
			+ "(n:NameNode) "
			+ "RETURN r, rel, s, b, e, k, n ")
	Iterable<SBMLSimpleReaction> findAllInPathwayWithPartnerSBOTerm(String pathwayUUID, List<String> partnerSBOTerms);
}
