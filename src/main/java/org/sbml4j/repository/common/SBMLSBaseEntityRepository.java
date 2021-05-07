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
package org.sbml4j.repository.common;

import java.util.List;

import org.sbml4j.model.common.SBMLSBaseEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SBMLSBaseEntityRepository extends Neo4jRepository<SBMLSBaseEntity, Long> {

	List<SBMLSBaseEntity> findBysBaseId(String sBaseId, int depth);
}
