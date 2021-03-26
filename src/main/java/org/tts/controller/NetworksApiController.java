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
package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.tts.Exception.AnnotationException;
import org.tts.Exception.NetworkAlreadyExistsException;
import org.tts.Exception.UserUnauthorizedException;
import org.tts.api.NetworksApi;
import org.tts.config.SBML4jConfig;
import org.tts.model.api.AnnotationItem;
import org.tts.model.api.FilterOptions;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.NetworkOptions;
import org.tts.model.api.NodeList;
import org.tts.model.flat.FlatEdge;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.ConfigService;
import org.tts.service.FlatSpeciesService;
import org.tts.service.GraphBaseEntityService;
import org.tts.service.MyDrugService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.WarehouseGraphService;
import org.tts.service.networks.NetworkResourceService;
import org.tts.service.networks.NetworkService;
import org.tts.service.warehouse.MappingNodeService;

/**
 * Controller class for all things networks related
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class NetworksApiController implements NetworksApi {

	Logger log = LoggerFactory.getLogger(NetworksApiController.class);
	
	@Autowired
	ConfigService configService;
	
	@Autowired
	FlatSpeciesService flatSpeciesService;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	MappingNodeService mappingNodeService;

	@Autowired
	MyDrugService myDrugService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	NetworkResourceService networkResourceService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	@Override
	public ResponseEntity<NetworkInventoryItem> addAnnotationToNetwork(UUID UUID, @Valid AnnotationItem annotationItem,
			String user, @Valid String networkname, @Valid Boolean prefixName, @Valid Boolean derive) {
		String uuid = UUID.toString();
		log.info("Serving POST /networks/" + uuid + "/annotation" + (user != null ? " for user " + user : "") + " with AnnotationItem " + annotationItem.toString());
		
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(uuid) == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		
		// 3. Force copy if network is from public user!
		if (this.configService.isPublicUser(networkUser) && !derive) {
			derive = true;
			log.warn("Overriding derive parameter because given user is the public user and network has to be derived");
		}
				
		// 4. Add annotation to network
		MappingNode annotatedNetwork;
		try {
			annotatedNetwork = this.networkService.annotateNetwork(user != null ? user.strip() : networkUser,
												annotationItem, uuid, networkname, prefixName, derive);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		} catch (AnnotationException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
	
		// 4. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(annotatedNetwork.getEntityUUID()), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<NetworkInventoryItem> addCsvDataToNetwork(UUID UUID, @NotNull @Valid String type, String user,
			@Valid String networkname, @Valid Boolean prefixName, @Valid Boolean derive,
			@Valid List<MultipartFile> data) {
		
		String uuid = UUID.toString();
		log.info("Serving POST /networks/" + uuid + "/csv " + (user != null ? " for user " + user : ""));
		
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		
		// 3. Force copy if network is from public user!
		if (this.configService.isPublicUser(networkUser) && !derive) {
			derive = true;
			log.warn("Overriding derive parameter because given user is the public user and network has to be derived");
		}
		// 4. add the data to the network
		MappingNode newNetwork;
		try {
			newNetwork = this.networkService.addCsvDataToNetwork(user != null ? user.strip() : networkUser, data, type, uuid, networkname, prefixName, derive);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		} catch (IOException e) {
			return ResponseEntity.badRequest()
					.header("reason", "Could not read input file: " + e.getMessage())
					.build();
		}
		
		return ResponseEntity.ok(this.networkService.getNetworkInventoryItem(newNetwork.getEntityUUID()));
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> addMyDrugRelations(UUID UUID, @NotNull @Valid String myDrugURL,
			String user, @Valid String networkname, @Valid Boolean prefixName, @Valid Boolean derive) {
		
		String uuid = UUID.toString();
		log.info("Serving POST /networks/" + uuid + "/mydrug " + (user != null ? " for user " + user : "")+ " with myDrugURL " + myDrugURL);
		
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		
		// 3. Force copy if network is from public user!
		if (this.configService.isPublicUser(networkUser) && !derive) {
			derive = true;
			log.warn("Overriding derive parameter because given user is the public user and network has to be derived");
		}
		// 4. add the data to the network
		MappingNode newNetwork;
		// 4. Add MyDrug Nodes/Edges
		try {
			newNetwork = this.myDrugService.addMyDrugToNetwork(user != null ? user.strip() : networkUser, uuid, myDrugURL, networkname, prefixName, derive);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 5. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(newNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> copyNetwork(@NotNull @Valid UUID parentUUID, String user,
			@Valid String networkname, @Valid Boolean prefixName, @Valid Boolean suffixName) {
		String uuid = parentUUID.toString();
		log.info("Serving POST /networks?parentUUID=" + uuid + (user != null ? " for user " + user : ""));
		
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		
		// 3. Determine the name of the network
		String copiedNetworkName;
		if (prefixName && networkname != null) {
			copiedNetworkName = (networkname.endsWith("_") ? networkname : networkname + "_") + mappingForUUID.getMappingName();
		} else if (suffixName && networkname != null) { 
			copiedNetworkName = mappingForUUID.getMappingName() + (networkname.startsWith("_") ? networkname : "_" + networkname);
		} else if (networkname == null && (prefixName || suffixName)) {
			copiedNetworkName = (prefixName ? "CopyOf_" : "") + mappingForUUID.getMappingName() +(suffixName ? "_copy" : "");
		} else {
			copiedNetworkName = networkname;
		}
		MappingNode newNetwork;
		// 4. copy the network
		try {
			newNetwork = this.networkService.getCopiedOrNamedMappingNode(networkUser, uuid, copiedNetworkName, false, true, "CopyOf_");
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 5. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(newNetwork.getEntityUUID()), HttpStatus.CREATED);
		
	}
	
	@Override
	public ResponseEntity<Void> deleteNetwork(String user, UUID UUID) {
		
		String uuid = UUID.toString();
		log.info("Serving DELETE /networks/" + uuid + (user != null ? " for user " + user : ""));
		
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 3. Is this a public user network?
		if (this.configService.isPublicUser(networkUser) ) {
			// 3. Is user allowed to delete a public network, i.e. was the public user given in request and is config set to true?
			if (!this.configService.isAllowedToDeletePublicNetwork(user)) {
				return ResponseEntity.badRequest()
						.header("reason", "Tried to delete public network, but did not pass public user and set correct config. Denying deletion.")
						.build();
			}
		}
		// 4. Delete network / Deactivate network
		boolean isDeleted = false;
		if (sbml4jConfig.getNetworkConfigProperties().isHardDelete()) {
			isDeleted = this.networkService.deleteNetwork(UUID.toString());
		} else {
			isDeleted = this.networkService.deactivateNetwork(UUID.toString());
		}
		if(isDeleted) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().header("reason", "User was authorized to delete existing network, but attempt failed due to unknown error.").build();
		}
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> filterNetwork(UUID UUID, @Valid FilterOptions filterOptions,
			String user, @Valid String networkname, @Valid Boolean prefixName) {
		
		String uuid = UUID.toString();
		log.info("Serving POST /networks/" + uuid + "/filter" + (user != null ? " for user " + user : "") + " with filterOptions " + filterOptions.toString());
		
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(uuid) == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
				
		// 3. Filter the network
		MappingNode filteredNetwork;
		try {
			filteredNetwork = this.networkService.filterNetwork(user != null ? user.strip() : networkUser,
												filterOptions, uuid, networkname, prefixName);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
	
		// 4. Network not created?
		if (filteredNetwork == null) {
			return ResponseEntity.badRequest().header("reason", "There has been an error filtering the network with entityUUID: " + uuid).build();
		}
		// 5. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(filteredNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	@Override
	public ResponseEntity<Resource> getContext(UUID UUID, @NotNull @Valid String genes, String user,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid String terminateAt, @Valid String direction,
			@Valid Boolean directed, @Valid String weightproperty) {
		
		String uuid = UUID.toString();
		log.info("Serving GET /networks/" + uuid + "/context " + (user != null ? " for user " + user : "") + " with genes " + genes);
		// 1. Does the network exist?
		MappingNode mappingForContext = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForContext == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		try {
			this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 3. Extract the genes
		List<String> geneNames = Arrays.asList(genes.split(", "));
		if(geneNames.size() < 1) {
			return ResponseEntity.badRequest().header("reason", "Must give at least on gene").build();
		}
		
		// 4. Check the input parameters
		int minDepth; 
		if (minSize != null) {
			minDepth = minSize.intValue();
		} else if (this.configService.getContextMinSize() != null) {
			minDepth = this.configService.getContextMinSize().intValue();
		} else {
			return ResponseEntity.badRequest()
					.header("reason", "No minSize-parameter given and no parameter value set in config. Please provide either one")
					.build();
		}
		int maxDepth; 
		if (maxSize != null) {
			maxDepth = maxSize.intValue();
		} else if (this.configService.getContextMaxSize() != null) {
			maxDepth = this.configService.getContextMaxSize().intValue();
		} else {
			return ResponseEntity.badRequest()
					.header("reason", "No maxSize-parameter given and no parameter value set in config. Please provide either one")
					.build();
		}
		String terminateAtString;
		if (terminateAt == null && this.configService.getContextTerminateAt() != null) {
			terminateAtString = this.configService.getContextTerminateAt();
		} else if (terminateAt != null) {
			terminateAtString = terminateAt;
		} else {
			terminateAtString = null;
		}
		String directionString;
		if (direction != null) {
			directionString = direction;
		} else if (this.configService.getContextDirection() != null) {
			directionString = this.configService.getContextDirection();
		} else {
			return ResponseEntity.badRequest()
					.header("reason", "No direction-parameter given and no parameter value set in config. Please provide either one")
					.build();
		}
		// 5. Get the context
		List<FlatEdge> contextFlatEdges = this.networkService.getNetworkContextFlatEdges(uuid, geneNames, minDepth, maxDepth, terminateAtString, directionString, weightproperty);
		if(contextFlatEdges == null) {
			return ResponseEntity.badRequest().header("reason", "Failed to gather context from parent network").build();
		}
		// 6. Create a filename for the output file
		StringBuilder filename = new StringBuilder();
		filename.append("context");
		for (String gene : geneNames) {
			filename.append("_");
			filename.append(gene);
		}
		filename.append("_IN_");
		filename.append(mappingForContext.getMappingName());
		filename.append(".graphml");
		// 6. Get the Resource containing the GraphML of the context
		Resource contextResource = this.networkResourceService.getNetworkForFlatEdges(contextFlatEdges, directed, filename.toString());
		if(contextResource == null) {
			return ResponseEntity.badRequest().header("reason", "Failed to create graphML resource").build();
		}
		log.info("Finished creating graphml resource for context.");
		return ResponseEntity.ok(contextResource);
	}
	
	@Override
	public ResponseEntity<Resource> getNetwork(UUID UUID, String user, @Valid Boolean directed) {
		String uuid = UUID.toString();	
		log.info("Serving GET /networks/" + uuid + (user != null ? " for user " + user : ""));
		
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(uuid) == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		try {
			this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 3. Get the network as a GraphML Resource
		Resource networkResource = this.networkResourceService.getNetwork(uuid, directed);
		if (networkResource == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(networkResource);
	}
	
	@Override
	public ResponseEntity<NetworkOptions> getNetworkOptions(UUID UUID, String user) {
		
		String uuid = UUID.toString();
		log.info("Serving GET /networks/" + uuid + "/options" + (user != null ? " for user " + user : ""));
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		try {
			this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 3. Get the network options
		NetworkOptions networkOptions = this.mappingNodeService.getNetworkOptions(uuid);
		if (networkOptions.getAnnotation() == null || networkOptions.getFilter() == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(networkOptions);
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> listAllNetworks(String user) {
		log.info("Serving GET /networks" + (user != null ? " for user " + user : ""));
		// 0. Get list of users including the public user
		List<String> userList = this.configService.getUserList(user);
		if(userList.size() < 1) {
			return ResponseEntity.badRequest().header("reason", "Bad username given: " + user + " and no public user configured. Set config parameter sbml4j.networks.public-user and/or give proper username to prevent this.").build();
		}
		// 1. Find all networks for that user
		List<NetworkInventoryItem> userNetworkInventoryItems = new ArrayList<>();
		List<MappingNode> userNetworks = this.mappingNodeService.findAllFromUsers(userList, !this.sbml4jConfig.getNetworkConfigProperties().isShowInactiveNetworks());
		// 2. Get NetworkInventoryItem for each of them
		for (MappingNode userMap : userNetworks) {
			NetworkInventoryItem item = this.networkService.getNetworkInventoryItem(userMap.getEntityUUID());
			userNetworkInventoryItems.add(item);
		}
		return ResponseEntity.ok(userNetworkInventoryItems);
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> postContext(UUID UUID, @Valid NodeList nodeList, String user,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid String terminateAt, @Valid String direction,
			@Valid String networkname, @Valid Boolean prefixName, @Valid String weightproperty) {
		String uuid = UUID.toString();
		log.info("Serving POST /networks/" + uuid + "/context" + (user != null ? " for user " + user : "")+ " with NodeList " + nodeList.toString());
		// 0. Extract the genes
		List<String> geneNames = nodeList.getGenes();
		if(geneNames.size() < 1) {
			return ResponseEntity.badRequest().header("reason", "Must give at least one gene in body.").build();
		}
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 4. Check the input parameters
		int minDepth; 
		if (minSize != null) {
			minDepth = minSize.intValue();
		} else if (this.configService.getContextMinSize() != null) {
			minDepth = this.configService.getContextMinSize().intValue();
		} else {
			return ResponseEntity.badRequest()
					.header("reason", "No minSize-parameter given and no parameter value set in config. Please provide either one")
					.build();
		}
		int maxDepth; 
		if (maxSize != null) {
			maxDepth = maxSize.intValue();
		} else if (this.configService.getContextMaxSize() != null) {
			maxDepth = this.configService.getContextMaxSize().intValue();
		} else {
			return ResponseEntity.badRequest()
					.header("reason", "No maxSize-parameter given and no parameter value set in config. Please provide either one")
					.build();
		}
		String terminateAtString;
		if (terminateAt != null) {
			terminateAtString = terminateAt;
		} else if (terminateAt == null && this.configService.getContextTerminateAt() != null) {
			terminateAtString = this.configService.getContextTerminateAt();
		} else {
			terminateAtString = null;
		}
		String directionString;
		if (direction != null) {
			directionString = direction;
		} else if (this.configService.getContextDirection() != null) {
			directionString = this.configService.getContextDirection();
		} else {
			return ResponseEntity.badRequest()
					.header("reason", "No direction-parameter given and no parameter value set in config. Please provide either one")
					.build();
		}

		// 3. Create the context network
		MappingNode contextNetwork;
		try {
			contextNetwork = this.networkService.createContextNetwork(user != null ? user.strip() : networkUser, geneNames, mappingForUUID, minDepth, maxDepth, terminateAtString,
					directionString, weightproperty, networkname, prefixName);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		} catch (Exception e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 7. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(contextNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
}
