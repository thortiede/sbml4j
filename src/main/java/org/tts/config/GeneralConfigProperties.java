package org.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.general")
public class GeneralConfigProperties {

	private String api_documenation_url;

	public String getApi_documenation_url() {
		return api_documenation_url;
	}

	public void setApi_documenation_url(String api_documenation_url) {
		this.api_documenation_url = api_documenation_url;
	}
}
