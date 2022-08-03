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
package org.sbml4j.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.general")
public class GeneralConfigProperties {

	private String api_documentation_url;

	public String getApi_documentation_url() {
		return api_documentation_url;
	}

	public void setApi_documentation_url(String api_documentation_url) {
		this.api_documentation_url = api_documentation_url;
	}
}
