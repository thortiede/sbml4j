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

@ConfigurationProperties(prefix = "vcf.default")
public class VcfDefaultProperties {
/*
 * vcf.default.baseNetworkUUID=2f5b5686-c877-4043-9164-1047cf839816
vcf.default.minSize=1
vcf.default.maxSize=3
vcf.default.terminateAtDrug=false
vcf.default.direction=both

 */
	private String baseNetworkUUID;
	private int minSize;
	private int maxSize;
	private boolean terminateAtDrug;
	private String direction;
	
	
	public String getBaseNetworkUUID() {
		return baseNetworkUUID;
	}
	public void setBaseNetworkUUID(String baseNetworkUUID) {
		this.baseNetworkUUID = baseNetworkUUID;
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
	public boolean isTerminateAtDrug() {
		return terminateAtDrug;
	}
	public void setTerminateAtDrug(boolean terminateAtDrug) {
		this.terminateAtDrug = terminateAtDrug;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	
	
	
	
}
