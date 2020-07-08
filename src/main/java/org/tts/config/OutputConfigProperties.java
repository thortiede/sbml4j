package org.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.output")
public class OutputConfigProperties {
	private boolean hideModelUUIDs;

	public boolean isHideModelUUIDs() {
		return hideModelUUIDs;
	}

	public void setHideModelUUIDs(boolean hideModelUUIDs) {
		this.hideModelUUIDs = hideModelUUIDs;
	}
}
