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
package org.tts.repository.provenance;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.provenance.ProvenanceGraphActivityNode;

public interface ProvenanceGraphActivityNodeRepository extends Neo4jRepository<ProvenanceGraphActivityNode, Long> {

	ProvenanceGraphActivityNode findByGraphActivityTypeAndGraphActivityName(ProvenanceGraphActivityType graphActivityType, String graphActivityName);

}
