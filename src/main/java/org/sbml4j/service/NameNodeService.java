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
package org.sbml4j.service;

import org.sbml4j.model.common.NameNode;
import org.sbml4j.repository.common.NameNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NameNodeService {

	@Autowired
	private NameNodeRepository nameNodeRepository;

	public NameNode findByName(String name) { 
		return this.nameNodeRepository.findByName(name);
	}

	public Iterable<NameNode> findAllByExternalResource(String externalResourceEntityUUID) {
		return this.nameNodeRepository.findAllByExternalResource(externalResourceEntityUUID);
	}
	
}