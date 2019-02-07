package org.tts.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
	
	@RequestMapping(value = "/testCreateEntity", method=RequestMethod.GET)	
	public GraphBaseEntity createNewBaseEntity() {
		GraphBaseEntity newEntity = new GraphBaseEntity();
		newEntity.setEntityUUID(UUID.randomUUID().toString());
		return graphBaseEntityService.persistEntity(newEntity);
	}
	
	@RequestMapping(value = "/testCreateSBase", method=RequestMethod.GET)	
	public GraphBaseEntity createNewSBaseEntity() {
		SBMLSBaseEntity newEntity = new SBMLSBaseEntity();
		newEntity.setEntityUUID(UUID.randomUUID().toString());
		newEntity.setSbaseId("123");
		return graphBaseEntityService.persistEntity(newEntity);
	}
	
	
	
	
}
