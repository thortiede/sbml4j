package org.tts.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Input.PathwayCollectionCreationItem;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.NetworkInventoryItemDetail;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.api.Output.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.MappingStep;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.flat.FlatSpecies;
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
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
																					@RequestParam("idSystem") String idSystem) {
		
		// Need Agent
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", username);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService.createProvenanceGraphAgentNode(agentNodeProperties);
		
		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createMapping);
		String mappingName = "Pathway_" + pathwayEntityUUID + "_to_" + mappingType + "_withidSystem_" + idSystem;
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
		MappingNode mappingNode = this.networkMappingService.createMappingFromPathway(pathway, NetworkMappingType.valueOf(mappingType), IDSystem.valueOf(idSystem), createMappingActivityNode, userAgentNode);
			
		
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
		List<NetworkInventoryItem> networkInventory = this.warehouseGraphService.getListOfNetworkInventoryItems(username);
		return new ResponseEntity<List<NetworkInventoryItem>>(networkInventory, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/networkInventory/{entityUUID}", method = RequestMethod.GET)
	public ResponseEntity<NetworkInventoryItemDetail> getNetworkInventoryDetail(@PathVariable String entityUUID) {
		NetworkInventoryItemDetail networkInventoryItemDetail = new NetworkInventoryItemDetail(this.warehouseGraphService.getNetworkInventoryItem(entityUUID));
		networkInventoryItemDetail.setNodes(this.warehouseGraphService.getNetworkNodeSymbols(entityUUID));
		return new ResponseEntity<NetworkInventoryItemDetail>(networkInventoryItemDetail, HttpStatus.OK);
	}
	
	@RequestMapping(value="/context", method = RequestMethod.GET)
	public ResponseEntity<Resource> getContext(	@RequestHeader("user") String username,
												@RequestParam(value="baseNetworkUUID", defaultValue="edb856fb-7c91-44de-8b23-68364b9c00e8") String baseNetworkUUID,
												@RequestParam(value="gene") String geneSymbol,
												@RequestParam(value = "minSize", defaultValue = "1") int minSize,
												@RequestParam(value = "maxSize", defaultValue = "3") int maxSize) {
		String mappingEntityUUID = this.warehouseGraphService.getMappingEntityUUID(baseNetworkUUID, geneSymbol, minSize, maxSize);
		if(mappingEntityUUID != null) {
			return this.getNetwork(mappingEntityUUID, "undirected", "graphml");
		} else {
			FilterOptions masterOptions = this.warehouseGraphService.getFilterOptions(WarehouseGraphNodeType.MAPPING, baseNetworkUUID);
			List<String> contextNodeSymbol = new ArrayList<>();
			contextNodeSymbol.add(geneSymbol);
			masterOptions.setNodeSymbols(contextNodeSymbol);
			masterOptions.setMinSize(minSize);
			masterOptions.setMaxSize(maxSize);
			//String step = "CONTEXT";
			ResponseEntity<NetworkInventoryItemDetail> resp = this.createMappingFromMappingWithOptions(username, baseNetworkUUID, MappingStep.CONTEXT, masterOptions);
			if(resp.getStatusCode().equals(HttpStatus.OK)) {
				return this.getContext(username, baseNetworkUUID, geneSymbol, minSize, maxSize);
			} else {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			
		}
	}
	
	@RequestMapping(value="/network", method = RequestMethod.POST)
	public ResponseEntity<NetworkInventoryItemDetail> createMappingFromMappingWithOptions(@RequestHeader("user") String username,
																			@RequestParam(value="networkEntityUUID") String parentUUID,
																			@RequestParam(value="step") MappingStep step,
																			@RequestBody FilterOptions options) {
		// for now there is no need for the user to give uuid in filterOptions, as it is part of the path
		/*if(!parentUUID.equals(options.getMappingUuid())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}*/
		// rather I can set it here, to have it
		options.setMappingUuid(parentUUID);
		MappingNode parent = this.warehouseGraphService.getMappingNode(parentUUID);
		
		// Need Agent
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", username);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService.createProvenanceGraphAgentNode(agentNodeProperties);
		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createMapping);
		String activityName = "Create_Mapping_from_" + parentUUID + "_byApplyingStep_" + step.name() + "_with_" + options.toString();
		activityNodeProvenanceProperties.put("graphactivityname", activityName); 
		ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createMappingActivityNode, userAgentNode, ProvenanceGraphEdgeType.wasAssociatedWith);
		Instant startTime = Instant.now();
		// create new networkMapping that is the same as the parent one
		// need mapingNode (name: OldName + step)
		MappingNode newMapping = this.warehouseGraphService.createMappingNode(parent, options.getNetworkType(), parent.getMappingName() + "_" + step.name() + "_with_" + options.toString()); 
		
		this.provenanceGraphService.connect(newMapping, parent, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(newMapping, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		this.provenanceGraphService.connect(newMapping, createMappingActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
		// copy all Nodes with their relationships to new mapping
		List<FlatSpecies> oldSpeciesList = this.warehouseGraphService.getNetworkNodes(parentUUID);
		List<FlatSpecies> newSpeciesList = new ArrayList<>(oldSpeciesList.size());
		
		switch (step) {
		case COPY:
			newSpeciesList = this.warehouseGraphService.copyFlatSpeciesList(oldSpeciesList);
			break;
		case FILTER:
			newSpeciesList = this.warehouseGraphService.copyAndFilterFlatSpeciesList(oldSpeciesList, options);
			break;
		case ANNOTATE:
			// same as copy, but add annotation from options (this means FilterOptions needs to be extended)
			newSpeciesList = this.warehouseGraphService.copyFlatSpeciesList(oldSpeciesList);
			break;
		case CONTEXT:
			newMapping.setGeneSymbol(options.getNodeSymbols().get(0));
			newMapping.setMinSize(options.getMinSize());
			newMapping.setMaxSize(options.getMaxSize());
			newMapping.setBaseNetworkEntityUUID(parentUUID);
			newMapping = (MappingNode) this.warehouseGraphService.saveWarehouseGraphNodeEntity(newMapping, 0);
			// this needs one (or) more nodes in the options
			// plus some parameters for the context search (number of relationships to travel, endpointType of travel)
			// then calculate the context on the parentNet
			// persist the resulting network with all relationships between contained nodes
			List<FlatSpecies> contextSpeciesList = this.warehouseGraphService.createNetworkContext(oldSpeciesList, parentUUID, options);
			// adjust the options here to include all the nodes that are present in the created oldSpeciesList
			List<String> newNodeSymbols = new ArrayList<>();
			for (FlatSpecies species : contextSpeciesList) {
				newNodeSymbols.add(species.getSymbol());
			}
			options.setNodeSymbols(newNodeSymbols);
			// then copy and filter the FlatSpecies accordingly
			// The nodeSymbols step now guarantees, that only those nodes remain (and get referenced in relations) that are part of the network context
			newSpeciesList = this.warehouseGraphService.copyAndFilterFlatSpeciesList(oldSpeciesList, options);
			break;
		default:
			break;
		}
		
		int numberOfRelations = 0;
		int numberOfNodes = 0;
		Set<String> nodeTypes = new HashSet<>();
		Set<String> relationTypes = new HashSet<>();
		for (int i = 0; i != newSpeciesList.size(); i++) {
			FlatSpecies newSpecies = newSpeciesList.get(i);
			if(newSpecies != null) {
				numberOfNodes++;
				Map<String, List<FlatSpecies>> relatedSpeciesList = newSpecies.getAllRelatedSpecies();
				
				nodeTypes.add(newSpecies.getSboTerm());
				for (String key : relatedSpeciesList.keySet()) {
					numberOfRelations += relatedSpeciesList.get(key).size();
					relationTypes.add(key);
				}
				newSpecies = this.networkMappingService.persistFlatSpecies(newSpecies);
				this.warehouseGraphService.connect(newMapping, newSpecies, WarehouseGraphEdgeType.CONTAINS);
				this.provenanceGraphService.connect(newSpecies, oldSpeciesList.get(i), ProvenanceGraphEdgeType.wasDerivedFrom);
				this.provenanceGraphService.connect(newSpecies, createMappingActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			}
			
		}
		Instant endTime = Instant.now();
		newMapping.addWarehouseAnnotation("creationstarttime", startTime.toString());
		newMapping.addWarehouseAnnotation("creationendtime", endTime.toString());
		newMapping.addWarehouseAnnotation("numberofnodes",  String.valueOf(numberOfNodes));
		newMapping.addWarehouseAnnotation("numberofrelations", String.valueOf(numberOfRelations));
		newMapping.setMappingNodeTypes(nodeTypes);
		newMapping.setMappingRelationTypes(relationTypes);
		this.warehouseGraphService.saveWarehouseGraphNodeEntity(newMapping, 0);
		return this.getNetworkInventoryDetail(newMapping.getEntityUUID());		
	}
	
	@RequestMapping(value="/network", method = RequestMethod.GET)
	public ResponseEntity<Resource> getNetwork(@RequestParam("UUID") String mappingNodeEntityUUID, @RequestParam(value = "method", defaultValue = "undirected") String method, @RequestParam("format") String format) {
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
		Instant startTime = Instant.now();
		NodeEdgeList nel = this.warehouseGraphService.getNetwork(mappingNodeEntityUUID, method);
		Instant middleTime = Instant.now();
		resource = this.networkMappingService.getResourceFromNodeEdgeList(nel, format);
		Instant afterTime = Instant.now();
		if (resource != null) {
			//logger.info("Converted flatNetwork to Resource");
			// Try to determine file's content type
		    String contentType =  "application/octet-stream";
		  
		    // only generic contentType as we are not dealing with an actual file serverside,
			// but the file shall only be created at the client side.
			String filename = "network." + method + "." + nel.getName() + "." + format;
			logger.info("Start: " + startTime.toString() + " middle " + middleTime.toString() + " after " + afterTime.toString() + "=> " + Duration.between(startTime, middleTime).toString() + " / " + Duration.between(middleTime, afterTime).toString());
			return ResponseEntity.ok()
			        .contentType(MediaType.parseMediaType(contentType))
			        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
			        .body(resource);	
		} else {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT);
		}
	}

	@RequestMapping(value = "/networkInventory/{entityUUID}/filterOptions", method = RequestMethod.GET)
	public ResponseEntity<FilterOptions> getNetworkFilterOptions(@PathVariable String entityUUID) {
		return new ResponseEntity<FilterOptions>(this.warehouseGraphService.getFilterOptions(WarehouseGraphNodeType.MAPPING, entityUUID), HttpStatus.OK);
	}

	public boolean mappingForPathwayExists(String entityUUID) {
		return this.warehouseGraphService.mappingForPathwayExists(entityUUID);
	}
}
