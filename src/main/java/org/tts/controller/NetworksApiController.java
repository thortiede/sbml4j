/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.tts.service.MyDrugService;
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

	Logger log = LoggerFactory.getLogger(NetworksApiController.class);
	
	@Autowired
	ContextService contextService;
	
	@Autowired
	MappingNodeService mappingNodeService;

	@Autowired
	MyDrugService myDrugService;
	
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
			UUID UUID, boolean derive) {
		
		log.info("Serving POST /networks/" + UUID.toString() + "/annotation for user " + user + " with AnnotationItem " + body.toString());
		
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
		MappingNode annotatedNetwork = this.networkService.annotateNetwork(user, body, UUID.toString(), derive);
	
		// 4. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(annotatedNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
    
	@Override
	public ResponseEntity<NetworkInventoryItem> addMyDrugRelations(String user, UUID UUID,
			@NotNull @Valid String myDrugURL) {
		
		log.info("Serving POST /networks/" + UUID.toString() + "/mydrug for user " + user + " with myDrugURL " + myDrugURL);
		
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
		// 3. Copy the network
		MappingNode networkCopy = this.networkService.copyNetwork(UUID.toString(), user, "MyDrugOn");
		// 4. Add MyDrug Nodes/Edges
		networkCopy = this.myDrugService.addMyDrugToNetwork(networkCopy.getEntityUUID(), myDrugURL);
		// 5. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(networkCopy.getEntityUUID()), HttpStatus.CREATED);
	}
	
	@Override
	public ResponseEntity<NetworkInventoryItem> copyNetwork(String user, UUID UUID) {
		
		log.info("Serving POST /networks/" + UUID.toString() + " for user " + user);
		
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
		MappingNode networkCopy = this.networkService.copyNetwork(UUID.toString(), user, "CopyOf");
		// 4. Network not created?
		if (networkCopy == null) {
			return ResponseEntity.badRequest().header("reason", "There has been an error creating a copy of the network with entityUUID: " + UUID.toString()).build();
		}
		// 5. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(networkCopy.getEntityUUID()), HttpStatus.CREATED);
		
	}
	
	@Override
	public ResponseEntity<Void> deleteNetwork(String user, UUID UUID) {
		
		log.info("Serving DELETE /networks/" + UUID.toString() + " for user " + user);
		
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
		
		log.info("Serving POST /networks/" + UUID.toString() + "/filter for user " + user + " with filterOptions " + body.toString());
		
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
		
		log.info("Serving GET /networks/" + UUID.toString() + "/context for user " + user + " with genes " + genes);
		
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
		
		log.info("Serving GET /networks/" + UUID.toString() + " for user " + user);
		
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
		
		log.info("Serving GET /networks/" + UUID.toString() + "/options for user " + user);
		
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
		
		log.info("Serving GET /networks for user " + user);
		
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
		
		log.info("Serving POST /networks/" + UUID.toString() + "/context for user " + user + " with NodeList " + body.toString());
		
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
		// 6. Create the mapping holding the context
		MappingNode contextNetwork = this.networkService.createMappingFromFlatEdges(user, UUID.toString(), contextFlatEdges, networkName.toString());
		// 7. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(contextNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	
}
