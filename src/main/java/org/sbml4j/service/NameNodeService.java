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

import org.sbml4j.model.sbml.ext.sbml4j.ExternalResourceEntity;
import org.sbml4j.model.sbml.ext.sbml4j.NameNode;
import org.sbml4j.repository.common.NameNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NameNodeService {

	@Autowired
	private NameNodeRepository nameNodeRepository;

	/**
	 * Query the database for {@link NameNode}s with the name 'name'
	 * @param name The name to find
	 * @return {@link NameNode} with given name
	 */
	public NameNode findByName(String name) { 
		return this.nameNodeRepository.findByName(name);
	}

	/**
	 * Find all {@link NameNode}s that are connected to the {@link ExternalResourceEntity} given by the UUID
	 * @param externalResourceEntityUUID The UUID as {@link String} of the {@link ExternalResourceEntity}
	 * @return {@link Iterable} if {@link NameNode} containing all found {@link NameNode}s connected to the {@link ExternalResourceEntity}
	 */
	public Iterable<NameNode> findAllByExternalResource(String externalResourceEntityUUID) {
		return this.nameNodeRepository.findAllByExternalResource(externalResourceEntityUUID);
	}
	
}
