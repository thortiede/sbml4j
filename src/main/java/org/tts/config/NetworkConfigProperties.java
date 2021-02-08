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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.network")
public class NetworkConfigProperties {

	private boolean hardDelete;
	
	private boolean showInactiveNetworks;
	
	private boolean useSharedPathwaySearch;
	
	private String publicUser;
	
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
	
	public boolean isUseSharedPathwaySearch() {
		return useSharedPathwaySearch;
	}

	public void setUseSharedPathwaySearch(boolean useSharedPathwaySearch) {
		this.useSharedPathwaySearch = useSharedPathwaySearch;
	}

	public String getPublicUser() {
		return publicUser;
	}

	public void setPublicUser(String publicUser) {
		this.publicUser = publicUser;
	}
}
