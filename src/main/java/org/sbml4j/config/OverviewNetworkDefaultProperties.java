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

@ConfigurationProperties(prefix = "overviewnetwork.default")
public class OverviewNetworkDefaultProperties {

	private String baseNetworkName;
	private int minSize;
	private int maxSize;
	private String terminateAt;
	private String direction;
	
	
	public String getBaseNetworkName() {
		return baseNetworkName;
	}
	public void setBaseNetworkName(String baseNetworkName) {
		this.baseNetworkName = baseNetworkName;
	}
	public int getMinSize() {
		return minSize;
	}
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}
	public int getMaxSize() {
		return maxSize;
	}
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	public String getTerminateAt() {
		return terminateAt;
	}
	public void setTerminateAt(String terminateAt) {
		this.terminateAt = terminateAt;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
}
