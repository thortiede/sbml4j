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
@EnableConfigurationProperties({OutputConfigProperties.class, NetworkConfigProperties.class, AnnotationConfigProperties.class, 
	ExternalResourcesProperties.class, MdAndersonProperties.class, BiologicalQualifierProperties.class, KEGGDatabaseProperties.class,
	ContextConfigProperties.class, CsvConfigProperties.class, GeneralConfigProperties.class, GraphMLProperties.class})
public class SBML4jConfig {
	@Autowired
	private OutputConfigProperties outputConfigProperties;
	
	@Autowired
	private NetworkConfigProperties networkConfigProperties;
	
	@Autowired
	private AnnotationConfigProperties annotationConfigProperties;
	
	@Autowired
	private ExternalResourcesProperties externalResourcesProperties;
	
	@Autowired
	private ContextConfigProperties contextConfigProperties;

	@Autowired
	private CsvConfigProperties csvConfigProperties;
	
	@Autowired
	private GeneralConfigProperties generalConfigProperties;
	
	@Autowired
	private GraphMLProperties graphMLProperties;
	
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

	public ExternalResourcesProperties getExternalResourcesProperties() {
		return externalResourcesProperties;
	}

	public void setExternalResourcesProperties(ExternalResourcesProperties externalResourcesProperties) {
		this.externalResourcesProperties = externalResourcesProperties;
	}

	public ContextConfigProperties getContextConfigProperties() {
		return contextConfigProperties;
	}

	public void setContextConfigProperties(ContextConfigProperties contextConfigProperties) {
		this.contextConfigProperties = contextConfigProperties;
	}

	public CsvConfigProperties getCsvConfigProperties() {
		return csvConfigProperties;
	}

	public void setCsvConfigProperties(CsvConfigProperties csvConfigProperties) {
		this.csvConfigProperties = csvConfigProperties;
	}

	public GeneralConfigProperties getGeneralConfigProperties() {
		return generalConfigProperties;
	}

	public void setGeneralConfigProperties(GeneralConfigProperties generalConfigProperties) {
		this.generalConfigProperties = generalConfigProperties;
	}

	public GraphMLProperties getGraphMLProperties() {
		return graphMLProperties;
	}

	public void setGraphMLProperties(GraphMLProperties graphMLProperties) {
		this.graphMLProperties = graphMLProperties;
	}
}
