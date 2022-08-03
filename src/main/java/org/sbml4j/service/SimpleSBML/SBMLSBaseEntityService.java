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
package org.sbml4j.service.SimpleSBML;

import org.sbml4j.model.base.GraphBaseEntity;
import org.sbml4j.model.sbml.SBMLSBaseEntity;
import org.sbml4j.repository.sbml.SBMLSBaseEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SBMLSBaseEntityService {

	@Autowired
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	
	/**
	 * Persist the provided {@link SBMLSBaseEntity} and all connected {@link GraphBaseEntity} up to the provided depth
	 * @param sbase The {@link SBMLSBaseEntity} to persist
	 * @param depth The number of relationships to traverse to look for {@link GraphBaseEntity} to persist
	 * @return The persisted {@link SBMLSBaseEntity} from the database
	 */
	public SBMLSBaseEntity save(SBMLSBaseEntity sbase, int depth) {
		return this.sbmlSBaseEntityRepository.save(sbase, depth);
	}
	
	
}
