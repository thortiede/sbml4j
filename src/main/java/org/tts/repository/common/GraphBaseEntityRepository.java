/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
 */

package org.tts.repository.common;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.common.GraphBaseEntity;

/**
 * Repository for handling <a href="#{@link}">{@link GraphBaseEntity}</a>
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
public interface GraphBaseEntityRepository extends Neo4jRepository<GraphBaseEntity, Long> {

	/**
	 * Find a <a href="#{@link}">{@link GraphBaseEntity}</a> by its entityUUID attribute
	 * 
	 * @param entityUUID the entityUUID to find
	 * @return <a href="#{@link}">{@link GraphBaseEntity}</a> found for the entityUUID attribute
	 */
	GraphBaseEntity findByEntityUUID(String entityUUID);
	
	@Query(value="MATCH "
			+ "(p:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(t:SBMLSimpleTransition) "
			+ "WHERE p.entityUUID = $entityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND t.sBaseSboTerm IN $transitionSBOTerms "
			+ "WITH t "
			+ "MATCH "
			+ "(s1:SBMLSpecies)-[:IS]-(q1:SBMLQualSpecies)-[tr1:IS_INPUT]-"
			+ "(t)"
			+ "-[tr2:IS_OUTPUT ]-(q2:SBMLQualSpecies)-[:IS]-(s2:SBMLSpecies) "
			+ "WHERE s1.sBaseSboTerm IN $nodeSBOTerms "
			+ "AND s2.sBaseSboTerm IN $nodeSBOTerms "
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
