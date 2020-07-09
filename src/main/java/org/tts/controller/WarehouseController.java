package org.tts.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.tts.config.VcfConfig;
import org.tts.model.api.Input.Drivergenes;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Input.PathwayCollectionCreationItem;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.NetworkInventoryItemDetail;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.api.Output.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.MappingStep;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.service.HttpService;
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

	@Autowired
	HttpService httpService;

	@Autowired
	VcfConfig vcfConfig;
	
	Map<String, Map<String, Resource>> networkResources = new HashMap<>();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(value = "/pathwayInventory", method = RequestMethod.GET)
	public ResponseEntity<List<PathwayInventoryItem>> listAllPathways(@RequestHeader("user") String username) {
		logger.debug("Serving PathwayInventory for user " + username);
		return new ResponseEntity<List<PathwayInventoryItem>>(
				this.warehouseGraphService.getListofPathwayInventory(username), HttpStatus.OK);

	}

	@RequestMapping(value = "/pathwayUUIDs", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getPathwayUUIDs() {
		return new ResponseEntity<List<String>>(this.warehouseGraphService.getListofPathwayUUIDs(), HttpStatus.OK);
	}

	@RequestMapping(value = "/databaseUUID", method = RequestMethod.GET)
	public ResponseEntity<String> getDatabaseUUID(@RequestParam("source") String source,
			@RequestParam("version") String version, @RequestParam("organism") String orgCode,
			@RequestParam("matchingAttribute") String matchingAttribute) {
		Map<String, String> matchingAttributeMap = new HashMap<>();
		matchingAttributeMap.put("Default", matchingAttribute);
		DatabaseNode database = this.warehouseGraphService.getDatabaseNode(source, version,
				this.organismService.getOrgansimByOrgCode(orgCode), matchingAttributeMap);
		if (database != null) {
			return new ResponseEntity<String>(database.getEntityUUID(), HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/pathwayContents", method = RequestMethod.GET)
	public ResponseEntity<List<ProvenanceEntity>> getPathwayContents(@RequestHeader("user") String username,
			@RequestParam("uuid") String entityUUID) {
		PathwayNode pathway = this.warehouseGraphService.getPathwayNode(username, entityUUID);
		if (pathway == null) {
			return new ResponseEntity<List<ProvenanceEntity>>(HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<List<ProvenanceEntity>>(
					this.warehouseGraphService.getPathwayContents(username, pathway.getEntityUUID()), HttpStatus.OK);
		}

	}

	@RequestMapping(value = "/pathwayCollection", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Integer>> createPathwayCollection(@RequestHeader("user") String username,
			@RequestBody PathwayCollectionCreationItem pathwayCollectionCreationItem) {
		// Need Agent
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", username);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(agentNodeProperties);

		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createKnowledgeGraph);
		String activityName = "Create_KnowledgeGraph_for_" + pathwayCollectionCreationItem.getDatabaseEntityUUID();
		activityNodeProvenanceProperties.put("graphactivityname", activityName);

		ProvenanceGraphActivityNode createKnowledgeGraphActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, userAgentNode,
				ProvenanceGraphEdgeType.wasAssociatedWith);

		// get databaseNode
		DatabaseNode database = this.warehouseGraphService
				.getDatabaseNode(pathwayCollectionCreationItem.getDatabaseEntityUUID());
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, database, ProvenanceGraphEdgeType.used);

		// create a pathwayCollectionNode
		PathwayCollectionNode pathwayCollectionNode = this.warehouseGraphService.createPathwayCollection(
				pathwayCollectionCreationItem, database, createKnowledgeGraphActivityNode, userAgentNode);

		// then create the pathway to the collection by iterating over all pathways in
		// the collection and adding them to the pathway via CONTAINS
		PathwayNode pathwayNode = this.warehouseGraphService.createPathwayNode(
				"CollectionPathway_of_" + pathwayCollectionNode.getEntityUUID(),
				"CollectionPathway_of_" + pathwayCollectionNode.getPathwayCollectionName(), database.getOrganism());
		this.provenanceGraphService.connect(pathwayNode, pathwayCollectionNode, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(pathwayNode, createKnowledgeGraphActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy);
		this.provenanceGraphService.connect(pathwayNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);

		Map<String, Integer> collectionPathwayCounterMap = this.warehouseGraphService.buildPathwayFromCollection(
				pathwayNode, pathwayCollectionNode, createKnowledgeGraphActivityNode, userAgentNode);
		return new ResponseEntity<Map<String, Integer>>(collectionPathwayCounterMap, HttpStatus.OK);
	}

	@RequestMapping(value = "/mapping", method = RequestMethod.POST)
	public ResponseEntity<WarehouseInventoryItem> createMappingFromWarehouseGraphNode(
			@RequestHeader("user") String username, @RequestParam("pathwayEntityUUID") String pathwayEntityUUID,
			@RequestParam("mappingType") String mappingType, @RequestParam("idSystem") String idSystem) {

		// Need Agent
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", username);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(agentNodeProperties);

		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createMapping);
		String mappingName = "Pathway_" + pathwayEntityUUID + "_to_" + mappingType + "_withidSystem_" + idSystem;
		activityNodeProvenanceProperties.put("graphactivityname", mappingName);
		// activityNodeProvenanceProperties.put("createdate", createDate);

		ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createMappingActivityNode, userAgentNode,
				ProvenanceGraphEdgeType.wasAssociatedWith);

		// need the pathwayNode
		PathwayNode pathway = this.warehouseGraphService.getPathwayNode(username, pathwayEntityUUID);
		if (pathway == null) {
			return new ResponseEntity<WarehouseInventoryItem>(HttpStatus.BAD_REQUEST);
		}
		this.provenanceGraphService.connect(createMappingActivityNode, pathway, ProvenanceGraphEdgeType.used);
		// create the mappingNode
		MappingNode mappingNode;
		try {
			mappingNode = this.networkMappingService.createMappingFromPathway(pathway,
					NetworkMappingType.valueOf(mappingType), IDSystem.valueOf(idSystem), createMappingActivityNode,
					userAgentNode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<WarehouseInventoryItem>(HttpStatus.BAD_REQUEST);
		}

		// build the inventoryItem
		WarehouseInventoryItem mappingInventoryItem = this.warehouseGraphService.getWarehouseInventoryItem(mappingNode);

		// return the inventoryItem
		return new ResponseEntity<WarehouseInventoryItem>(mappingInventoryItem, HttpStatus.OK);
	}

	@RequestMapping(value = "/networkInventory", method = RequestMethod.GET)
	public ResponseEntity<List<NetworkInventoryItem>> listAllNetworks(@RequestHeader("user") String username,
			@RequestParam(value = "active", defaultValue = "true") boolean isActiveOnly) {
		List<NetworkInventoryItem> networkInventory = this.warehouseGraphService
				.getListOfNetworkInventoryItems(username, isActiveOnly);
		return new ResponseEntity<List<NetworkInventoryItem>>(networkInventory, HttpStatus.OK);
	}

	@RequestMapping(value = "/networkInventory/{entityUUID}", method = RequestMethod.GET)
	public ResponseEntity<NetworkInventoryItemDetail> getNetworkInventoryDetail(@PathVariable String entityUUID) {
		NetworkInventoryItemDetail networkInventoryItemDetail = new NetworkInventoryItemDetail(
				this.warehouseGraphService.getNetworkInventoryItem(entityUUID));
		networkInventoryItemDetail.setNodes(new ArrayList<>(this.warehouseGraphService.getNetworkNodeSymbols(entityUUID)));
		return new ResponseEntity<NetworkInventoryItemDetail>(networkInventoryItemDetail, HttpStatus.OK);
	}

	/*
	 * @RequestMapping(value = "/context", method = RequestMethod.GET) public
	 * ResponseEntity<Resource> getContext(@RequestHeader("user") String username,
	 * 
	 * @RequestParam(value = "baseNetworkUUID", defaultValue =
	 * "edb856fb-7c91-44de-8b23-68364b9c00e8") String baseNetworkUUID,
	 * 
	 * @RequestParam(value = "gene") String geneSymbol,
	 * 
	 * @RequestParam(value = "minSize", defaultValue = "1") int minSize,
	 * 
	 * @RequestParam(value = "maxSize", defaultValue = "3") int maxSize,
	 * 
	 * @RequestParam(value = "persist", defaultValue = "false") boolean persist,
	 * 
	 * @RequestParam(value = "format", defaultValue = "graphml") String format,
	 * 
	 * @RequestParam(value = "terminateAtDrug", defaultValue = "false") boolean
	 * terminateAtDrug) {
	 * 
	 * logger.info("Creating context in " + baseNetworkUUID + " (geneSymbol=" +
	 * geneSymbol + ", maxSize=" + maxSize + ", persist=" + persist + ",format=" +
	 * format + ") for user " + username);
	 * 
	 * if (persist) { String mappingEntityUUID =
	 * this.warehouseGraphService.getMappingEntityUUID(baseNetworkUUID, geneSymbol,
	 * minSize, maxSize); if (mappingEntityUUID != null) { return
	 * this.getNetwork(mappingEntityUUID, "undirected", "graphml"); } else {
	 * FilterOptions masterOptions = this.warehouseGraphService
	 * .getFilterOptions(WarehouseGraphNodeType.MAPPING, baseNetworkUUID);
	 * List<String> contextNodeSymbol = new ArrayList<>();
	 * contextNodeSymbol.add(geneSymbol);
	 * masterOptions.setNodeSymbols(contextNodeSymbol);
	 * masterOptions.setMinSize(minSize); masterOptions.setMaxSize(maxSize); //
	 * String step = "CONTEXT"; ResponseEntity<NetworkInventoryItemDetail> resp =
	 * this.createMappingFromMappingWithOptions(username, baseNetworkUUID,
	 * MappingStep.CONTEXT, masterOptions); if
	 * (resp.getStatusCode().equals(HttpStatus.OK)) { return
	 * this.getContext(username, baseNetworkUUID, geneSymbol, minSize, maxSize,
	 * persist, format, terminateAtDrug); } else { return new
	 * ResponseEntity<>(HttpStatus.BAD_REQUEST); } } } else { Resource resource;
	 * Instant startTime = Instant.now(); // get startNode EntityUUID String
	 * startNodeEntityUUID =
	 * this.warehouseGraphService.findStartNode(baseNetworkUUID, geneSymbol);
	 * Instant findNodeTime = Instant.now(); FilterOptions masterOptions =
	 * this.warehouseGraphService.getFilterOptions(WarehouseGraphNodeType.MAPPING,
	 * baseNetworkUUID); Instant getFilterOptionsTime = Instant.now(); List<String>
	 * contextNodeSymbol = new ArrayList<>(); contextNodeSymbol.add(geneSymbol);
	 * masterOptions.setNodeSymbols(contextNodeSymbol);
	 * masterOptions.setMinSize(minSize); masterOptions.setMaxSize(maxSize);
	 * masterOptions.setTerminateAtDrug(terminateAtDrug);
	 * logger.info(masterOptions.toString()); List<FlatSpecies>
	 * networkContextFlatSpeciesList = this.warehouseGraphService
	 * .findNetworkContext(startNodeEntityUUID, masterOptions); Instant
	 * findNetworkContextTime = Instant.now(); NodeEdgeList nel =
	 * this.warehouseGraphService.flatSpeciesListToNEL(
	 * networkContextFlatSpeciesList, baseNetworkUUID); Instant nelTime =
	 * Instant.now(); resource =
	 * this.networkMappingService.getResourceFromNodeEdgeList(nel, format); Instant
	 * afterTime = Instant.now(); if (resource != null) { //
	 * logger.info("Converted flatNetwork to Resource"); // Try to determine file's
	 * content type String contentType = "application/octet-stream";
	 * 
	 * // only generic contentType as we are not dealing with an actual file //
	 * serverside, // but the file shall only be created at the client side. String
	 * filename = "network." + nel.getName() + "." + format; //
	 * logger.info("Start: " + startTime.toString() + " middle " + //
	 * middleTime.toString() + " after " + afterTime.toString() + "=> " + //
	 * Duration.between(startTime, middleTime).toString() + " / " + //
	 * Duration.between(middleTime, afterTime).toString());
	 * logger.info("findNodeTime: " + Duration.between(startTime,
	 * findNodeTime).toString() + " / " + "getFilterOptionsTime: " +
	 * Duration.between(findNodeTime, getFilterOptionsTime).toString() + " / " +
	 * "findNetworkContextTime: " + Duration.between(getFilterOptionsTime,
	 * findNetworkContextTime).toString() + " / " + "nelTime: " +
	 * Duration.between(findNetworkContextTime, nelTime).toString() + " / " +
	 * "resourceTime: " + Duration.between(nelTime, afterTime).toString()); return
	 * ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
	 * .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename
	 * + "\"") .body(resource); } else { return new
	 * ResponseEntity<Resource>(HttpStatus.NO_CONTENT); } } }
	 */
	
	
	@RequestMapping(value = "/network", method = RequestMethod.POST)
	public ResponseEntity<NetworkInventoryItemDetail> createMappingFromMappingWithOptions(
											  @RequestHeader("user") String username
											, @RequestParam(value = "networkEntityUUID") String parentUUID
											, @RequestParam(value = "step") MappingStep step
											, @RequestBody FilterOptions options) {
		// Check that we are working on the right network
		if (!parentUUID.equals(options.getMappingUuid())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		
		// rather I can set it here, to have it
		// options.setMappingUuid(parentUUID);
		MappingNode parent = this.warehouseGraphService.getMappingNode(parentUUID);
		String newMappingName = parent.getMappingName() + "_" + step.name() + "_with_" + options.toString();
		String activityName = "Create_Mapping_from_" + parentUUID + "_byApplyingStep_" + step.name() + "_with_"
				+ options.toString();
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createMapping;
		NetworkMappingType mappingType = options.getNetworkType();
		// Need Agent
		MappingNode newMapping = this.warehouseGraphService.createMappingPre(username, parent, newMappingName, activityName, activityType,
				mappingType);

		/**
		 * Refactor 2020 At this point divert into the service to not have this logic in
		 * the controller The basic operation is to copy all network nodes and edges,
		 * arrange them under the new mapping node and connect them to the old nodes
		 * 
		 */
		if (step.equals(MappingStep.FILTER) || step.equals(MappingStep.ANNOTATE) || step.equals(MappingStep.COPY) || step.equals(MappingStep.PATHWAYINFO)) {
			newMapping = this.warehouseGraphService.createMappingFromMappingWithOptions(parent, newMapping, options,
					step);
		}

		Instant endTime = Instant.now();
		
		newMapping.addWarehouseAnnotation("creationendtime", endTime.toString());

		this.warehouseGraphService.saveWarehouseGraphNodeEntity(newMapping, 0);
		return this.getNetworkInventoryDetail(newMapping.getEntityUUID());
	}

	

	@RequestMapping(value = "/network", method = RequestMethod.DELETE)
	public ResponseEntity<NetworkInventoryItem> deactivateNetwork(@RequestParam("UUID") String mappingNodeEntityUUID) {

		NetworkInventoryItem networkInventoryItem = this.warehouseGraphService.deactivateNetwork(mappingNodeEntityUUID);
		if (networkInventoryItem != null) {
			return new ResponseEntity<NetworkInventoryItem>(networkInventoryItem, HttpStatus.OK);
		} else {
			return new ResponseEntity<NetworkInventoryItem>(HttpStatus.BAD_REQUEST);
		}
	}

	
	@RequestMapping(value="/network", method = RequestMethod.GET)
	public ResponseEntity<Resource> getNetwork(@RequestParam("networkEntityUUID")String networkEntityUUID,
												@RequestParam(value = "directed", defaultValue = "false")boolean directed) {
		Resource resource = this.warehouseGraphService.getNetwork(networkEntityUUID, directed);
		String contentType = "application/octet-stream";
		if(resource != null) {
			String filename = resource.getFilename();
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"") .body(resource); 
		} else { 
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} 
	}
	
	
	@RequestMapping(value="geneset", method = RequestMethod.GET)
	public ResponseEntity<Resource> getGeneset(@RequestParam("networkEntityUUID")String networkEntityUUID,
												@RequestParam("genes")List<String> genes,
												@RequestParam(value = "directed", defaultValue = "false")boolean directed) {
													
		Resource resource = this.warehouseGraphService.getNetwork(networkEntityUUID, genes, directed);
		if(resource == null) {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} else {
			String contentType = "application/octet-stream";
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"geneset.graphml\"").body(resource) ;
		} 
	}
	
	
	@RequestMapping(value="/context", method = RequestMethod.GET)
	public ResponseEntity<Resource> getContext(@RequestParam("networkEntityUUID")String networkEntityUUID, 
												@RequestParam("genes")List<String> genes,
												@RequestParam(value = "minSize", defaultValue = "1") int minSize,
												@RequestParam(value = "maxSize", defaultValue = "3") int maxSize,
												@RequestParam(value = "directed", defaultValue = "false") boolean directed,
												//@RequestParam(value = "persist", defaultValue = "false") boolean persist,
												//@RequestParam(value = "format", defaultValue = "graphml") String format,
												@RequestParam(value = "terminateAtDrug", defaultValue = "false") boolean terminateAtDrug,
												@RequestParam(value = "direction", defaultValue = "both") String direction /*upstream, downstream, both*/) {
		String contentType = "application/octet-stream";
		Resource resource;
		if(genes == null || genes.size() < 1) {
			resource = null;
		} else {
			resource = this.warehouseGraphService.getNetworkContext(networkEntityUUID, genes, minSize, maxSize, terminateAtDrug, direction, directed);
		}
		
		if(resource != null) {
			String filename = resource.getDescription().substring(21, resource.getDescription().length() - 1);
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"").body(resource); 
		} else { 
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} 	
	}
	
	@RequestMapping(value="/context", method = RequestMethod.POST)
	public ResponseEntity<String> postContext(	  @RequestHeader(value = "user") String username
												, @RequestParam("networkEntityUUID")String networkEntityUUID
												, @RequestParam("genes")List<String> genes
												, @RequestParam(value = "minSize", defaultValue = "1") int minSize
												, @RequestParam(value = "maxSize", defaultValue = "3") int maxSize
												//, @RequestParam(value = "directed", defaultValue = "false") boolean directed
												//, @RequestParam(value = "persist", defaultValue = "false") boolean persist
												//, @RequestParam(value = "format", defaultValue = "graphml") String format
												, @RequestParam(value = "terminateAtDrug", defaultValue = "false") boolean terminateAtDrug
												, @RequestParam(value = "direction", defaultValue = "both") String direction /*upstream, downstream, both*/
												, @RequestParam(value = "contextName", defaultValue = "") String contextName) {
		
		String contextNetworkEntityUUID;
		MappingNode parent = this.warehouseGraphService.getMappingNode(networkEntityUUID);
		if(genes == null || genes.size() < 1) {
			contextNetworkEntityUUID = null;
		} else {
			/**
			 * get all needed parameters for the pre function
			 */
			String newMappingName = "";
			String geneString = "";
			if (contextName.equals("")) {
				newMappingName = "Context_derived_from_" + networkEntityUUID + "_for_gene";
				
				if (genes.size() > 1 ) {
					geneString += "s";
				}
				for (String gene : genes) {
					geneString += "_";
					geneString += gene;
				}
				newMappingName += geneString;
			} else {
				newMappingName = contextName;
			}
			String activityName = "Create_Context_from_" + networkEntityUUID + "_for_gene" + geneString;
			
			ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createContext;
			NetworkMappingType mappingType = parent.getMappingType();
			
			MappingNode contextMappingNode = this.warehouseGraphService.createMappingPre(username, 
																			parent, 
																			newMappingName,
																			activityName, 
																			activityType, 
																			mappingType);
			// then create the context and attach the nodes to this new mapping node (as in createMappingFromMappingWithOptions)
			contextNetworkEntityUUID = this.warehouseGraphService.postNetworkContext(contextMappingNode, genes, minSize, maxSize, terminateAtDrug, direction);
		}
		
		if(contextNetworkEntityUUID != null) {
			
			return new ResponseEntity<String>(contextNetworkEntityUUID, HttpStatus.OK); 
		} else { 
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT); 
		} 	
	}
	
	/*
	 * @RequestMapping(value="/network", method = RequestMethod.GET) public
	 * ResponseEntity<Resource> getNetwork( @RequestParam("UUID") String
	 * mappingNodeEntityUUID,
	 * 
	 * @RequestParam(value = "method", defaultValue = "undirected") String method,
	 * 
	 * @RequestParam("format") String format) {
	 *//**
		 * An Alternative might be to directly run: MATCH (m:MappingNode
		 * {entityUUID:"3859039b-f92a-41da-b393-4c90a18e8e4e"})-[:Warehouse
		 * {warehouseGraphEdgeType: "CONTAINS"}]->(fs:FlatSpecies)-[r]-(fs2:FlatSpecies)
		 * RETURN fs.entityUUID, fs.symbol, fs.sboTerm, type(r), fs2.symbol,
		 * fs2.entityUUID, fs2.sboTerm; YES, THIS IS WAYYYY FASTER!!! See
		 * warehouseGraphService.getNetwork But,is it the same? Nope, not the same.
		 * directed version of direct method has same number of nodes all have different
		 * number of edges test1 grep -c "/node"
		 * network_3859039b-f92a-41da-b393-4c90a18e8e4e 281 test1 grep -c "/node"
		 * network_3859039b-f92a-41da-b393-4c90a18e8e4e_longwayx 260 test1 grep -c
		 * "/node" network_3859039b-f92a-41da-b393-4c90a18e8e4e_longway_directed 281
		 * test1 grep -c "/relation" network_3859039b-f92a-41da-b393-4c90a18e8e4e 0
		 * test1 grep -c "/edge" network_3859039b-f92a-41da-b393-4c90a18e8e4e 734 test1
		 * grep -c "/edge" network_3859039b-f92a-41da-b393-4c90a18e8e4e_longway 320
		 * test1 grep -c "/edge"
		 * network_3859039b-f92a-41da-b393-4c90a18e8e4e_longway_directed 367
		 * 
		 *//*
			 * Resource resource; Map<String, Resource> uuidMap; if
			 * (this.networkResources.containsKey(mappingNodeEntityUUID)) { uuidMap =
			 * networkResources.get(mappingNodeEntityUUID); if (uuidMap.containsKey(format))
			 * { resource = networkResources.get(mappingNodeEntityUUID).get(format); } else
			 * { resource = this.networkMappingService.getResourceFromNodeEdgeList(this.
			 * warehouseGraphService.getNetwork(mappingNodeEntityUUID), format);
			 * uuidMap.put(format, resource);
			 * this.networkResources.put(mappingNodeEntityUUID, uuidMap); } } else { uuidMap
			 * = new HashMap<>(); resource =
			 * this.networkMappingService.getResourceFromNodeEdgeList(this.
			 * warehouseGraphService.getNetwork(mappingNodeEntityUUID), format);
			 * uuidMap.put(format, resource);
			 * this.networkResources.put(mappingNodeEntityUUID, uuidMap); }
			 * 
			 * Instant startTime = Instant.now(); NodeEdgeList nel =
			 * this.warehouseGraphService.getNetwork(mappingNodeEntityUUID, method); Instant
			 * middleTime = Instant.now(); resource =
			 * this.networkMappingService.getResourceFromNodeEdgeList(nel, format); Instant
			 * afterTime = Instant.now(); if (resource != null) {
			 * //logger.info("Converted flatNetwork to Resource"); // Try to determine
			 * file's content type String contentType = "application/octet-stream";
			 * 
			 * // only generic contentType as we are not dealing with an actual file
			 * serverside, // but the file shall only be created at the client side. String
			 * filename = "network." + method + "." + nel.getName() + "." + format;
			 * logger.info("Start: " + startTime.toString() + " middle " +
			 * middleTime.toString() + " after " + afterTime.toString() + "=> " +
			 * Duration.between(startTime, middleTime).toString() + " / " +
			 * Duration.between(middleTime, afterTime).toString()); return
			 * ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
			 * .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename
			 * + "\"") .body(resource); } else { return new
			 * ResponseEntity<Resource>(HttpStatus.NO_CONTENT); } }
			 */

	@RequestMapping(value = "/networkInventory/{entityUUID}/filterOptions", method = RequestMethod.GET)
	public ResponseEntity<FilterOptions> getNetworkFilterOptions(@PathVariable String entityUUID) {
		return new ResponseEntity<FilterOptions>(
				this.warehouseGraphService.getFilterOptions(WarehouseGraphNodeType.MAPPING, entityUUID), HttpStatus.OK);
	}

	public boolean mappingForPathwayExists(String entityUUID) {
		return this.warehouseGraphService.mappingForPathwayExists(entityUUID);
	}

	@RequestMapping(value = "/drivergenes", method = RequestMethod.POST)
	public ResponseEntity<String> createDriverGeneNetwork(
													@RequestHeader(value = "user") String username,
													@RequestBody Drivergenes drivergenes) {
		if (drivergenes.getGenes() == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		
		ResponseEntity<String> contextMappingResponse = this.postContext(username
				, (drivergenes.getBaseNetworkUUID() != null) ? drivergenes.getBaseNetworkUUID() : this.vcfConfig.getVcfDefaultProperties().getBaseNetworkUUID()
				, drivergenes.getGenes()
				, this.vcfConfig.getVcfDefaultProperties().getMaxSize()
				, this.vcfConfig.getVcfDefaultProperties().getMaxSize()
				, this.vcfConfig.getVcfDefaultProperties().isTerminateAtDrug()
				, this.vcfConfig.getVcfDefaultProperties().getDirection()
				, username + "_" + drivergenes.toSingleLineString());
		
		
		if(contextMappingResponse.getStatusCode().is2xxSuccessful()) {
			// add inlineAnnotation
			String networkEntityUUID = this.warehouseGraphService.addAnnotationToNetwork(contextMappingResponse.getBody(), drivergenes.getGenes());
			return new ResponseEntity<String>(networkEntityUUID, HttpStatus.CREATED);
		} else {
			return contextMappingResponse;
		}
	}
	
	/*
	 * @RequestMapping(value = "/mydrug", method=RequestMethod.POST) public
	 * ResponseEntity<NetworkInventoryItem>
	 * addMyDrugAnnotationToMapping(@RequestHeader("user") String username,
	 * 
	 * @RequestParam("networkUUID") String networkUUID,
	 * 
	 * @RequestParam("mydrugURL") String mydrugURL,
	 * 
	 * @RequestParam(name = "mappingType", defaultValue = "PATHWAYMAPPING")
	 * NetworkMappingType networkMappingType,
	 * 
	 * @RequestParam(name = "idSystem", defaultValue = "KEGG") IDSystem idSystem) {
	 * 
	 * // need a user Map<String, Object> agentNodeProperties = new HashMap<>();
	 * agentNodeProperties.put("graphagentname", username);
	 * agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
	 * ProvenanceGraphAgentNode userAgentNode =
	 * this.provenanceGraphService.createProvenanceGraphAgentNode(
	 * agentNodeProperties);
	 * 
	 * // need an activity //Instant createDate = Instant.now(); Map<String, Object>
	 * activityNodeProvenanceProperties = new HashMap<>();
	 * activityNodeProvenanceProperties.put("graphactivitytype",
	 * ProvenanceGraphActivityType.addMyDrugNodes);
	 * activityNodeProvenanceProperties.put("graphactivityname", mydrugURL);
	 * //activityNodeProvenanceProperties.put("createdate", createDate);
	 * 
	 * ProvenanceGraphActivityNode addmydrugActivityNode =
	 * this.provenanceGraphService.createProvenanceGraphActivityNode(
	 * activityNodeProvenanceProperties);
	 * 
	 * // Associate Activity with User Agent
	 * this.provenanceGraphService.connect(addmydrugActivityNode, userAgentNode,
	 * ProvenanceGraphEdgeType.wasAssociatedWith);
	 * 
	 * // check if basenetwork exists MappingNode baseNetwork =
	 * this.warehouseGraphService.getMappingNode(networkUUID); MappingNode
	 * newNetwork; if(baseNetwork == null) { PathwayNode basePathway =
	 * this.warehouseGraphService.getPathwayNode(username, networkUUID); if
	 * (basePathway == null) { return new
	 * ResponseEntity<NetworkInventoryItem>(HttpStatus.BAD_REQUEST); } else {
	 * logger.info("Using Pathway with uuid: " + networkUUID +
	 * " to build mapping with mydrug nodes"); // associate basePathway with
	 * activity this.provenanceGraphService.connect(addmydrugActivityNode,
	 * basePathway, ProvenanceGraphEdgeType.used); //create mapping from pathway
	 * ResponseEntity<WarehouseInventoryItem> mappingFromPathwayResponseEntity =
	 * createMappingFromWarehouseGraphNode(username, networkUUID,
	 * networkMappingType.name(), idSystem.name());
	 * if(!mappingFromPathwayResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
	 * return new
	 * ResponseEntity<NetworkInventoryItem>(mappingFromPathwayResponseEntity.
	 * getStatusCode()); } else { newNetwork =
	 * this.warehouseGraphService.getMappingNode(mappingFromPathwayResponseEntity.
	 * getBody().getEntityUUID()); } } } else { // associate baseNetwork with
	 * activity this.provenanceGraphService.connect(addmydrugActivityNode,
	 * baseNetwork, ProvenanceGraphEdgeType.used); // copy the network
	 * ResponseEntity<NetworkInventoryItemDetail>
	 * newNetworkInventoryItemDetailResponseEntity =
	 * this.createMappingFromMappingWithOptions(username, networkUUID,
	 * MappingStep.COPY,
	 * this.warehouseGraphService.getFilterOptions(WarehouseGraphNodeType.MAPPING,
	 * networkUUID));
	 * if(!newNetworkInventoryItemDetailResponseEntity.getStatusCode().equals(
	 * HttpStatus.OK)) { return new ResponseEntity<NetworkInventoryItem>(
	 * newNetworkInventoryItemDetailResponseEntity.getStatusCode()); } else {
	 * newNetwork = this.warehouseGraphService.getMappingNode(
	 * newNetworkInventoryItemDetailResponseEntity.getBody().getEntityUUID()); } }
	 * 
	 * // get all (compound)-[targets]->(gene) from mydrug (preferably as a stream)
	 * List<FlatSpecies> drugSpeciesForNewNetwork =
	 * this.httpService.getMyDrugCompoundsForNetwork(mydrugURL, newNetwork);
	 * 
	 * // save the newSpecies drugSpeciesForNewNetwork = (List<FlatSpecies>)
	 * this.networkMappingService.persistListOfFlatSpecies(drugSpeciesForNewNetwork)
	 * ;
	 * 
	 * 
	 * int numberOfRelatedSpecies = 0; Map<String, String> annotationTypeMap = new
	 * HashMap<>(); // connect new Species to activity and networknode
	 * for(FlatSpecies drug: drugSpeciesForNewNetwork) { numberOfRelatedSpecies +=
	 * drug.getTargetsSpeciesList().size(); for (String annotation :
	 * drug.getAnnotation().keySet()) {
	 * if(!annotationTypeMap.containsKey(annotation)) {
	 * annotationTypeMap.put(annotation, "String"); } }
	 * this.provenanceGraphService.connect(drug, addmydrugActivityNode,
	 * ProvenanceGraphEdgeType.wasGeneratedBy);
	 * this.warehouseGraphService.connect(newNetwork, drug,
	 * WarehouseGraphEdgeType.CONTAINS); } // update mappingNode
	 * newNetwork.addMappingNodeType("Drug");
	 * newNetwork.addMappingRelationType("targets");
	 * newNetwork.setAnnotationType(annotationTypeMap);
	 * 
	 * try { int numNodes =
	 * Integer.parseInt((String)newNetwork.getWarehouse().get("numberofnodes"));
	 * numNodes += drugSpeciesForNewNetwork.size();
	 * newNetwork.addWarehouseAnnotation("numberofnodes", String.valueOf(numNodes));
	 * int numRelations =
	 * Integer.parseInt((String)newNetwork.getWarehouse().get("numberofrelations"));
	 * numRelations += numberOfRelatedSpecies;
	 * newNetwork.addWarehouseAnnotation("numberofrelations",
	 * String.valueOf(numRelations));
	 * newNetwork.addWarehouseAnnotation("creationendtime",
	 * Instant.now().toString()); } catch (ClassCastException e) { // TODO
	 * Auto-generated catch block e.printStackTrace();
	 * logger.info("ClassCastException for annotation"); return new
	 * ResponseEntity<NetworkInventoryItem>(HttpStatus.INTERNAL_SERVER_ERROR); }
	 * catch (NumberFormatException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); logger.info("NumberFormatException for annotation");
	 * return new
	 * ResponseEntity<NetworkInventoryItem>(HttpStatus.INTERNAL_SERVER_ERROR); }
	 * 
	 * newNetwork.setMappingName(newNetwork.getMappingName() +
	 * "_AddedMyDrugAnnotations_From_" + mydrugURL); newNetwork = (MappingNode)
	 * this.warehouseGraphService.saveWarehouseGraphNodeEntity(newNetwork, 0);
	 * this.provenanceGraphService.connect(newNetwork, addmydrugActivityNode,
	 * ProvenanceGraphEdgeType.wasGeneratedBy);
	 * 
	 * // done NetworkInventoryItem fullNetworkWithMyDrugNodesItem =
	 * this.warehouseGraphService.getNetworkInventoryItem(newNetwork.getEntityUUID()
	 * ); if(fullNetworkWithMyDrugNodesItem != null ) { return new
	 * ResponseEntity<NetworkInventoryItem>(fullNetworkWithMyDrugNodesItem,
	 * HttpStatus.OK); } else { return new
	 * ResponseEntity<NetworkInventoryItem>(HttpStatus.INTERNAL_SERVER_ERROR); }
	 * 
	 * }
	 */

	
	
}
