package org.tts.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OutputConfigProperties.class, NetworkConfigProperties.class})
public class SBML4jConfig {
	@Autowired
	private OutputConfigProperties outputConfigProperties;
	
	@Autowired
	private NetworkConfigProperties networkConfigProperties;

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
}
