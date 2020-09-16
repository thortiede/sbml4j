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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for handling VCF Files
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Configuration
@EnableConfigurationProperties({VcfDefaultProperties.class, VcfConfigProperties.class})
public class VcfConfig {
	@Autowired
	private VcfDefaultProperties vcfDefaultProperties;

	@Autowired
	private VcfConfigProperties vcfConfigProperties;
	
	public VcfDefaultProperties getVcfDefaultProperties() {
		return vcfDefaultProperties;
	}

	public void setVcfDefaultProperties(VcfDefaultProperties vcfDefaultProperties) {
		this.vcfDefaultProperties = vcfDefaultProperties;
	}

	public VcfConfigProperties getVcfConfigProperties() {
		return vcfConfigProperties;
	}

	public void setVcfConfigProperties(VcfConfigProperties vcfConfigProperties) {
		this.vcfConfigProperties = vcfConfigProperties;
	}
	
	
	
}
