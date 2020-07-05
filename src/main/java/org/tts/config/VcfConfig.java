package org.tts.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VcfDefaultProperties.class)
public class VcfConfig {
	@Autowired
	private VcfDefaultProperties vcfDefaultProperties;

	public VcfDefaultProperties getVcfDefaultProperties() {
		return vcfDefaultProperties;
	}

	public void setVcfDefaultProperties(VcfDefaultProperties vcfDefaultProperties) {
		this.vcfDefaultProperties = vcfDefaultProperties;
	}
}
