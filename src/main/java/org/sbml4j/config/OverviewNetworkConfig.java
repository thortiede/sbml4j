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

package org.sbml4j.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for handling overview networks
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Configuration
@EnableConfigurationProperties({OverviewNetworkDefaultProperties.class, OverviewNetworkConfigProperties.class})
public class OverviewNetworkConfig {
	@Autowired
	private OverviewNetworkDefaultProperties overviewNetworkDefaultProperties;

	@Autowired
	private OverviewNetworkConfigProperties overviewNetworkConfigProperties;
	
	public OverviewNetworkDefaultProperties getOverviewNetworkDefaultProperties() {
		return overviewNetworkDefaultProperties;
	}

	public void setOverviewNetworkDefaultProperties(OverviewNetworkDefaultProperties overviewNetworkDefaultProperties) {
		this.overviewNetworkDefaultProperties = overviewNetworkDefaultProperties;
	}

	public OverviewNetworkConfigProperties getOverviewNetworkConfigProperties() {
		return overviewNetworkConfigProperties;
	}

	public void setOverviewNetworkConfigProperties(OverviewNetworkConfigProperties overviewNetworkConfigProperties) {
		this.overviewNetworkConfigProperties = overviewNetworkConfigProperties;
	}
	
	
	
}
