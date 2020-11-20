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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.OverviewApi;
import org.tts.config.OverviewNetworkConfig;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.OverviewNetworkItem;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.ContextService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.networks.NetworkResourceService;
import org.tts.service.networks.NetworkService;
import org.tts.service.warehouse.MappingNodeService;

/**
 * Controller for handling of VCF analysis tasks
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class OverviewApiController implements OverviewApi {

	Logger log = LoggerFactory.getLogger(OverviewApiController.class);
	
	@Autowired
	ContextService contextService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	NetworkResourceService networkResourceService;
	
	@Autowired
	OverviewNetworkConfig overviewNetworkConfig;
	
	/**
	 * Create a network context for the genes in OverviewNetworkItem and annotate the given nodes with the given annotation
	 */
	@Override
	public ResponseEntity<NetworkInventoryItem> createOverviewNetwork(@Valid OverviewNetworkItem overviewNetworkItem, String user) {
		
		log.info("Serving POST /overview for user " + user + " with overviewNetworkItem: " + overviewNetworkItem.toString());
		
		// 1a. BaseNetworkUUID provided?
		String networkEntityUUID;
		boolean baseNetworkUUIDprovided = false;
		if (overviewNetworkItem.getBaseNetworkUUID() == null) {
			networkEntityUUID = this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getBaseNetworkUUID();
			log.debug("Using baseNetwork given by properties: " + networkEntityUUID);
		} else {
			networkEntityUUID = overviewNetworkItem.getBaseNetworkUUID().toString();
			baseNetworkUUIDprovided = true;
			log.debug("Using baseNetwork given input: " + networkEntityUUID);
		}
		// 1b. AnnotationName provided?
		if (overviewNetworkItem.getAnnotationName() == null || overviewNetworkItem.getAnnotationName().equals("")) {
			return ResponseEntity.badRequest().header("reason", "Annotation name not provided in request body in the annotationName - field.").build();
		}
		// 2. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(networkEntityUUID) == null) {
			if (baseNetworkUUIDprovided) {
				return ResponseEntity.badRequest().header("reason", "Could not find the base network provided via the baseNetworkUUID.").build();
			} else {
				return ResponseEntity.badRequest().header("reason", "Could not find the default base network provided in the configuration.").build();
			}
		}
		
		// 3. Create the user if not existent
		ProvenanceGraphAgentNode graphAgent = this.provenanceGraphService.createProvenanceGraphAgentNode(user, ProvenanceGraphAgentType.User);
		
		List<String> geneNames = overviewNetworkItem.getGenes();
		// 4. Any genes provided?
		if (geneNames == null || geneNames.isEmpty()) {
			return ResponseEntity.badRequest().header("reason", "Genes not provided in body").build();
		}
		// 5. Check whether we know at least one of the genes provided
		boolean foundGene = false;
		for (String gene : geneNames) {
			if (this.networkService.getFlatSpeciesEntityUUIDOfSymbolInNetwork(networkEntityUUID, gene) != null) {
				foundGene = true;
				break;
			}
		}
		if (!foundGene) {
			return ResponseEntity.badRequest().header("reason", "Could not find any of the provided genes. Cannot calculate overview network.").build();
		}
		
		// 6. Get the FlatEdges making up the context
		List<FlatEdge> contextFlatEdges = this.contextService.getNetworkContextFlatEdges(
											  networkEntityUUID
											, geneNames 
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getMinSize()
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getMaxSize()
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().isTerminateAtDrug()
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getDirection());
		if(contextFlatEdges == null) {
			return ResponseEntity.badRequest().header("reason", "Could not get network context").build();
		}
		log.info("Created overview network species and relationships");
		Map<String, Object> nodeAnnotation = new HashMap<>();
		for (String geneName : geneNames) {
			nodeAnnotation.put(geneName, true);
		}
		// 7. Add the annotation inline
		this.networkService.addNodeAnnotation(contextFlatEdges, overviewNetworkItem.getAnnotationName(), "boolean", nodeAnnotation);
		log.info("Added annotation " + overviewNetworkItem.getAnnotationName() + " to network");
		// 8. Create a networkName for the new network file
		String networkName = null;
		if (overviewNetworkItem.getNetworkName() != null && !overviewNetworkItem.getNetworkName().equals("")) {
			networkName = overviewNetworkItem.getNetworkName();
		} else {
			StringBuilder networkNameSB = new StringBuilder();
			networkNameSB.append("Overview-network");
			for (String gene : geneNames) {
				networkNameSB.append("_");
				networkNameSB.append(gene);
			}
			networkNameSB.append("_IN_");
			networkNameSB.append(networkEntityUUID);
			networkName = networkNameSB.toString();
		}
		// 9. Create a new mapping with the FlatEdges
		MappingNode overviewNetwork = this.networkService.createMappingFromFlatEdges(graphAgent, networkEntityUUID, contextFlatEdges, networkName);
		log.info("Created MappingNode for network");
		// 10. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(overviewNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	@Override
	public ResponseEntity<Resource> getOverviewNetwork(String user, String name) {
		log.info("Serving GET /overview for user " + user + " with network name: " + name);
		
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. Does the network exist?
		MappingNode overviewNetwork = this.mappingNodeService.findByNetworkNameAndUser(name, user);
		if (overviewNetwork == null) {
			log.info("GET /overview: Network for user " + user + " with network name: " + name + " could not be found") ;
			// If the network does not exist at all, return 204 still in creation
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.header("reason", "Network with name " + name + " for user " + user + " could not be created", "reason", "Network with name " + name + " for user " + user + " could not be created")
					.build();
		}
		// 2. Is the network active?
		if (!overviewNetwork.isActive()) {
			// 2.a No, return 204
			log.info("GET /overview: Network for user " + user + " with network name: " + name + " is not ready yet") ;
			return ResponseEntity.notFound().header("reason", "Network is not ready yet").build();
		} else {
			// 2.b Yes, return network
			log.info("GET /overview: Network for user " + user + " with network name: " + name + " is available, returning GraphML") ;
			Resource networkResource = this.networkResourceService.getNetwork(overviewNetwork.getEntityUUID(), true);
			return ResponseEntity.ok(networkResource);
		}
	}
}
