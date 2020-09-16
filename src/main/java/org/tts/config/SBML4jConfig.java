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
