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

import org.tts.model.common.ExternalResourceEntity;

public interface HttpService {

	public List<String> getGeneNamesFromKeggURL(String resource);

	public ExternalResourceEntity setCompoundAnnotationFromResource(String resource, ExternalResourceEntity entity);
	
}
