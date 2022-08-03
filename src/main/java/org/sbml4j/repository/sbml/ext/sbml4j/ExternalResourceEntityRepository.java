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

import org.sbml4j.model.sbml.ext.sbml4j.ExternalResourceEntity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ExternalResourceEntityRepository extends Neo4jRepository<ExternalResourceEntity, Long> {

	public ExternalResourceEntity findByUri(String uri);
	
	//@unused
	@Query("MATCH "
			+ "(e:ExternalResourceEntity) "
			+ "WHERE e.uri in $listOfUris "
			+ "return e")
	public List<ExternalResourceEntity> findAllByUri(List<String> listOfUris);
	
	
}
