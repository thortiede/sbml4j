package org.sbml4j.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.general")
public class GeneralConfigProperties {

	private String api_documentation_url;

	public String getApi_documentation_url() {
		return api_documentation_url;
	}

	public void setApi_documentation_url(String api_documentation_url) {
		this.api_documentation_url = api_documentation_url;
	}
}
