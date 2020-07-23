package org.tts.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.MappingStep;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.GraphBaseEntityService;
import org.tts.service.HttpService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.WarehouseGraphService;

@RestController
public class GraphBaseEntityTestController {

	GraphBaseEntityService graphBaseEntityService;
	HttpService httpService;
	WarehouseGraphService warehouseGraphService;
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	public GraphBaseEntityTestController(GraphBaseEntityService graphBaseEntityService,
										HttpService httpService,
										WarehouseGraphService warehouseGraphService,
										ProvenanceGraphService provenanceGraphService) {
		super();
		this.graphBaseEntityService = graphBaseEntityService;
		this.httpService = httpService;
		this.warehouseGraphService = warehouseGraphService;
		this.provenanceGraphService = provenanceGraphService;
	}
	/**
	 * Test GraphBaseEntity
	 */

	@RequestMapping(value = "/testListBaseEntities", method=RequestMethod.GET)	
	public Iterable<GraphBaseEntity> getBaseEntities() {
		
		return graphBaseEntityService.findAll();
	}
	
	
	@RequestMapping(value = "/testBaseEntity/{uuid}", method=RequestMethod.GET)	
	public GraphBaseEntity getNewBaseEntity(@PathVariable String uuid) {
		
		return graphBaseEntityService.findByEntityUUID(uuid);
	}
	
	@RequestMapping(value = "/testBaseEntity", method=RequestMethod.POST)	
	public GraphBaseEntity createNewBaseEntity() {
		GraphBaseEntity newEntity = new GraphBaseEntity();
		newEntity.setEntityUUID(UUID.randomUUID().toString());
		return graphBaseEntityService.persistEntity(newEntity, 0);
	}
	
	/**
	 * Test SBMLSBaseEntity
	 */
	
	
	@RequestMapping(value = "/testSBase", method=RequestMethod.POST)	
	public GraphBaseEntity createNewSBaseEntity(@RequestParam(value = "sbaseId") String sBaseId,
												@RequestParam(value = "sbaseName") String sBaseName) {
		SBMLSBaseEntity newEntity = new SBMLSBaseEntity();
		newEntity.setEntityUUID(UUID.randomUUID().toString());
		newEntity.setsBaseId(sBaseId);
		newEntity.setsBaseName(sBaseName);
		return graphBaseEntityService.persistEntity(newEntity, 0);
	}
	
	@RequestMapping(value = "/testSBase/{uuid}", method=RequestMethod.GET)
	public GraphBaseEntity getGraphBaseSBase(@PathVariable String uuid) {
		return graphBaseEntityService.findByEntityUUID(uuid);
	}
	
	
	/*
	 * @RequestMapping(value = "/testMyDrug", method = RequestMethod.POST) public
	 * ResponseEntity<List<FlatSpecies>> testMyDrug(@RequestHeader("user") String
	 * username,
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
	 * 
	 * //this.httpService.getMyDrugCompoundsForNetwork(mydrugURL, (MappingNode)
	 * this.graphBaseEntityService.findByEntityUUID(networkUUID)); return new
	 * ResponseEntity<List<FlatSpecies>>(this.httpService.
	 * getMyDrugCompoundsForNetwork(mydrugURL, (MappingNode)
	 * this.graphBaseEntityService.findByEntityUUID(networkUUID)), HttpStatus.OK);
	 * 
	 * 
	 * 
	 * }
	 */
	
	@RequestMapping(value="/testSubgrapExtract", method = RequestMethod.GET)
	public ResponseEntity<String> testSubgraphExtract(@RequestParam("EntityUUID")String entityUUID) {
		String ret = this.graphBaseEntityService.testFlatMappingExtract(entityUUID);
		return new ResponseEntity<>(ret, HttpStatus.OK);
	}
	
	
	
	
	@RequestMapping(value="/multiGeneSubNet", method = RequestMethod.POST)
	public ResponseEntity<Resource> testMultiGeneSubNet(@RequestParam("networkEntityUUID")String networkEntityUUID,
														@RequestParam("genes")List<String> genes,
														@RequestBody FilterOptions options) {
		String contentType = "application/octet-stream";
		Resource resource = null;
		if(genes == null || genes.size() < 1) {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} else {
			//Resource resource = this.graphBaseEntityService.testMultiGeneSubNet(networkEntityUUID, genes, options);
			
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
					 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.graphml\"").body(resource) ;
			 
		}
	}
	
	
	
	@RequestMapping(value="/testMyDrug", method=RequestMethod.GET)
	public ResponseEntity<Resource> testMyDrug(@RequestParam("networkEntityUUID")String networkEntityUUID,
												@RequestParam("myDrugURL")String myDrugURL,
												@RequestHeader("user") String user)
	{
		
		MappingNode parentMapping = this.warehouseGraphService.getMappingNode(networkEntityUUID);
		MappingStep myDrugCopyStep = MappingStep.COPY;
		
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", user);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(agentNodeProperties);
		
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createMapping);
		String activityName = "Create_Mapping_from_" + networkEntityUUID + "_byApplyingStep_" + myDrugCopyStep.name() + "_and_MyDrugQuery";
		activityNodeProvenanceProperties.put("graphactivityname", activityName);
		ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		
		MappingNode myDrugMapping = this.warehouseGraphService.createMappingNode(parentMapping, parentMapping.getMappingType(),
				parentMapping.getMappingName() + "_" + myDrugCopyStep.name() + "_for_myDrug");

		this.provenanceGraphService.connect(myDrugMapping, parentMapping, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(myDrugMapping, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		this.provenanceGraphService.connect(myDrugMapping, createMappingActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy); 
		System.out.println("Reaching here");
		myDrugMapping = this.warehouseGraphService.createMappingFromMappingWithOptions(parentMapping, myDrugMapping, this.warehouseGraphService.getFilterOptions(WarehouseGraphNodeType.MAPPING, networkEntityUUID),
				myDrugCopyStep);
		System.out.println("And here");
		Resource resource = this.graphBaseEntityService.testMyDrug(myDrugMapping.getEntityUUID(), myDrugURL);
		System.out.println("Also Reaching here");
		if(resource == null) {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} else {
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType("application/octet-stream"))
					 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+ resource.getFilename()).body(resource) ;
		}
	}
	
}
