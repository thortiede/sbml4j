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
package org.tts.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@ConfigurationProperties(prefix = "sbml4j.externalresources")
public class ExternalResourcesProperties {

	@Autowired
	private MdAndersonProperties mdAndersonProperties;

	public MdAndersonProperties getMdAndersonProperties() {
		return mdAndersonProperties;
	}

	public void setMdAndersonProperties(MdAndersonProperties mdAndersonProperties) {
		this.mdAndersonProperties = mdAndersonProperties;
	}
	
}
