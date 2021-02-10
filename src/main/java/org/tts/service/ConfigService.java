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
		String publicUser = this.sbml4jConfig.getNetworkConfigProperties().getPublicUser();
		if (publicUser != null && !publicUser.equals("")) {
			userList.add(publicUser);
		}
		if (!user.equals(publicUser) && !user.strip().equals("")) {
			userList.add(user);
		}
		return userList;
		
	}
	
	public String getUserNameAttributedToNetwork(String uuid, String user) throws UserUnauthorizedException {
		String publicUser = this.sbml4jConfig.getNetworkConfigProperties().getPublicUser();
		if (publicUser == null || publicUser.strip().equals("")) {
			log.warn("No public user defined in configuration; Public network access not available. Please consider configuring a public user to get rid of this message.");
		}
		publicUser = publicUser.strip();
		if (user!=null) {
			user = user.strip();
		}
		Iterable<ProvenanceEntity> userEntities = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasAttributedTo, uuid);
		Iterator<ProvenanceEntity> peIter = userEntities.iterator();
		ProvenanceEntity pe = peIter.next();
		while (peIter.hasNext()) {
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
		throw new UserUnauthorizedException(user != null ? "User " +user +" is not authorized to access network with uuid" +uuid : "Network with uuid " +uuid + " not accesible for public user");
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
	
}
