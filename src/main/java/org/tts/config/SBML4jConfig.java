package org.tts.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OutputConfigProperties.class)
public class SBML4jConfig {
	@Autowired
	private OutputConfigProperties outputConfigProperties;

	public OutputConfigProperties getOutputConfigProperties() {
		return outputConfigProperties;
	}

	public void setOutputConfigProperties(OutputConfigProperties outputConfigProperties) {
		this.outputConfigProperties = outputConfigProperties;
	}
}
