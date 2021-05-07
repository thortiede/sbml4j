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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.output")
public class OutputConfigProperties {
	private boolean hideModelUUIDs;

	public boolean isHideModelUUIDs() {
		return hideModelUUIDs;
	}

	public void setHideModelUUIDs(boolean hideModelUUIDs) {
		this.hideModelUUIDs = hideModelUUIDs;
	}
}
