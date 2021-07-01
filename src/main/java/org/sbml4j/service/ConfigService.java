package org.sbml4j.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sbml4j.Exception.UserUnauthorizedException;
import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	Logger log = LoggerFactory.getLogger(ConfigService.class);
	
	/**
	 * Get a List of Usernames that included the configured public user
	 * @param user The username to form a list with
	 * @return List of String of Usernames including the public user
	 */
	public List<String> getUserList(String user) {
		List<String> userList = new ArrayList<>();
		// get public user
		String publicUser = this.getPublicUser();
		if (publicUser != null && !publicUser.equals("")) {
			userList.add(publicUser);
		}
		if (user != null && !user.equals(publicUser) && !user.strip().equals("")) {
			userList.add(user);
		}
		return userList;
		
	}
	
	/**
	 * Get the user that is attributed to the Network with the given uuid-String.
	 * Returns the provided user if it is associated with the network, or the public user otherwise
	 * @param uuid The UUID of the network to check
	 * @param user The UUID that needs to be checked as the attributed User
	 * @return The name of the provided user, if it it is associated to the network, or the public user if the network is associated to the public user
	 * @throws UserUnauthorizedException If neither the provided user, nor the public user are associated to the network with the provided uuid
	 */
	public String getUserNameAttributedToNetwork(String uuid, String user) throws UserUnauthorizedException {
		String publicUser = this.sbml4jConfig.getNetworkConfigProperties().getPublicUser();
		if (publicUser == null || publicUser.isEmpty() || publicUser.isBlank()) {
			log.warn("No public user defined in configuration; Public network access not available. Please consider configuring a public user to get rid of this message.");
		}
		publicUser = publicUser.strip();
		if (user!=null) {
			user = user.strip();
		}
		Iterable<ProvenanceEntity> userEntities = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasAttributedTo, uuid);
		Iterator<ProvenanceEntity> peIter = userEntities.iterator();
		while (peIter.hasNext()) {
			ProvenanceEntity pe = peIter.next();
			try {
				ProvenanceGraphAgentNode peAgent = (ProvenanceGraphAgentNode) pe;
				if (user != null && peAgent.getGraphAgentName().equals(user)) {
					return user;
				} else if (publicUser != null && peAgent.getGraphAgentName().equals(publicUser)) {
					return publicUser;
				}
			} catch (ClassCastException e) {
				log.warn("Entity attributed to network with uuid " + uuid + " is not ProvenanceGraphAgentNode. The UUID is: " +pe.getEntityUUID());
			}		
		}
		// if we reached here, the network was not attributed to the given user, or the configured public user
		throw new UserUnauthorizedException(user != null 
													? "User " +user +" is not authorized to access network with uuid " +uuid 
													: "Network with uuid " +uuid + " not accesible for public user");
	}
	
	/**
	 * Check if the provided user is the configured public user
	 * @param user The user to check
	 * @return true if the provided user is the public user, false otherwise
	 */
	public boolean isPublicUser(String user) {
		String publicUser = this.sbml4jConfig.getNetworkConfigProperties().getPublicUser();
	
		if (user != null && publicUser != null) {
			return user.strip().equals(publicUser.strip());
		} else {
			return false;
		}
	}
	
	/**
	 * Check the value of the Network config option publicUser and return it
	 * @return the configured public user, will return Null if no public user is set.
	 */
	public String getPublicUser() {
		String publicUser = this.sbml4jConfig.getNetworkConfigProperties().getPublicUser();
		if (publicUser == null) {
			log.warn("No public user defined in configuration; Public network access not available. Please consider configuring a public user to get rid of this message.");
		}
		return publicUser;
	}
	
	/**
	 * Check if the provided user is the public user and if the configuration is set to delete public networks
	 * @param user The user to check
	 * @return true if the provided user is the public user and if the forceDeleteOfPublicNetworks config property is set to True, false otherwise
	 */
	public boolean isAllowedToDeletePublicNetwork(String user) {
		return this.isPublicUser(user) && this.sbml4jConfig.getNetworkConfigProperties().isForceDeleteOfPublicNetwork();
	}
	
	/**
	 * Check the value of the configured context-option: minSize and return it
	 * @return the value of the configured context-option: minSize
	 */
	public Integer getContextMinSize() {
		return this.sbml4jConfig.getContextConfigProperties().getMinSize();
	}
	
	/**
	 * Check the value of the configured context-option: maxSize and return it
	 * @return the value of the configured context-option: maxSize
	 */
	public Integer getContextMaxSize() {
		return this.sbml4jConfig.getContextConfigProperties().getMaxSize();
	}
	/**
	 * Check the value of the configured context-option: direction and return it
	 * @return the value of the configured context-option: direction
	 */
	public String getContextDirection() {
		return this.sbml4jConfig.getContextConfigProperties().getDirection();
	}
	/**
	 * Check the value of the configured context-option: terminateAt and return it
	 * @return the value of the configured context-option: terminateAt
	 */
	public String getContextTerminateAt() {
		return this.sbml4jConfig.getContextConfigProperties().getTerminateAt();
	}
	
	/**
	 * Check the value of the configured network-option: allow-inactive-duplicates and return it
	 * @return the value of the configured network-option: allow-inactive-duplicates
	 */
	public boolean isAllowInactiveDuplicates() {
		return this.sbml4jConfig.getNetworkConfigProperties().isAllowInactiveDuplicates();
	}
	/**
	 * Check the value of the configured network-option: hard-delete and return it
	 * @return the value of the configured network-option: hard-delete
	 */
	public boolean isHardDeleteNetwork() {
		return this.sbml4jConfig.getNetworkConfigProperties().isHardDelete();
	}
	/**
	 * Check the value of the configured network-option: delete-existing and return it
	 * @return the value of the configured network-option: delete-existing
	 */
	public boolean isDeleteExistingNetwork() {
		return this.sbml4jConfig.getNetworkConfigProperties().isDeleteExisting();
	}
	/**
	 * Check the value of the configured network-option: delete-derived and return it
	 * @return the value of the configured network-option: delete-derived
	 */
	public boolean isDeleteDerivedNetworks() {
		return this.sbml4jConfig.getNetworkConfigProperties().isDeleteDerived();
	}
	/**
	 * Check if the provided symbol is contained in the configured externalresources.mdanderson-option: genelist
	 * @param symbol The symbol to check
	 * @return true if the symbol is in the geneList, false otherwise
	 */
	public boolean isSymbolInMDAnderson(String symbol) {
		return this.sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().getGenelist().contains(symbol);
	}
	
	/**
	 * Check if the config option externalresources.mdanderson.add-md-anderson-annotation is set to true or false or not set
	 * @return true if the option is set to true, false otherwise
	 */
	public boolean isAddMDAnderson() {
		return this.sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().isAddMdAndersonAnnotation();
	}
	
	/**
	 * Create a url in the form https://pct.mdanderson.org/home/ABL1?section=Overview from the symbol
	 * @param symbol The Symbol to create the URL for
	 * @return String containing the full URL, or NULL if the symbol is not part of MDAnderson
	 */
	public String getMDAndersonString(String symbol) {
		if (isSymbolInMDAnderson(symbol)) {
			return this.sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().getBaseurl()
				+ symbol
				+ "?section="
				+ this.sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().getSection();
		} else {
			return null;
		}
	}
	
	/**
	 * {@link CsvConfigProperties}
	 */
	
	/**
	 * Checks if any matching column names are configured
	 * @return true if at least one column name is configured, false otherwise
	 */
	public boolean areMatchingColumnsConfigured() {
		if (this.sbml4jConfig.getCsvConfigProperties().getMatchingColumnName() != null
				&& !this.sbml4jConfig.getCsvConfigProperties().getMatchingColumnName().isEmpty()) {
			return true;
		} else {
			return false;
		}		
	}
	
	/**
	 * Checks if the given String is in the List of configured column names for matching the genesymbol
	 * @param other The String to check
	 * @return true if the String other is in the List, false otherwise
	 */
	public boolean isInMatchingColums(String other) {
		return this.sbml4jConfig.getCsvConfigProperties().getMatchingColumnName().contains(other.strip().toLowerCase());
	}
	

	/**
	 * {@link GeneralConfigProperties}
	 */
	
	/**
	 * Find the config-option: general.api-documentation-url
	 * @return the configured url
	 */
	public String getApi_documentation_url() {
		return this.sbml4jConfig.getGeneralConfigProperties().getApi_documentation_url();
	}
	
	
}
