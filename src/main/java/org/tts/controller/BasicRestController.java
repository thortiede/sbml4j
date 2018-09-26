package org.tts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.NodeEdgeList;
import org.tts.service.NodeEdgeListService;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class BasicRestController {

	@Autowired
	NodeEdgeListService nodeEdgeListService;
	
	@RequestMapping(value = "/fullnet", method=RequestMethod.GET)
	public NodeEdgeList fullnet() {
		return nodeEdgeListService.getFullNet();
	}
	
	
}
