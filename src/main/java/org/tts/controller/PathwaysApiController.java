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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.Exception.NetworkMappingError;
import org.tts.Exception.UserUnauthorizedException;
import org.tts.api.PathwaysApi;
import org.tts.model.api.PathwayCollectionCreationItem;
import org.tts.model.api.PathwayInventoryItem;
import org.tts.model.api.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.service.ConfigService;
import org.tts.service.NetworkMappingService;
import org.tts.service.PathwayService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.WarehouseGraphService;
import org.tts.service.warehouse.DatabaseNodeService;
import org.tts.service.warehouse.MappingNodeService;
import org.tts.service.warehouse.PathwayCollectionNodeService;

/**
 * Controller for handling Pathway requests
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class PathwaysApiController implements PathwaysApi {
	
	@Autowired
	ConfigService configService;
	
	@Autowired
	DatabaseNodeService databaseNodeService;

	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkMappingService networkMappingService;
	
	@Autowired
	PathwayCollectionNodeService pathwayCollectionNodeService;
	
	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	
	Logger log = LoggerFactory.getLogger(PathwaysApiController.class);
	
	/**
	 * Create a <a href="#{@link}">{@link PathayNode}</a>  as a collection from the submitted pathways
	 * 
	 * @param body The <a href="#{@link}">{@link PathayCollectionCreationItem}</a> to create the collection with
	 * @param user The user to associate with the created pathway
	 * @return The UUID of the created Pathway
	 */
	@Override
	public ResponseEntity<UUID> createPathwayCollection(
			@Valid PathwayCollectionCreationItem pathwayCollectionCreationItem, String user) {
		log.info("Serving POST /pathwayCollection" + (user != null ? " for user " + user : ""));
		Map<String, Object> agentNodeProperties = new HashMap<>();
		
		if (user == null) {
			user = this.configService.getPublicUser();
		}
		if (user == null) {
			return ResponseEntity.badRequest().header("reason", "Unable to create PathwayCollection without a user. Either provide one in the header, or configure one in the properties.").build();
		}
		agentNodeProperties.put("graphagentname", user);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(agentNodeProperties);

		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createKnowledgeGraph);
		String activityName = "Create_PathwayCollection_for";
		List<String> pathwayUUIDStrings = new ArrayList<>();
		pathwayCollectionCreationItem.getSourcePathwayUUIDs().forEach(e -> pathwayUUIDStrings.add(e.toString()));
		
		for (String uuid : pathwayUUIDStrings) {
			activityName += ("_" + uuid.substring(0, 8));
		}
		activityNodeProvenanceProperties.put("graphactivityname", activityName);

		ProvenanceGraphActivityNode createKnowledgeGraphActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, userAgentNode,
				ProvenanceGraphEdgeType.wasAssociatedWith);
		List<DatabaseNode> databaseList = this.databaseNodeService.findByPathwayList(pathwayUUIDStrings);
		DatabaseNode database;
		if (databaseList.size() < 1) {
			log.error("Found no database connected to the given pathways. This is fatal. Aborting.");
			return ResponseEntity.badRequest().header("reason", "Cannot create pathwayCollection, as no DatabaseNode was found connected to the input pathways").build();
		} else if (databaseList.size() > 1) {
			// the pathways in the collection stem from different databases.
			// TODO:Deal with this scenario.
			log.warn("Found multiple databases for one PathwayCollection to be created. This cannot be handled yet. If you see this warning, please open an issue on github. For now, taking the first Database node in the list: " 
						+databaseList.get(0).getSource() + "-" + databaseList.get(0).getSourceVersion());
		}
		// now we either have one DatabaseNode (what we expect) or multiple, use the first element in the List.
		database = databaseList.get(0);
		
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, database, ProvenanceGraphEdgeType.used);
		
		// create a pathwayCollectionNode
		PathwayCollectionNode pathwayCollectionNode = this.pathwayCollectionNodeService.createPathwayCollection(
				pathwayCollectionCreationItem, database, createKnowledgeGraphActivityNode, userAgentNode);

		// then create the pathway to the collection by iterating over all pathways in
		// the collection and adding them to the pathway via CONTAINS
		PathwayNode pathwayNode = this.pathwayService.createPathwayNode(
				pathwayCollectionCreationItem.getName(),
				pathwayCollectionCreationItem.getDescription(), database.getOrganism());
		this.provenanceGraphService.connect(pathwayNode, pathwayCollectionNode, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(pathwayNode, createKnowledgeGraphActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy);
		this.provenanceGraphService.connect(pathwayNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);

		pathwayNode = this.pathwayCollectionNodeService.buildPathwayFromCollection(
				pathwayNode, pathwayCollectionNode, createKnowledgeGraphActivityNode, userAgentNode);
		return new ResponseEntity<UUID>(UUID.fromString(pathwayNode.getEntityUUID()), HttpStatus.CREATED);
	}
   
	
	/**
	 * Get a list of <a href="#{@link}">{@link PathwayInventoryItem}</a>  for all Pathways associated to a user
	 * 
	 * @param user The user for which the pathways are to be fetched
	 * @param hideCollections Hide collection pathways from the output (true), or include them (false)
	 * @return List of <a href="#{@link}">{@link PathwayInventoryItem}</a> of the pathways for user user
	 */
	@Override
	public ResponseEntity<List<PathwayInventoryItem>> listAllPathways(String user, @Valid Boolean hideCollections) {
	   log.info("Serving GET /pathways" + (user != null ? " for user " + user : ""));
	   
	   // 0. Get list of users including the public user
	   List<String> userList = this.configService.getUserList(user);
	   
		return new ResponseEntity<List<PathwayInventoryItem>>(
				this.pathwayService.getListofPathwayInventory(userList, hideCollections), HttpStatus.OK);
	}
   
   /**
    * Get a list of UUIDs of Pathways for user user
    * 
    * @param user The user for which the pathwayUUIDs are to be fetched
    * @param hideCollections Hide collection pathways from the output (true), or include them (false)
    * @Return A List of UUIDs of <a href="#{@link}">{@link PathwayNode}</a> 
    */
	@Override
	public ResponseEntity<List<UUID>> listAllPathwayUUIDs(String user, @Valid Boolean hideCollections) {
	   log.info("Serving POST /pathwayUUIDs" + (user != null ? " for user " + user : ""));
	   // 0. Get list of users including the public user
	   List<String> userList = this.configService.getUserList(user);
	   
	   return new ResponseEntity<List<UUID>>(this.pathwayService.getListofPathwayUUIDs(userList, hideCollections), HttpStatus.OK);
	}
   
	/**
	 * Endpoint for mapping a Pathway to a <a href="#{@link}">{@link MappingNode}</a>
	 * @param user The user which requests this mapping and is associated with it
	 * @param uuid The UUID of the <a href="#{@link}">{@link PathwayNpde}</a> to derive the mapping from
	 * @param mappingType The <a href="#{@link}">{@link NetworkMappingType}</a> for thw new NetworkMapping
	 * @return The <a href="#{@link}">{@link WarehouseInventoryItem}</a> of the created Mapping
	 */
	@Override
	public ResponseEntity<WarehouseInventoryItem> mapPathway(UUID UUID, @NotNull @Valid String mappingType, String user,
		@Valid String networkname, @Valid Boolean prefixName, @Valid Boolean suffixName) {
		
		String uuid = UUID.toString();
		log.info("Serving POST /mapping/" + uuid + (user != null ? " for user " + user : ""));
		// 2. Is the given user or the public user authorized for this pathway?
		String pathwayUser;
		try {
			pathwayUser = this.configService.getUserNameAttributedToNetwork(uuid, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage().replace("network", "pathway").replace("Network", "Pathway"))
					.build();
		}
		if (pathwayUser == null) {
			return ResponseEntity.badRequest()
					.header("reason", "Unable to get user attributed to the pathway.")
					.build();
		}
		// 3. Determine the name of the network
		PathwayNode pathwayNode = this.pathwayService.findByEntityUUID(uuid);
		if (pathwayNode == null) {
			return ResponseEntity.badRequest().header("reason", "Could not fetch pathway with uuid:" +uuid).build();
		}
		String mappingName;
		if (prefixName && networkname != null) {
			mappingName = (networkname.endsWith("_") ? networkname : networkname + "_") + pathwayNode.getPathwayNameString();
		} else if (suffixName && networkname != null) { 
			mappingName = pathwayNode.getPathwayNameString() + (networkname.startsWith("_") ? networkname : "_" + networkname);
		} else if (networkname == null && (prefixName || suffixName)) {
			mappingName = (prefixName ? mappingType + "_" : "") + pathwayNode.getPathwayNameString() + (suffixName ? "_" + mappingType : "");
		} else {
			mappingName = networkname;
		}
		
		// Check the MappingName against existing names for that user
		// The name should be unique per user
		// if user already exists AND a mapping with that name already exists, then error
		// if user doesn't exist in the first place, it is fine and we do not need to check for the network name
		if (this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, pathwayUser) != null
				&& this.mappingNodeService.findByNetworkNameAndUser(mappingName, pathwayUser) != null) {
			return ResponseEntity.badRequest()
					.header("reason", "Network with name " + mappingName + " already exists for user " + pathwayUser + ". The network has UUID: " + this.mappingNodeService.findByNetworkNameAndUser(mappingName, pathwayUser).getEntityUUID())
					.build();
		}
		
		// Need Agent
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", pathwayUser);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(agentNodeProperties);
		
		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createMapping);
		
		
		activityNodeProvenanceProperties.put("graphactivityname", "Create_" + mappingName);
		// activityNodeProvenanceProperties.put("createdate", createDate);
		
		ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createMappingActivityNode, userAgentNode,
				ProvenanceGraphEdgeType.wasAssociatedWith);
		
		this.provenanceGraphService.connect(createMappingActivityNode, pathwayNode, ProvenanceGraphEdgeType.used);
		// create the mappingNode
		MappingNode mappingNode;
		try {
			mappingNode = this.networkMappingService.createMappingFromPathway(pathwayNode,
					NetworkMappingType.valueOf(mappingType), createMappingActivityNode,
					userAgentNode, mappingName);
		} catch (NetworkMappingError nme) {
			log.error(nme.getMessage());
			//TODO: Do a rollback of the transaction
			nme.printStackTrace();
			return ResponseEntity.badRequest().header("reason", nme.getMessage()).build();
		} catch (IllegalArgumentException e) {
			if (e.getMessage().contains("NetworkMappingType")) {
				return ResponseEntity.badRequest().header("reason", "Illegal value (" +mappingType+") for mappingType. Allowed values: PPI, SIGNALLING, REGULATORY, METABOLIC, PATHWAYMAPPING").build();
			} else {
				return ResponseEntity.badRequest().header("reason", "Illegal parameter value in request.").build();
			}
		} catch (Exception e) {
				
			// TODO Auto-generated catch block
			log.error(e.getMessage());
			e.printStackTrace();
			return ResponseEntity.badRequest().header("reason", "Failed to create Mapping from pathway: " + e.getMessage()).build();
		}
		
		// build the inventoryItem
		WarehouseInventoryItem mappingInventoryItem = this.warehouseGraphService.getWarehouseInventoryItem(mappingNode);
		
		// return the inventoryItem
		return new ResponseEntity<WarehouseInventoryItem>(mappingInventoryItem, HttpStatus.CREATED);
	}

}
