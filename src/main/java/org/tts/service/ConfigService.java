package org.tts.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.Exception.UserUnauthorizedException;
import org.tts.config.SBML4jConfig;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphAgentNode;

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
	
	public boolean isPublicUser(String user) {
		String publicUser = this.sbml4jConfig.getNetworkConfigProperties().getPublicUser();
	
		if (user != null && publicUser != null) {
			return user.strip().equals(publicUser.strip());
		} else {
			return false;
		}
	
	}
	
	public boolean isAllowedToDeletePublicNetwork(String user) {
		return this.isPublicUser(user) && this.sbml4jConfig.getNetworkConfigProperties().isForceDeleteOfPublicNetwork();
	}
	
	public Integer getContextMinSize() {
		return this.sbml4jConfig.getContextConfigProperties().getMinSize();
	}
	public Integer getContextMaxSize() {
		return this.sbml4jConfig.getContextConfigProperties().getMaxSize();
	}
	public String getContextDirection() {
		return this.sbml4jConfig.getContextConfigProperties().getDirection();
	}
	public String getContextTerminateAt() {
		return this.sbml4jConfig.getContextConfigProperties().getTerminateAt();
	}
	
	public String getPublicUser() {
		String publicUser = this.sbml4jConfig.getNetworkConfigProperties().getPublicUser();
		if (publicUser == null) {
			log.warn("No public user defined in configuration; Public network access not available. Please consider configuring a public user to get rid of this message.");
		}
		return publicUser;
	}

	public boolean isAllowInactiveDuplicates() {
		return this.sbml4jConfig.getNetworkConfigProperties().isAllowInactiveDuplicates();
	}
	
	
	
	public boolean isSymbolInMDAnderson(String symbol) {
		return this.sbml4jConfig.getExternalResourcesProperties().getMdAndersonProperties().getGenelist().contains(symbol);
	}
	
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
}
