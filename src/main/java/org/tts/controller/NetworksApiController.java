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
	public ResponseEntity<NetworkInventoryItem> addAnnotationToNetwork(@Valid AnnotationItem body, String user,
			UUID UUID) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.notFound().build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<NetworkInventoryItem>(HttpStatus.FORBIDDEN);
		}
		// 3. Add annotation to network
		MappingNode annotatedNetwork = this.networkService.annotateNetwork(user, body, UUID.toString());
	
		// 4. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(annotatedNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
    
	@Override
	public ResponseEntity<NetworkInventoryItem> addMyDrugRelations(String user, UUID UUID,
			@NotNull @Valid String myDrugURL) {
		// TODO Auto-generated method stub
		return NetworksApi.super.addMyDrugRelations(user, UUID, myDrugURL);
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> copyNetwork(String user, UUID UUID) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.notFound().build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		// 3. Copy the network
		MappingNode networkCopy = this.networkService.copyNetwork(UUID.toString(), user);
		// 4. Network not created?
		if (networkCopy == null) {
			return ResponseEntity.badRequest().header("reason", "There has been an error creating a copy of the network with entityUUID: " + UUID.toString()).build();
		}
		// 5. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(networkCopy.getEntityUUID()), HttpStatus.CREATED);
		
	}
	
	@Override
	public ResponseEntity<Void> deleteNetwork(String user, UUID UUID) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
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
	public ResponseEntity<NetworkInventoryItem> filterNetwork(@Valid FilterOptions body, String user, UUID UUID) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.badRequest().header("reason", "Network with entityUUID " + UUID.toString() + " does not exist.").build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<NetworkInventoryItem>(HttpStatus.FORBIDDEN);
		}
		MappingNode filteredNetwork = this.networkService.filterNetwork(UUID.toString(), body, user);
		
		// 3. Network not created?
		if (filteredNetwork == null) {
			return ResponseEntity.badRequest().header("reason", "There has been an error filtering the network with entityUUID: " + UUID.toString()).build();
		}
		// 4. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(filteredNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	@Override
	public ResponseEntity<Resource> getContext(String user, UUID UUID, @NotNull @Valid String genes,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid Boolean terminateAtDrug, @Valid String direction, @Valid boolean directed) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.badRequest().header("reason", "Network with entityUUID " + UUID.toString() + " does not exist.").build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<Resource>(HttpStatus.FORBIDDEN);
		}
		// 3. Extract the genes
		List<String> geneNames = Arrays.asList(genes.split(", "));
		if(geneNames.size() < 1) {
			return ResponseEntity.badRequest().header("reason", "Must give at least on gene").build();
		}
		// 4. Get the FlatEdges making up the context
		List<FlatEdge> contextFlatEdges = this.contextService.getNetworkContextFlatEdges(UUID.toString(), geneNames, minSize, maxSize, terminateAtDrug, direction);
		if(contextFlatEdges == null) {
			return ResponseEntity.badRequest().header("reason", "Could not get network context").build();
		}
		// 5. Create a filename for the output file
		StringBuilder filename = new StringBuilder();
		filename.append("context");
		for (String gene : geneNames) {
			filename.append("_");
			filename.append(gene);
		}
		filename.append("_IN_");
		filename.append(UUID.toString());
		filename.append(".graphml");
		// 6. Get the Resource containing the GraphML of the context
		Resource contextResource = this.networkResourceService.getNetworkForFlatEdges(contextFlatEdges, directed, filename.toString());
		if(contextResource == null) {
			return ResponseEntity.badRequest().header("reason", "Failed to create graphML resource").build();
		}
		return ResponseEntity.ok(contextResource);
	}
	
	@Override
	public ResponseEntity<Resource> getNetwork(String user, UUID UUID, @Valid boolean directed) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.badRequest().header("reason", "Network with entityUUID " + UUID.toString() + " does not exist.").build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<Resource>(HttpStatus.FORBIDDEN);
		}
		// 3. Get the network as a GraphML Resource
		Resource networkResource = this.networkResourceService.getNetwork(UUID.toString(), directed);
		if (networkResource == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(networkResource);
	}
	
	@Override
	public ResponseEntity<NetworkOptions> getNetworkOptions(String user, UUID UUID) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.badRequest().header("reason", "Network with entityUUID " + UUID.toString() + " does not exist.").build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		// 3. Get the network options
		NetworkOptions networkOptions = this.networkService.getNetworkOptions(UUID.toString(), user);
		if (networkOptions.getAnnotation() == null || networkOptions.getFilter() == null) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(networkOptions);
	}
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> listAllNetworks(String user) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Find all networks for that user
		List<NetworkInventoryItem> userNetworkInventoryItems = new ArrayList<>();
		List<MappingNode> userNetworks = this.mappingNodeService.findAllFromUser(user, !this.sbml4jConfig.getNetworkConfigProperties().isShowInactiveNetworks());
		// 2. Get NetworkInventoryItem for each of them
		for (MappingNode userMap : userNetworks) {
			NetworkInventoryItem item = this.networkService.getNetworkInventoryItem(userMap.getEntityUUID());
			userNetworkInventoryItems.add(item);
		}
		return ResponseEntity.ok(userNetworkInventoryItems);
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> postContext(@Valid NodeList body, String user, UUID UUID,
			@Valid Integer minSize, @Valid Integer maxSize, @Valid Boolean terminateAtDrug, @Valid String direction) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(UUID.toString()) == null) {
			return ResponseEntity.badRequest().header("reason", "Network with entityUUID " + UUID.toString() + " does not exist.").build();
		}
		// 2. Is the user allowed to work on that network?
		if (!this.mappingNodeService.isMappingNodeAttributedToUser(UUID.toString(), user)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		// 3. Extract the genes
		List<String> geneNames = body.getGenes();
		if(geneNames.size() < 1) {
			return ResponseEntity.badRequest().header("reason", "Must give at least on gene").build();
		}
		// 4. Get the FlatEdges making up the context
		List<FlatEdge> contextFlatEdges = this.contextService.getNetworkContextFlatEdges(UUID.toString(), geneNames, minSize, maxSize, terminateAtDrug, direction);
		if(contextFlatEdges == null) {
			return ResponseEntity.badRequest().header("reason", "Could not get network context").build();
		}
		// 5. Create a networkName for the new network file
		StringBuilder networkName = new StringBuilder();
		networkName.append("context");
		for (String gene : geneNames) {
			networkName.append("_");
			networkName.append(gene);
		}
		networkName.append("_IN_");
		networkName.append(UUID.toString());
		networkName.append(".graphml");
		// 6. Create the mapping holding the context
		MappingNode contextNetwork = this.networkService.createMappingFromFlatEdges(user, UUID.toString(), contextFlatEdges, networkName.toString());
		// 7. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(contextNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	
}
