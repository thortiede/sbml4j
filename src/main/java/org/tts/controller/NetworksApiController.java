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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.tts.config.SBML4jConfig;
import org.tts.model.api.AnnotationItem;
import org.tts.model.api.FilterOptions;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.NetworkOptions;
import org.tts.model.api.NodeList;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.ContextService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.WarehouseGraphService;
import org.tts.service.networks.NetworkResourceService;
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
	ContextService contextService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	NetworkResourceService networkResourceService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> addAnnotationToNetwork(@Valid AnnotationItem body, String user,
			UUID UUID) {
		
		MappingNode parent = this.mappingNodeService.findByEntityUUID(UUID.toString());
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
		// 1. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		MappingNode networkCopy = this.networkService.copyNetwork(UUID.toString());
		// TODO Auto-generated method stub
		return NetworksApi.super.copyNetwork(user, UUID);
	}
	
	@Override
	public ResponseEntity<Void> deleteNetwork(String user, UUID UUID) {
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.notFound().build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
		}
		// 3. Delete network / Deactivate network
		boolean isDeleted = false;
		if (sbml4jConfig.getNetworkConfigProperties().isHardDelete()) {
			isDeleted = this.networkService.deleteNetwork(UUID.toString());
		} else {
			isDeleted = this.networkService.deactivateNetwork(UUID.toString());
		}
		if(isDeleted) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.badRequest().header("reason", "Found network associated to user, but failed to delete it").build();
		}
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> filterNetwork(@Valid FilterOptions body, String user, UUID UUID) {
		// TODO Auto-generated method stub
		return NetworksApi.super.filterNetwork(body, user, UUID);
	}
	
	@Override
	public ResponseEntity<Resource> getContext(String user, UUID UUID, @NotNull @Valid String genes,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid Boolean terminateAtDrug, @Valid String direction, @Valid boolean directed) {
		
		// 1. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<Resource>(HttpStatus.FORBIDDEN);
		}
		// 2. Extract the genes
		List<String> geneNames = Arrays.asList(genes.split(", "));
		if(geneNames.size() < 1) {
			return ResponseEntity.badRequest().header("reason", "Must give at least on gene").build();
		}
		
		List<FlatEdge> contextFlatEdges = this.contextService.getNetworkContextFlatEdges(UUID.toString(), geneNames, minSize, maxSize, terminateAtDrug, direction);
		if(contextFlatEdges == null) {
			return ResponseEntity.badRequest().header("reason", "Could not get network context").build();
		}
		StringBuilder filename = new StringBuilder();
		filename.append("context");
		for (String gene : geneNames) {
			filename.append("_");
			filename.append(gene);
		}
		filename.append("_IN_");
		filename.append(UUID.toString());
		filename.append(".graphml");
		Resource contextResource = this.networkResourceService.getNetworkForFlatEdges(contextFlatEdges, directed, filename.toString());
		if(contextResource == null) {
			return ResponseEntity.badRequest().header("reason", "Failed to create graphML resource").build();
		}
		
		return ResponseEntity.ok(contextResource);
	}
	
	@Override
	public ResponseEntity<Resource> getNetwork(String user, UUID UUID, @Valid boolean directed) {
		
		// does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<Resource>(HttpStatus.FORBIDDEN);
		}
		Resource networkResource = this.networkResourceService.getNetwork(UUID.toString(), directed);
		if (networkResource == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(networkResource);
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
		// does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		List<NetworkInventoryItem> userNetworkInventoryItems = new ArrayList<>();
		List<MappingNode> userNetworks = this.mappingNodeService.findAllFromUser(user, !this.sbml4jConfig.getNetworkConfigProperties().isShowInactiveNetworks());
		for (MappingNode userMap : userNetworks) {
			NetworkInventoryItem item = this.networkService.getNetworkInventoryItem(userMap.getEntityUUID());
			userNetworkInventoryItems.add(item);
		}
		return ResponseEntity.ok(userNetworkInventoryItems);
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> postContext(@Valid NodeList body, String user, UUID UUID,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid Boolean terminateAtDrug, @Valid String direction) {
		// TODO Auto-generated method stub
		return NetworksApi.super.postContext(body, user, UUID, minSize, maxSize, terminateAtDrug, direction);
	}
	
	
}
