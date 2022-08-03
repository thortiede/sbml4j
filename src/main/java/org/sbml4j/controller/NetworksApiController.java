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
package org.sbml4j.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.sbml4j.Exception.AnnotationException;
import org.sbml4j.Exception.NetworkAlreadyExistsException;
import org.sbml4j.Exception.NetworkDeletionException;
import org.sbml4j.Exception.UserUnauthorizedException;
import org.sbml4j.api.NetworksApi;
import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.api.network.AnnotationItem;
import org.sbml4j.model.api.network.FilterOptions;
import org.sbml4j.model.api.network.NetworkInventoryItem;
import org.sbml4j.model.api.network.NetworkOptions;
import org.sbml4j.model.api.network.NodeList;
import org.sbml4j.model.api.provenance.ProvenanceInfoItem;
import org.sbml4j.model.base.GraphEnum.Operation;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.FlatSpeciesService;
import org.sbml4j.service.MyDrugService;
import org.sbml4j.service.ProvenanceGraphService;
import org.sbml4j.service.WarehouseGraphService;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.sbml4j.service.networks.NetworkResourceService;
import org.sbml4j.service.networks.NetworkService;
import org.sbml4j.service.utility.FileCheckService;
import org.sbml4j.service.warehouse.MappingNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	FileCheckService fileCheckService;
	
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
		
		Operation op = Operation.POST;
		String endpoint = "/networks/" + uuid + "/annotation";
		
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : "") + " with AnnotationItem " + annotationItem.toString());
		
		MappingNode oldMapping = this.mappingNodeService.findByEntityUUID(uuid);
		// 1. Does the network exist?
		if (oldMapping == null) {
			return ResponseEntity.notFound().build();
		}
		String oldMappingName = oldMapping.getMappingName();
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
		} catch (NetworkDeletionException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
	
		// 5. get the networkInventoryItem
		NetworkInventoryItem item = this.networkService.getNetworkInventoryItem(annotatedNetwork.getEntityUUID());
		
		// 6. Add the provenance annotation
		// get the activity node
		Iterable<ProvenanceEntity> allWasGeneratedByProvenanceEntityNodes = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, annotatedNetwork.getEntityUUID());
		for (ProvenanceEntity provEntity : allWasGeneratedByProvenanceEntityNodes) {
			try {
				ProvenanceGraphActivityNode activity = (ProvenanceGraphActivityNode) provEntity;
				if(activity.getGraphActivityType().equals(ProvenanceGraphActivityType.addJsonAnnotation) 
						&& activity.getGraphActivityName().equals(oldMappingName + "-" + ProvenanceGraphActivityType.addJsonAnnotation + (derive ? "->" : "|noDerive->") + annotatedNetwork.getMappingName())) {
					// Add provenance annotation
					Map<String, Map<String, Object>> provenanceAnnotation = new HashMap<>();

					Map<String, Object> paramsMap = new HashMap<>();
					paramsMap.put("user", user);
					paramsMap.put("UUID", uuid);
					paramsMap.put("networkname", networkname);
					paramsMap.put("derive", derive);
					paramsMap.put("prefixName", prefixName);
					provenanceAnnotation.put("params",  paramsMap);	
					
					Map<String, Object> endpointMap = new HashMap<>();
					endpointMap.put("operation", op.getOperation());
					endpointMap.put("endpoint", endpoint);
					
					provenanceAnnotation.put("endpoint",  endpointMap);
					
					Map<String, Object> bodyMap = new HashMap<>();
					String bodyString = new ObjectMapper().writeValueAsString(annotationItem);
					bodyMap.put("body", bodyString);
					provenanceAnnotation.put("body", bodyMap);
					this.provenanceGraphService.addProvenanceAnnotationMap(activity, provenanceAnnotation);
										
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
				log.warn("Found ProvenanceEntity connected by wasGeneratedBy which is not a ProvenanceGraphActivityNode. The entityUUID is: " + provEntity.getEntityUUID());
			} catch (JsonProcessingException e1) {
				log.warn("Could not create body-json-string from AnnotationItem: " + annotationItem.toString());
			}
		}
		
		// 7. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(item, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<NetworkInventoryItem> addCsvDataToNetwork(UUID UUID, @NotNull @Valid String type, String user,
			@Valid String networkname, @Valid Boolean prefixName, @Valid Boolean derive,
			@Valid List<MultipartFile> data) {
		String uuid = UUID.toString();
		Operation op = Operation.POST;
		String endpoint = "/networks/" + uuid + "/csv";
		
		
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : ""));
		
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		String oldMappingName = mappingForUUID.getMappingName();
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
		String bodyString = "{\nFiles:[";
		
		try {
			boolean first = true;
			for (MultipartFile file : data) {
				if (!first) bodyString.concat(", ");
				bodyString.concat("\n{\nFilename:");
				bodyString.concat(file.getOriginalFilename());
				bodyString.concat(",\n");
				bodyString.concat("MD5Sum:");
				bodyString.concat(this.fileCheckService.getMD5Sum(file));
				bodyString.concat("\n}");
			}
			bodyString.concat("\n]");
			newNetwork = this.networkService.addCsvDataToNetwork(user != null ? user.strip() : networkUser, data, type, uuid, networkname, prefixName, derive);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		} catch (IOException e) {
			return ResponseEntity.badRequest()
					.header("reason", "Could not read input file: " + e.getMessage())
					.build();
		} catch (NetworkDeletionException e) {
			return ResponseEntity.badRequest()
					.header("reason", "Could not read input file: " + e.getMessage())
					.build();
		}
		
		// 5. Add the provenance annotation
		// get the activity node
		String newNetworkEntityUUID = newNetwork.getEntityUUID();
		String newNetworkMappingName = newNetwork.getMappingName();
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.addCsvAnnotation;
		
		Iterable<ProvenanceEntity> allWasGeneratedByProvenanceEntityNodes = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, newNetworkEntityUUID);
		for (ProvenanceEntity provEntity : allWasGeneratedByProvenanceEntityNodes) {
			try {
				ProvenanceGraphActivityNode activity = (ProvenanceGraphActivityNode) provEntity;
				
				if(activity.getGraphActivityType().equals(activityType) 
						&& activity.getGraphActivityName().equals(oldMappingName + "-" + activityType + (derive ? "->" : "|noDerive->") + newNetworkMappingName)) {
					
					
					// Add provenance annotation
					Map<String, Map<String, Object>> provenanceAnnotation = new HashMap<>();

					Map<String, Object> paramsMap = new HashMap<>();
					paramsMap.put("user", user);
					paramsMap.put("UUID", uuid);
					paramsMap.put("networkname", networkname);
					paramsMap.put("derive", derive);
					paramsMap.put("prefixName", prefixName);
					paramsMap.put("type", type);
					provenanceAnnotation.put("params",  paramsMap);	
					
					Map<String, Object> endpointMap = new HashMap<>();
					endpointMap.put("operation", op.getOperation());
					endpointMap.put("endpoint", endpoint);
					
					provenanceAnnotation.put("endpoint",  endpointMap);
					
					Map<String, Object> bodyMap = new HashMap<>();
					
					bodyMap.put("body", bodyString);
					provenanceAnnotation.put("body", bodyMap);
					this.provenanceGraphService.addProvenanceAnnotationMap(activity, provenanceAnnotation);
					
					
					
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
				log.warn("Found ProvenanceEntity connected by wasGeneratedBy which is not a ProvenanceGraphActivityNode. The entityUUID is: " + provEntity.getEntityUUID());
			}
		}
		// 6. Return the InventoryItem of the new Network
		return ResponseEntity.ok(this.networkService.getNetworkInventoryItem(newNetworkEntityUUID));
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> addMyDrugRelations(UUID UUID, @NotNull @Valid String myDrugURL,
			String user, @Valid String networkname, @Valid Boolean prefixName, @Valid Boolean derive) {
		
		String uuid = UUID.toString();
		Operation op = Operation.POST;
		String endpoint = "/networks/" + uuid + "/mydrug";
		
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : "")+ " with myDrugURL " + myDrugURL);
		
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		String oldMappingName = mappingForUUID.getMappingName();
		
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
		// 5. Add MyDrug Nodes/Edges
		try {
			newNetwork = this.myDrugService.addMyDrugToNetwork(user != null ? user.strip() : networkUser, uuid, myDrugURL, networkname, prefixName, derive);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		} catch (NetworkDeletionException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 6. Add the provenance annotation
		// get the activity node
		String newNetworkEntityUUID = newNetwork.getEntityUUID();
		String newNetworkMappingName = newNetwork.getMappingName();
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.addMyDrugNodes;
		
		Iterable<ProvenanceEntity> allWasGeneratedByProvenanceEntityNodes = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, newNetworkEntityUUID);
		for (ProvenanceEntity provEntity : allWasGeneratedByProvenanceEntityNodes) {
			try {
				ProvenanceGraphActivityNode activity = (ProvenanceGraphActivityNode) provEntity;
				
				if(activity.getGraphActivityType().equals(activityType) 
						&& activity.getGraphActivityName().equals(oldMappingName + "-" + activityType + (derive ? "->" : "|noDerive->") + newNetworkMappingName)) {

					// Add provenance annotation
					Map<String, Map<String, Object>> provenanceAnnotation = new HashMap<>();

					Map<String, Object> paramsMap = new HashMap<>();
					paramsMap.put("user", user);
					paramsMap.put("UUID", uuid);
					paramsMap.put("networkname", networkname);
					paramsMap.put("derive", derive);
					if (prefixName != null) paramsMap.put("prefixName", prefixName);
					paramsMap.put("myDrugURL", myDrugURL);
					provenanceAnnotation.put("params",  paramsMap);	
					
					Map<String, Object> endpointMap = new HashMap<>();
					endpointMap.put("operation", op.getOperation());
					endpointMap.put("endpoint", endpoint);
					
					provenanceAnnotation.put("endpoint",  endpointMap);
					
					this.provenanceGraphService.addProvenanceAnnotationMap(activity, provenanceAnnotation);
					
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
				log.warn("Found ProvenanceEntity connected by wasGeneratedBy which is not a ProvenanceGraphActivityNode. The entityUUID is: " + provEntity.getEntityUUID());
			}
		}
		
		// 7. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(newNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> copyNetwork(@NotNull @Valid UUID parentUUID, String user,
			@Valid String networkname, @Valid Boolean prefixName, @Valid Boolean suffixName) {
		String uuid = parentUUID.toString();
		Operation op = Operation.POST;
		String endpoint = "/networks";
		
		log.info("Serving " + op.getOperation() + " " + endpoint + "?parentUUID=" + uuid + (user != null ? " for user " + user : ""));
		
		
		// 0. Assemble information to store for the provenance
		
		
		// 1. Does the network exist?
		MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(uuid);
		if (mappingForUUID == null) {
			return ResponseEntity.notFound().build();
		}
		String oldMappingName = mappingForUUID.getMappingName();
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
			newNetwork = this.networkService.getCopiedOrNamedMappingNode(networkUser, uuid, copiedNetworkName, false, true, "CopyOf_", ProvenanceGraphActivityType.copyNetwork);
		} catch (NetworkAlreadyExistsException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		} catch (NetworkDeletionException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		
		// 5. Add the provenance annotation
		// get the activity node
		String newNetworkEntityUUID = newNetwork.getEntityUUID();
		String newNetworkMappingName = newNetwork.getMappingName();
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.copyNetwork;
		
		Iterable<ProvenanceEntity> allWasGeneratedByProvenanceEntityNodes = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, newNetworkEntityUUID);
		for (ProvenanceEntity provEntity : allWasGeneratedByProvenanceEntityNodes) {
			try {
				ProvenanceGraphActivityNode activity = (ProvenanceGraphActivityNode) provEntity;
				
				if(activity.getGraphActivityType().equals(activityType) 
						&& activity.getGraphActivityName().equals(oldMappingName + "-" + activityType +  "->" + newNetworkMappingName)) {

					// Add provenance annotation
					Map<String, Map<String, Object>> provenanceAnnotation = new HashMap<>();

					Map<String, Object> paramsMap = new HashMap<>();
					paramsMap.put("user", user);
					paramsMap.put("parentUUID", uuid);
					paramsMap.put("networkname", networkname);
					if (prefixName != null) paramsMap.put("prefixName", prefixName);
					if (prefixName != null) paramsMap.put("suffixName", suffixName);

					provenanceAnnotation.put("params",  paramsMap);	
					
					Map<String, Object> endpointMap = new HashMap<>();
					endpointMap.put("operation", op.getOperation());
					endpointMap.put("endpoint", endpoint);
					
					provenanceAnnotation.put("endpoint",  endpointMap);

					this.provenanceGraphService.addProvenanceAnnotationMap(activity, provenanceAnnotation);
					
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
				log.warn("Found ProvenanceEntity connected by wasGeneratedBy which is not a ProvenanceGraphActivityNode. The entityUUID is: " + provEntity.getEntityUUID());
			}
		}
		
		// 6. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(newNetwork.getEntityUUID()), HttpStatus.CREATED);
		
	}
	
	@Override
	public ResponseEntity<Void> deleteNetwork(String user, UUID UUID) {
		
		String uuid = UUID.toString();
		Operation op = Operation.DELETE;
		String endpoint = "/networks/" + uuid;
		
		log.info("Serving " + op + " " + endpoint +  (user != null ? " for user " + user : ""));
		
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
			try {
				isDeleted = this.networkService.deleteNetwork(UUID.toString());
			} catch (NetworkDeletionException e) {
				e.printStackTrace();
				return ResponseEntity.badRequest().header("reason", "Failed to delete existing network with uuid: " + UUID.toString() + ".Additional Info: " + e.getMessage()).build();
			}
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
		
		Operation op = Operation.POST;
		String endpoint = "/networks/" + uuid+ "/filter";
		
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : "") + " with filterOptions " + filterOptions.toString());
		
		// 1. Does the network exist?
		MappingNode oldMapping = this.mappingNodeService.findByEntityUUID(uuid);
		if (oldMapping == null) {
			return ResponseEntity.notFound().build();
		}
		String oldMappingName = oldMapping.getMappingName();
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
		} catch (NetworkDeletionException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
	
		// 4. Network not created?
		if (filteredNetwork == null) {
			return ResponseEntity.badRequest().header("reason", "There has been an error filtering the network with entityUUID: " + uuid).build();
		}
		// 6. Add the provenance annotation
		// get the activity node
		String newNetworkEntityUUID = filteredNetwork.getEntityUUID();
		String newNetworkMappingName = filteredNetwork.getMappingName();
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.filterNetwork;
		
		Iterable<ProvenanceEntity> allWasGeneratedByProvenanceEntityNodes = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, newNetworkEntityUUID);
		for (ProvenanceEntity provEntity : allWasGeneratedByProvenanceEntityNodes) {
			try {
				ProvenanceGraphActivityNode activity = (ProvenanceGraphActivityNode) provEntity;
				
				if(activity.getGraphActivityType().equals(activityType) 
						&& activity.getGraphActivityName().equals(oldMappingName + "-" + activityType + "->" + newNetworkMappingName)) {

					// Add provenance annotation
					Map<String, Map<String, Object>> provenanceAnnotation = new HashMap<>();

					Map<String, Object> paramsMap = new HashMap<>();
					paramsMap.put("user", user);
					paramsMap.put("UUID", uuid);
					paramsMap.put("networkname", networkname);
					if (prefixName != null) paramsMap.put("prefixName", prefixName);

					provenanceAnnotation.put("params",  paramsMap);	
					
					Map<String, Object> endpointMap = new HashMap<>();
					endpointMap.put("operation", op.getOperation());
					endpointMap.put("endpoint", endpoint);
					
					provenanceAnnotation.put("endpoint",  endpointMap);
					
					Map<String, Object> bodyMap = new HashMap<>();
					String bodyString = new ObjectMapper().writeValueAsString(filterOptions);
					bodyMap.put("body", bodyString);
					provenanceAnnotation.put("body", bodyMap);
					this.provenanceGraphService.addProvenanceAnnotationMap(activity, provenanceAnnotation);
					
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
				log.warn("Found ProvenanceEntity connected by wasGeneratedBy which is not a ProvenanceGraphActivityNode. The entityUUID is: " + provEntity.getEntityUUID());
			} catch (JsonProcessingException e) {
				log.warn("Could not create body-json-string from FilterOptions: " + filterOptions.toString());
			}
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
	public ResponseEntity<ProvenanceInfoItem> getProvenanceInformation(UUID UUID, String user) {
		String uuid = UUID.toString();
		log.info("Serving GET /networks/" + uuid + "/prov" + (user != null ? " for user " + user : ""));
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
		// 3. Create the ProvenanceInfoItem
		//ProvenanceInfoItem item = new ProvenanceInfoItem();
		// and Fill the ProvenanceInfoItem with provenance content
		ProvenanceInfoItem item = this.provenanceGraphService.getProvenanceInfoItem(uuid);
		
		
		return ResponseEntity.ok(item);
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
		
		Operation op = Operation.POST;
		String endpoint = "/networks/" + uuid + "/context";
		
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : "")+ " with NodeList " + nodeList.toString());
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
		String oldMappingName = mappingForUUID.getMappingName();
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 3. Check the input parameters
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

		// 4. Create the context network
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
		
		// 5. Add the provenance annotation
		// get the activity node
		String newNetworkEntityUUID = contextNetwork.getEntityUUID();
		String newNetworkMappingName = contextNetwork.getMappingName();
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createContext;
		
		Iterable<ProvenanceEntity> allWasGeneratedByProvenanceEntityNodes = this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, newNetworkEntityUUID);
		for (ProvenanceEntity provEntity : allWasGeneratedByProvenanceEntityNodes) {
			try {
				ProvenanceGraphActivityNode activity = (ProvenanceGraphActivityNode) provEntity;
				
				if(activity.getGraphActivityType().equals(activityType) 
						&& activity.getGraphActivityName().equals(oldMappingName + "-" + activityType + "->" + newNetworkMappingName)) {
					
					// Add provenance annotation
					Map<String, Map<String, Object>> provenanceAnnotation = new HashMap<>();

					Map<String, Object> paramsMap = new HashMap<>();
					paramsMap.put("user", user);
					paramsMap.put("UUID", uuid);
					if (minSize != null) paramsMap.put("minSize", minSize);
					if (maxSize != null) paramsMap.put("maxSize", maxSize);
					if (terminateAt != null) paramsMap.put("terminateAt", terminateAt);
					if (direction != null) paramsMap.put("direction", direction);
					if (weightproperty != null) paramsMap.put("weightproperty", weightproperty);
					
					paramsMap.put("networkname", networkname);
					if (prefixName != null) paramsMap.put("prefixName", prefixName);

					provenanceAnnotation.put("params",  paramsMap);	
					
					Map<String, Object> endpointMap = new HashMap<>();
					endpointMap.put("operation", op.getOperation());
					endpointMap.put("endpoint", endpoint);
					
					provenanceAnnotation.put("endpoint",  endpointMap);
					
					Map<String, Object> bodyMap = new HashMap<>();
					String bodyString = new ObjectMapper().writeValueAsString(nodeList);
					bodyMap.put("body", bodyString);
					provenanceAnnotation.put("body", bodyMap);
					this.provenanceGraphService.addProvenanceAnnotationMap(activity, provenanceAnnotation);
					
				}
			} catch (ClassCastException e) {
				e.printStackTrace();
				log.warn("Found ProvenanceEntity connected by wasGeneratedBy which is not a ProvenanceGraphActivityNode. The entityUUID is: " + provEntity.getEntityUUID());
			} catch (JsonProcessingException e) {
				log.warn("Could not create body-json-string from NodeList: " + nodeList.toString());
			}
		}
		
		// 6. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(contextNetwork.getEntityUUID()), HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<ProvenanceInfoItem> putProvenanceInformation(UUID UUID, @NotNull @Valid String name,
			@Valid Map<String, Object> requestBody, String user) {

		String uuid = UUID.toString();
		Operation op = Operation.PUT;
		String endpoint = "/networks/" + uuid + "/prov";
		
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : ""));
		
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
		
		Map<String, Map<String, Object>> provenanceAnnotationMap = new HashMap<>();
		provenanceAnnotationMap.put(name, requestBody);
		
		// find latest activity for network and add the annotation to it
		ProvenanceEntity lastActivity = this.provenanceGraphService.findLatestGraphActivityNodeForWarehouseEntity(uuid);
				
		
		if (!this.provenanceGraphService.addProvenanceAnnotationMap(lastActivity, provenanceAnnotationMap)) {
			return ResponseEntity.badRequest().header("reason", "Unable to add provenance annotation to mapping").build(); // TODO: Give better reason
		} else {
			return this.getProvenanceInformation(UUID, networkUser);
		}
	}
}
