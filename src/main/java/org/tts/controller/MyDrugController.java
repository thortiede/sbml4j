package org.tts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.common.GraphEnum.MappingStep;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.GraphBaseEntityService;
import org.tts.service.MyDrugService;
import org.tts.service.WarehouseGraphService;

@RestController
public class MyDrugController {

	@Autowired
	MyDrugService myDrugService;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@RequestMapping(value = "/networks/{uuid}/myDrug", method = RequestMethod.POST)
	public ResponseEntity<NetworkInventoryItem> addMyDrugRelations( @RequestHeader(value = "user") String username,
								@PathVariable(value="uuid") String parentUUID,
								@RequestParam(value="myDrugURL") String myDrugURL) {
		
		MappingNode parent;
		try {
			parent = (MappingNode) this.graphBaseEntityService.findByEntityUUID(parentUUID);
		} catch (ClassCastException e) {
			logger.error("Could not cast to MappingNode for uuid: " + parentUUID);
			return ResponseEntity.badRequest().header("reason", "UUID is not a valid network UUID").build();
		} catch (Exception e) {
			logger.error("Failed to fetch parent Network (" + parentUUID + ") for MyDrug: " + e.getMessage());
			return ResponseEntity.badRequest().header("reason", "Error finding network with UUID given.").build();
		}
		
		String newMappingName = parent.getMappingName() + "_" + MappingStep.COPY.name() + "_for_myDrug";
		
		String activityName = "Create_Mapping_from_" + parentUUID + "_byApplyingStep_" + MappingStep.COPY.name() + "_and_MyDrugQuery";
		
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createMapping;
		NetworkMappingType mappingType = parent.getMappingType();
		
		MappingNode myDrugMapping = this.warehouseGraphService.createMappingPre(username, parent, newMappingName, activityName, activityType,
				mappingType);
		
		myDrugMapping = this.warehouseGraphService.createMappingFromMappingWithOptions(parent, myDrugMapping, this.warehouseGraphService.getFilterOptions(WarehouseGraphNodeType.MAPPING, parent.getEntityUUID()),
				MappingStep.COPY);
		
		myDrugMapping = this.myDrugService.addMyDrugToNetwork(myDrugMapping.getEntityUUID(), myDrugURL);
		
		return new ResponseEntity<NetworkInventoryItem>(this.warehouseGraphService.getNetworkInventoryItem(myDrugMapping.getEntityUUID()), HttpStatus.CREATED);
		
	}
	
}
