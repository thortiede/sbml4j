package org.tts.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.GraphBaseEntity;
import org.tts.model.SBMLSBaseEntity;
import org.tts.service.GraphBaseEntityService;

@RestController
public class GraphBaseEntityTestController {

	GraphBaseEntityService graphBaseEntityService;

	@Autowired
	public GraphBaseEntityTestController(GraphBaseEntityService graphBaseEntityService) {
		super();
		this.graphBaseEntityService = graphBaseEntityService;
	}
	/**
	 * Test GraphBaseEntity
	 */

	@RequestMapping(value = "/testListBaseEntities", method=RequestMethod.GET)	
	public Iterable<GraphBaseEntity> getBaseEntities() {
		
		return graphBaseEntityService.findAll();
	}
	
	
	@RequestMapping(value = "/testBaseEntity", method=RequestMethod.GET)	
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
		newEntity.setSbaseId(sBaseId);
		newEntity.setsBaseName(sBaseName);
		return graphBaseEntityService.persistEntity(newEntity);
	}
	
	@RequestMapping(value = "/testSBase/{uuid}", method=RequestMethod.GET)
	public GraphBaseEntity getGraphBaseSBase(@PathVariable String uuid) {
		return graphBaseEntityService.findByEntityUUID(uuid);
	}
	
	
	
	
	
	
}
