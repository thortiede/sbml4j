/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
 */

package org.tts.controller;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.NetworksApi;
import org.tts.model.api.AnnotationItem;
import org.tts.model.api.FilterOptions;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.NetworkOptions;
import org.tts.model.api.NodeList;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.WarehouseGraphService;
import org.tts.service.networks.NetworkService;
import org.tts.service.warehouse.MappingNodeService;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
/**
 * Controller class for all things networks related
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class NetworksApiController implements NetworksApi {

	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> addAnnotationToNetwork(@Valid AnnotationItem body, String user,
			UUID UUID) {
		
		MappingNode parent = this.warehouseGraphService.getMappingNode(UUID.toString());
		String newMappingName = parent.getMappingName() + "_annotate_with" + 
				(body.getNodeAnnotationName() != null ? "_nodeAnnotation:" + body.getNodeAnnotationName() : "") + 
				(body.getRelationAnnotationName() != null ? "_relationAnnotation:" + body.getRelationAnnotationName() : "");
		
		String activityName = "Add_annotation_to_mapping_" + UUID.toString() + 
				(body.getNodeAnnotationName() != null ? "_nodeAnnotation:" + body.getNodeAnnotationName() : "") + 
				(body.getRelationAnnotationName() != null ? "_relationAnnotation:" + body.getRelationAnnotationName() : "");
		
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createMapping;
		NetworkMappingType mappingType = parent.getMappingType();
		
		MappingNode newMapping = this.warehouseGraphService.createMappingPre(user, parent, newMappingName, activityName, activityType,
				mappingType);

		//newMapping = this.warehouseGraphService.createMappingFromMappingWithOptions(parent, newMapping, options, step);
		
		
		
		
		return NetworksApi.super.addAnnotationToNetwork(body, user, UUID);
	}
    
	@Override
	public ResponseEntity<NetworkInventoryItem> addMyDrugRelations(String user, UUID UUID,
			@NotNull @Valid String myDrugURL) {
		// TODO Auto-generated method stub
		return NetworksApi.super.addMyDrugRelations(user, UUID, myDrugURL);
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> copyNetwork(String user, UUID UUID) {
		// TODO Auto-generated method stub
		return NetworksApi.super.copyNetwork(user, UUID);
	}
	
	@Override
	public ResponseEntity<Void> deleteNetwork(String user, UUID UUID) {
		// TODO Auto-generated method stub
		return NetworksApi.super.deleteNetwork(user, UUID);
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> filterNetwork(@Valid FilterOptions body, String user, UUID UUID) {
		// TODO Auto-generated method stub
		return NetworksApi.super.filterNetwork(body, user, UUID);
	}
	
	@Override
	public ResponseEntity<Resource> getContext(String user, UUID UUID, @NotNull @Valid String genes,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid Boolean terminateAtDrug) {
		// TODO Auto-generated method stub
		return NetworksApi.super.getContext(user, UUID, genes, minSize, maxSize, terminateAtDrug);
	}
	
	@Override
	public ResponseEntity<Resource> getNetwork(String user, UUID UUID, @Valid boolean directed) {
		// TODO Auto-generated method stub
		return NetworksApi.super.getNetwork(user, UUID, directed);
	}
	
	@Override
	public ResponseEntity<NetworkOptions> getNetworkOptions(String user, UUID UUID) {
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<NetworkOptions>(HttpStatus.FORBIDDEN);
		}
		NetworkOptions networkOptions = this.networkService.getNetworkOptions(UUID.toString(), user);
		if (networkOptions.getAnnotation() == null || networkOptions.getFilter() == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(networkOptions);
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> listAllNetworks(String user) {
		// TODO Auto-generated method stub
		return NetworksApi.super.listAllNetworks(user);
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> postContext(@Valid NodeList body, String user, UUID UUID,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid Boolean terminateAtDrug) {
		// TODO Auto-generated method stub
		return NetworksApi.super.postContext(body, user, UUID, minSize, maxSize, terminateAtDrug);
	}
	
	
}
