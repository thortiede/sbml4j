package org.tts.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.api.Input.PathwayCollectionCreationItem;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.api.Output.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.service.NetworkMappingService;
import org.tts.service.OrganismService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.WarehouseGraphService;

@RestController
public class WarehouseController {

	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	NetworkMappingService networkMappingService;
	
	@Autowired
	OrganismService organismService;
	
	Map<String, Map<String, Resource>> networkResources = new HashMap<>();
	
	@RequestMapping(value="/pathwayInventory", method = RequestMethod.GET)
	public ResponseEntity<List<PathwayInventoryItem>> listAllPathways(@RequestHeader("user") String username) {
		
		return new ResponseEntity<List<PathwayInventoryItem>>(this.warehouseGraphService.getListofPathwayInventory(username), HttpStatus.OK);
		
	}

	@RequestMapping(value="/pathwayUUIDs", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getPathwayUUIDs() {
		return new ResponseEntity<List<String>>(this.warehouseGraphService.getListofPathwayUUIDs(), HttpStatus.OK);
	}
	
	@RequestMapping(value="/databaseUUID", method = RequestMethod.GET)
	public ResponseEntity<String> getDatabaseUUID(@RequestParam("source") String source, @RequestParam("version") String version, @RequestParam("organism") String orgCode, @RequestParam("matchingAttribute") String matchingAttribute) {
		Map<String, String> matchingAttributeMap = new HashMap<>();
		matchingAttributeMap.put("Default", matchingAttribute);
		DatabaseNode database = this.warehouseGraphService.getDatabaseNode(source, version, this.organismService.getOrgansimByOrgCode(orgCode), matchingAttributeMap);
		if (database != null) {
			return new ResponseEntity<String>(database.getEntityUUID(), HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/pathwayContents", method = RequestMethod.GET)
	public ResponseEntity<List<ProvenanceEntity>> getPathwayContents(@RequestHeader("user") String username, @RequestParam("uuid") String entityUUID) {
		PathwayNode pathway = this.warehouseGraphService.getPathwayNode(username, entityUUID);
		if(pathway == null) {
			return new ResponseEntity<List<ProvenanceEntity>>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<List<ProvenanceEntity>>(this.warehouseGraphService.getPathwayContents(username, pathway.getEntityUUID()), HttpStatus.OK);
		}
		
	}
	@RequestMapping(value="/mapping", method = RequestMethod.POST)
	public ResponseEntity<WarehouseInventoryItem> createMappingFromWarehouseGraphNode(@RequestHeader("user") String username, 
																					@RequestParam("pathwayEntityUUID") String pathwayEntityUUID, 
																					@RequestParam("mappingType") String mappingType,
																					@RequestParam("externalResource") String erType) {
		
		// Need Agent
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", username);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService.createProvenanceGraphAgentNode(agentNodeProperties);
		
		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createMapping);
		String mappingName = "Pathway_" + pathwayEntityUUID + "_to_" + mappingType + "_withER_" + erType;
		activityNodeProvenanceProperties.put("graphactivityname", mappingName); 
		//activityNodeProvenanceProperties.put("createdate", createDate);
		
		ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createMappingActivityNode, userAgentNode, ProvenanceGraphEdgeType.wasAssociatedWith);
		
		// need the pathwayNode
		PathwayNode pathway = this.warehouseGraphService.getPathwayNode(username, pathwayEntityUUID);
		if(pathway == null) {
			return new ResponseEntity<WarehouseInventoryItem>(HttpStatus.BAD_REQUEST);
		}
		this.provenanceGraphService.connect(createMappingActivityNode, pathway, ProvenanceGraphEdgeType.used);
		// create the mappingNode
		MappingNode mappingNode = this.networkMappingService.createMappingFromPathway(pathway, NetworkMappingType.valueOf(mappingType), ExternalResourceType.valueOf(erType), createMappingActivityNode, userAgentNode);
			
		
		// build the inventoryItem
		WarehouseInventoryItem mappingInventoryItem = this.warehouseGraphService.getWarehouseInventoryItem(mappingNode);
		
		// return the inventoryItem
		return new ResponseEntity<WarehouseInventoryItem>(mappingInventoryItem, HttpStatus.OK);
	}
	
	@RequestMapping(value="/pathwayCollection", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Integer>> createPathwayCollection(@RequestHeader("user") String username,
										@RequestBody PathwayCollectionCreationItem pathwayCollectionCreationItem) {
		// Need Agent
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", username);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService.createProvenanceGraphAgentNode(agentNodeProperties);
		
		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createKnowledgeGraph);
		String activityName = "Create_KnowledgeGraph_for_" + pathwayCollectionCreationItem.getDatabaseEntityUUID();
		activityNodeProvenanceProperties.put("graphactivityname", activityName); 
		
		ProvenanceGraphActivityNode createKnowledgeGraphActivityNode = this.provenanceGraphService.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, userAgentNode, ProvenanceGraphEdgeType.wasAssociatedWith);
		
		// get databaseNode
		DatabaseNode database = this.warehouseGraphService.getDatabaseNode(pathwayCollectionCreationItem.getDatabaseEntityUUID());
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, database, ProvenanceGraphEdgeType.used);
		
		// create a pathwayCollectionNode
		PathwayCollectionNode pathwayCollectionNode = this.warehouseGraphService.createPathwayCollection(pathwayCollectionCreationItem, database, createKnowledgeGraphActivityNode, userAgentNode);
		
		// then create the pathway to the collection by iterating over all pathways in the collection and adding them to the pathway via CONTAINS
		PathwayNode pathwayNode = this.warehouseGraphService.createPathwayNode("CollectionPathway_of_" + pathwayCollectionNode.getEntityUUID(), "CollectionPathway_of_" + pathwayCollectionNode.getPathwayCollectionName(), database.getOrganism());
		this.provenanceGraphService.connect(pathwayNode, pathwayCollectionNode, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(pathwayNode, createKnowledgeGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
		this.provenanceGraphService.connect(pathwayNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		
		Map<String, Integer> collectionPathwayCounterMap = this.warehouseGraphService.buildPathwayFromCollection(pathwayNode, pathwayCollectionNode, createKnowledgeGraphActivityNode, userAgentNode);
		return new ResponseEntity<Map<String, Integer>>(collectionPathwayCounterMap, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/networkInventory", method = RequestMethod.GET)
	public ResponseEntity<List<NetworkInventoryItem>> listAllNetworks(@RequestHeader("user") String username) {
		List<NetworkInventoryItem> networkInventory = this.warehouseGraphService.getListOfNetworkInventoryItems();
		return new ResponseEntity<List<NetworkInventoryItem>>(networkInventory, HttpStatus.OK);
	}
	
	@RequestMapping(value="/network", method = RequestMethod.GET)
	public ResponseEntity<Resource> getNetwork(@RequestParam("UUID") String mappingNodeEntityUUID, @RequestParam("method") String method, @RequestParam("format") String format) {
		/**
		 * An Alternative might be to directly run:
		 * MATCH (m:MappingNode {entityUUID:"3859039b-f92a-41da-b393-4c90a18e8e4e"})-[:Warehouse {warehouseGraphEdgeType: "CONTAINS"}]->(fs:FlatSpecies)-[r]-(fs2:FlatSpecies) RETURN fs.entityUUID, fs.symbol, fs.sboTerm, type(r), fs2.symbol, fs2.entityUUID, fs2.sboTerm;
		 * YES, THIS IS WAYYYY FASTER!!! See warehouseGraphService.getNetwork
		 * But,is it the same?
		 * Nope, not the same. 
		 * 	directed version of direct method has same number of nodes
		 * 	all have different number of edges
		 * 	test1 grep -c "/node" network_3859039b-f92a-41da-b393-4c90a18e8e4e
			281
			test1 grep -c "/node" network_3859039b-f92a-41da-b393-4c90a18e8e4e_longwayx
			260
			test1 grep -c "/node" network_3859039b-f92a-41da-b393-4c90a18e8e4e_longway_directed
			281
			test1 grep -c "/relation" network_3859039b-f92a-41da-b393-4c90a18e8e4e
			0
			test1 grep -c "/edge" network_3859039b-f92a-41da-b393-4c90a18e8e4e
			734
			test1 grep -c "/edge" network_3859039b-f92a-41da-b393-4c90a18e8e4e_longway
			320
			test1 grep -c "/edge" network_3859039b-f92a-41da-b393-4c90a18e8e4e_longway_directed
			367

		 */
		Resource resource;
		/*Map<String, Resource> uuidMap;
		if (this.networkResources.containsKey(mappingNodeEntityUUID)) {
			uuidMap = networkResources.get(mappingNodeEntityUUID);
			if (uuidMap.containsKey(format)) {
				resource = networkResources.get(mappingNodeEntityUUID).get(format);
			} else {
				resource = this.networkMappingService.getResourceFromNodeEdgeList(this.warehouseGraphService.getNetwork(mappingNodeEntityUUID), format);
				uuidMap.put(format, resource);
				this.networkResources.put(mappingNodeEntityUUID, uuidMap);
			}
		} else {
			uuidMap = new HashMap<>();
			resource = this.networkMappingService.getResourceFromNodeEdgeList(this.warehouseGraphService.getNetwork(mappingNodeEntityUUID), format);
			uuidMap.put(format, resource);
			this.networkResources.put(mappingNodeEntityUUID, uuidMap);
		}
		*/
		NodeEdgeList nel = this.warehouseGraphService.getNetwork(mappingNodeEntityUUID, method);
		resource = this.networkMappingService.getResourceFromNodeEdgeList(nel, format);
		if (resource != null) {
			//logger.info("Converted flatNetwork to Resource");
			// Try to determine file's content type
		    String contentType =  "application/octet-stream";
		  
		    // only generic contentType as we are not dealing with an actual file serverside,
			// but the file shall only be created at the client side.
			String filename = "network." + method + "." + nel.getName() + "." + format;
			return ResponseEntity.ok()
			        .contentType(MediaType.parseMediaType(contentType))
			        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
			        .body(resource);	
		} else {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT);
		}
	}
}
