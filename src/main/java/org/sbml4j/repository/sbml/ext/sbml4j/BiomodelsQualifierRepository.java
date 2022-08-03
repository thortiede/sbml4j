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
package org.sbml4j.repository.sbml.ext.sbml4j;

import java.util.List;

import org.sbml.jsbml.CVTerm;
import org.sbml4j.model.sbml.ext.sbml4j.BiomodelsQualifier;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface BiomodelsQualifierRepository extends Neo4jRepository<BiomodelsQualifier, Long> {

	// @unsused
	List<BiomodelsQualifier> findByTypeAndQualifier(CVTerm.Type type, CVTerm.Qualifier qualifier);
	
	
	// potentially switch this over to the findAllForSBaseEntity method
	@Query("MATCH "
			+ "(s:SBMLSpecies)"
			+ "-[bq:BQ]-"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE "
			+ "s.entityUUID = $sbmlSpeciesEntityUUID "
			+ "RETURN bq, e")
	List<BiomodelsQualifier> findAllForSBMLSpecies(String sbmlSpeciesEntityUUID);
	
	@Query("MATCH "
			+ "(s:SBase)"
			+ "-[bq:BQ]-"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE "
			+ "s.entityUUID = $sbmlSBaseEntityUUID "
			+ "RETURN bq, e")
	List<BiomodelsQualifier> findAllForSBaseEntity(String sbmlSBaseEntityUUID);
}
