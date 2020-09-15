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
 * Configuration class for SBML4j
 * 
 * Holds the NetworkConfigProperties and the AnnotationConfigProperties
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Configuration
@EnableConfigurationProperties({OutputConfigProperties.class, NetworkConfigProperties.class, AnnotationConfigProperties.class})
public class SBML4jConfig {
	@Autowired
	private OutputConfigProperties outputConfigProperties;
	
	@Autowired
	private NetworkConfigProperties networkConfigProperties;
	
	@Autowired
	private AnnotationConfigProperties annotationConfigProperties;

	public OutputConfigProperties getOutputConfigProperties() {
		return outputConfigProperties;
	}

	public void setOutputConfigProperties(OutputConfigProperties outputConfigProperties) {
		this.outputConfigProperties = outputConfigProperties;
	}

	public NetworkConfigProperties getNetworkConfigProperties() {
		return networkConfigProperties;
	}

	public void setNetworkConfigProperties(NetworkConfigProperties networkConfigProperties) {
		this.networkConfigProperties = networkConfigProperties;
	}

	public AnnotationConfigProperties getAnnotationConfigProperties() {
		return annotationConfigProperties;
	}

	public void setAnnotationConfigProperties(AnnotationConfigProperties annotationConfigProperties) {
		this.annotationConfigProperties = annotationConfigProperties;
	}
}
