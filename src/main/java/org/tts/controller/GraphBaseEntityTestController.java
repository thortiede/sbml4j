package org.tts.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
	
	
	@RequestMapping(value = "/testMyDrug", method = RequestMethod.POST)
	public ResponseEntity<List<FlatSpecies>> testMyDrug(@RequestHeader("user") String username,
														@RequestParam("networkUUID") String networkUUID,
														@RequestParam("mydrugURL") String mydrugURL,
														@RequestParam(name = "mappingType", defaultValue = "PATHWAYMAPPING") NetworkMappingType networkMappingType,
														@RequestParam(name = "idSystem", defaultValue = "KEGG") IDSystem idSystem) {
															
		
		//this.httpService.getMyDrugCompoundsForNetwork(mydrugURL, (MappingNode) this.graphBaseEntityService.findByEntityUUID(networkUUID));
		return new ResponseEntity<List<FlatSpecies>>(this.httpService.getMyDrugCompoundsForNetwork(mydrugURL, (MappingNode) this.graphBaseEntityService.findByEntityUUID(networkUUID)), HttpStatus.OK);
	
		
		
	}
	

	
	
}
