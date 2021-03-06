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
package org.tts.repository.common;

import java.util.List;

import org.sbml.jsbml.CVTerm;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.BiomodelsQualifier;

public interface BiomodelsQualifierRepository extends Neo4jRepository<BiomodelsQualifier, Long> {

	List<BiomodelsQualifier> findByTypeAndQualifier(CVTerm.Type type, CVTerm.Qualifier qualifier);
	
	@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "-[bq:BQ]-"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE "
			+ "s.entityUUID = $sbmlSpeciesEntityUUID "
			+ "RETURN bq, e")
	List<BiomodelsQualifier> findAllForSBMLSpecies(String sbmlSpeciesEntityUUID);
	
}
