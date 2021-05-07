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
package org.tts.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.repository.common.ExternalResourceEntityRepository;

@Service
public class ExternalResourceEntityService {

	@Autowired
	ExternalResourceEntityRepository externalResourceEntityRepository;
	
	
	/**
	 * Find a <a href="#{@link}">{@link ExternalResourceEntity}</a> by it's uri (Which is unique)
	 * @param uri The uri to search
	 * @return The <a href="#{@link}">{@link ExternalResourceEntity}</a> found, null otherwise
	 */
	ExternalResourceEntity findByUri(String uri) {
		return this.externalResourceEntityRepository.findByUri(uri);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link ExternalResourceEntity}</a> with a uri in the given List
	 * @param listOfUris <a href="#{@link}">{@link List}</a> of <a href="#{@link}">{@link String}</a> of uris
	 * @return A <a href="#{@link}">{@link List}</a> of <a href="#{@link}">{@link ExternalResourceEntity}</a> that each match one of the uris given
	 */
	List<ExternalResourceEntity> findAllByUri(List<String> listOfUris) {
		return this.externalResourceEntityRepository.findAllByUri(listOfUris);
	}
	
}
