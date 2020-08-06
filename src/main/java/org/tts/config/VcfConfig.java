/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
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
