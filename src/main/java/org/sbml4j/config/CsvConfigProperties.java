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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.csv")
public class CsvConfigProperties {
	private List<String> matchingColumnName;

	public List<String> getMatchingColumnName() {
		return matchingColumnName;
	}

	public void setMatchingColumnName(List<String> matchingColumnName) {
		this.matchingColumnName = matchingColumnName;
	}
	

}
