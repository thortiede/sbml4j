package org.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.network")
public class NetworkConfigProperties {

	private boolean hardDelete;
	
	private boolean showInactiveNetworks;

	public boolean isHardDelete() {
		return hardDelete;
	}

	public void setHardDelete(boolean hardDelete) {
		this.hardDelete = hardDelete;
	}

	public boolean isShowInactiveNetworks() {
		return showInactiveNetworks;
	}

	public void setShowInactiveNetworks(boolean showInactiveNetworks) {
		this.showInactiveNetworks = showInactiveNetworks;
	}
}
