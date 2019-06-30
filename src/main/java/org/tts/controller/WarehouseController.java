package org.tts.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.service.WarehouseGraphService;

@RestController
public class WarehouseController {

	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	
	@RequestMapping(value="/pathwayInventory", method = RequestMethod.GET)
	public List<PathwayInventoryItem> listAllPathways(@RequestHeader("user") String username) {
		
		return this.warehouseGraphService.getListofPathwayInventory(username);
		
	}
	
}
