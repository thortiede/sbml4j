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
package org.sbml4j.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sbml4j.network")
public class NetworkConfigProperties {

	private boolean hardDelete;
	
	private boolean showInactiveNetworks;
	
	private boolean allowInactiveDuplicates;
	
	private boolean useSharedPathwaySearch;
	
	private String publicUser;
	
	private boolean forceDeleteOfPublicNetwork;
	
	private boolean deleteExisting;
	
	private boolean deleteDerived;
	
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
	
	public boolean isAllowInactiveDuplicates() {
		return allowInactiveDuplicates;
	}

	public void setAllowInactiveDuplicates(boolean allowInactiveDuplicates) {
		this.allowInactiveDuplicates = allowInactiveDuplicates;
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

	public boolean isForceDeleteOfPublicNetwork() {
		return forceDeleteOfPublicNetwork;
	}

	public void setForceDeleteOfPublicNetwork(boolean forceDeleteOfPublicNetwork) {
		this.forceDeleteOfPublicNetwork = forceDeleteOfPublicNetwork;
	}

	public boolean isDeleteExisting() {
		return deleteExisting;
	}

	public void setDeleteExisting(boolean deleteExisting) {
		this.deleteExisting = deleteExisting;
	}

	public boolean isDeleteDerived() {
		return deleteDerived;
	}

	public void setDeleteDerived(boolean deleteDerived) {
		this.deleteDerived = deleteDerived;
	}
}
