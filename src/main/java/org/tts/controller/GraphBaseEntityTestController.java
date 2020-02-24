package org.tts.controller;

import java.util.List;
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
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.service.GraphBaseEntityService;
import org.tts.service.HttpService;

@RestController
public class GraphBaseEntityTestController {

	GraphBaseEntityService graphBaseEntityService;
	HttpService httpService;

	@Autowired
	public GraphBaseEntityTestController(GraphBaseEntityService graphBaseEntityService,
										HttpService httpService) {
		super();
		this.graphBaseEntityService = graphBaseEntityService;
		this.httpService = httpService;
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
		return graphBaseEntityService.persistEntity(newEntity);
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
		return graphBaseEntityService.persistEntity(newEntity);
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
	
	@RequestMapping(value="/testContext", method = RequestMethod.GET)
	public ResponseEntity<Resource> testContext(@RequestParam("networkEntityUUID")String networkEntityUUID, 
												@RequestParam("geneSymbol")String geneSymbol) {
		Resource resource = this.graphBaseEntityService.testContextMethod(networkEntityUUID, geneSymbol);
		String contentType = "application/octet-stream";
		if(resource != null) {
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"networkContext_"+ geneSymbol + ".graphml\"") .body(resource); 
		} else { 
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} 	}
	
	@RequestMapping(value="/testGetNet", method = RequestMethod.GET)
	public ResponseEntity<Resource> testGetNet(@RequestParam("networkEntityUUID")String networkEntityUUID) {
		Resource resource = this.graphBaseEntityService.testGetNet(networkEntityUUID);
		String contentType = "application/octet-stream";
		if(resource != null) {
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"network.graphml\"") .body(resource); 
		} else { 
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} 
	}
	
	@RequestMapping(value="/testMultiGeneSubNet", method = RequestMethod.POST)
	public ResponseEntity<Resource> testMultiGeneSubNet(@RequestParam("networkEntityUUID")String networkEntityUUID,
														@RequestParam("genes")List<String> genes,
														@RequestBody FilterOptions options) {
		String contentType = "application/octet-stream";
		if(genes == null || genes.size() < 1) {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} else {
			Resource resource = this.graphBaseEntityService.testMultiGeneSubNet(networkEntityUUID, genes, options);
			
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
					 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.graphml\"").body(resource) ;
			 
		}
	}
	
	@RequestMapping(value="/testGeneSet", method = RequestMethod.GET)
	public ResponseEntity<Resource> testGeneSet(@RequestParam("networkEntityUUID")String networkEntityUUID,
												@RequestParam("genes")List<String> genes){
													
		Resource resource = this.graphBaseEntityService.testGetNet(networkEntityUUID, genes);
		if(resource == null) {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT); 
		} else {
			String contentType = "application/octet-stream";
			return ResponseEntity.ok() .contentType(MediaType.parseMediaType(contentType))
				 .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"geneset.graphml\"").body(resource) ;
		} 
	}
}
