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
package org.sbml4j.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.sbml4j.Exception.NetworkDeletionException;
import org.sbml4j.Exception.UserUnauthorizedException;
import org.sbml4j.api.OverviewApi;
import org.sbml4j.config.OverviewNetworkConfig;
import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.api.NetworkInventoryItem;
import org.sbml4j.model.api.OverviewNetworkItem;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.ContextService;
import org.sbml4j.service.ProvenanceGraphService;
import org.sbml4j.service.networks.NetworkResourceService;
import org.sbml4j.service.networks.NetworkService;
import org.sbml4j.service.warehouse.MappingNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

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
	ConfigService configService;
	
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
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	/**
	 * Create a network context for the genes in OverviewNetworkItem and annotate the given nodes with the given annotation
	 */
	@Override
	public ResponseEntity<NetworkInventoryItem> createOverviewNetwork(@Valid OverviewNetworkItem overviewNetworkItem, String user) {
		
		log.info("Serving POST /overview" +  (user != null ? " for user " + user : "") + " with overviewNetworkItem: " + overviewNetworkItem.toString());
		
		// 1a. BaseNetworkUUID provided?
		String networkEntityUUID;
		MappingNode parentMapping = null;
		if (overviewNetworkItem.getBaseNetworkUUID() == null) {
			String defaultBaseNetworkName = this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getBaseNetworkName();
			if (defaultBaseNetworkName == null) {
				return ResponseEntity.badRequest().header("reason", "No baseNetworkEntityUUID provided in body and no default network configured. Unable to generate context without one or the other.").build();
			}
			parentMapping = this.mappingNodeService.findByNetworkNameAndUser(defaultBaseNetworkName, this.configService.getPublicUser());
			if (parentMapping == null) {
				if (user != null) {
					parentMapping = this.mappingNodeService.findByNetworkNameAndUser(defaultBaseNetworkName, user);
					if (parentMapping == null) {
						return ResponseEntity.badRequest().header("reason", "Could not find the default base network provided in the configuration with name " + defaultBaseNetworkName + ". Tried with public user and provided user: " + this.configService.getPublicUser()).build();
					}
				} else {
					return ResponseEntity.badRequest().header("reason", "Could not find the default base network provided in the configuration with name " + defaultBaseNetworkName + ".").build();
				}
			}
			networkEntityUUID = parentMapping.getEntityUUID();
			log.debug("Using baseNetwork given by properties with name: " + defaultBaseNetworkName);
		} else {
			networkEntityUUID = overviewNetworkItem.getBaseNetworkUUID().toString();
			log.debug("Using baseNetwork given in input: " + networkEntityUUID);
			parentMapping = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
			if(parentMapping == null) {
				return ResponseEntity.badRequest().header("reason", "Could not find the base network provided via the baseNetworkUUID.").build();
			}
		}
		// 2b. AnnotationName provided?
		if (overviewNetworkItem.getAnnotationName() == null || overviewNetworkItem.getAnnotationName().equals("")) {
			return ResponseEntity.badRequest().header("reason", "Annotation name not provided in request body in the annotationName - field.").build();
		}
		
		// 3. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(networkEntityUUID, user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		user = user != null ? user.strip() : networkUser;
		
		// 4a. Get the gene names
		List<String> geneNames = overviewNetworkItem.getGenes();
		// 4b. Any genes provided?
		if (geneNames == null || geneNames.isEmpty()) {
			return ResponseEntity.badRequest().header("reason", "Genes not provided in body").build();
		}
		// 5. Get the networkName
		String networkName = overviewNetworkItem.getNetworkName();
		if (overviewNetworkItem.getNetworkName() == null || overviewNetworkItem.getNetworkName().isBlank()) {
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
		
		// 6. Check if a network with this name already exists for that user (as it has to be unique and we need to check that here)
		MappingNode existingNetwork = this.mappingNodeService.findByNetworkNameAndUser(networkName, user);
		if (existingNetwork != null) {
			log.info("Found existing network with name " + networkName + " for user " + user + ". Deleting existing network.");
			// delete the network before continuing
			boolean isDeleted = false;
			if (sbml4jConfig.getNetworkConfigProperties().isHardDelete()) {
				try {
					isDeleted = this.networkService.deleteNetwork(existingNetwork.getEntityUUID());
				} catch (NetworkDeletionException e) {
					return ResponseEntity.badRequest().header("reason", "Found existing network with name " + networkName + " but failed to delete it. Additonal Info: " + e.getMessage()).build();
				}
			} else {
				isDeleted = this.networkService.deactivateNetwork(existingNetwork.getEntityUUID());
			}
			if (!isDeleted) {
				return ResponseEntity.badRequest().header("reason", "Found existing network with name " + networkName + " but failed to delete it.").build();
			}
		}
		
		// 7. Create the user if not existent
		ProvenanceGraphAgentNode graphAgent = this.provenanceGraphService.createProvenanceGraphAgentNode(user, ProvenanceGraphAgentType.User);
		
		// 8. Check whether we know at least one of the genes provided and get the entityUUIDs of the FlatSpecies
		boolean foundGene = false;
		List<FlatSpecies> geneSpecies=new ArrayList<>();
		for (String gene : geneNames) {
			List<FlatSpecies> foundGeneEntities = this.networkService.getFlatSpeciesOfSymbolInNetwork(networkEntityUUID, gene);
			if (foundGeneEntities != null
					&& !foundGeneEntities.isEmpty()) {
				foundGene = true;
				geneSpecies.addAll(foundGeneEntities);
			}
		}
		if (!foundGene) {
			return ResponseEntity.badRequest().header("reason", "Could not find any of the provided genes. Cannot calculate overview network.").build();
		}
		
		// 9. Create the MappingNode to use
		MappingNode overviewNetwork = this.networkService.createMappingPre(graphAgent, parentMapping, networkName, "Create_" + networkName, ProvenanceGraphActivityType.createContext, parentMapping.getMappingType());
		
		// 10. Get the FlatEdges making up the context
		List<FlatEdge> contextFlatEdges = this.contextService.getNetworkContextFlatEdges(
											  networkEntityUUID
											, geneNames 
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getMinSize()
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getMaxSize()
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getTerminateAt()
											, this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getDirection()
											, overviewNetworkItem.getEdgeweightproperty());
		if(contextFlatEdges == null) {
			try {
				this.networkService.deleteNetwork(overviewNetwork.getEntityUUID());
			} catch (NetworkDeletionException e) {
				return ResponseEntity.badRequest().header("reason", "Could not get network context. Attempted to delete newly created Network, but failed. The UUID is: " + overviewNetwork.getEntityUUID() + ". Additional info: " + e.getMessage()).build();
			}
			return ResponseEntity.badRequest().header("reason", "Could not get network context").build();
		}
		log.info("Created overview network species and relationships of network " + overviewNetwork.getMappingName());
		Map<String, Object> nodeAnnotation = new HashMap<>();

		for (FlatSpecies fs : geneSpecies) {
			nodeAnnotation.put(fs.getSymbol(), true);
		}
		// 11. Add the annotation inline
		this.networkService.addNodeAnnotation(contextFlatEdges, overviewNetworkItem.getAnnotationName(), "boolean", nodeAnnotation);
		log.info("Added annotation " + overviewNetworkItem.getAnnotationName() + " to network" + overviewNetwork.getMappingName());

		
		// 12. Connect the FlatEdges to the mappingNode
		overviewNetwork = this.networkService.createMappingFromFlatEdges(graphAgent, networkEntityUUID, contextFlatEdges, overviewNetwork);
		log.info("Created network " + overviewNetwork.getMappingName() + ". Returning.");
		// 10. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(overviewNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
	
	@Override
	public ResponseEntity<Resource> getOverviewNetwork(String user, String name) {
		log.info("Serving GET /overview" + (user != null ? " for user " + user : "") + " with network name: " + name);
		
		// 1. Does the network exist?
		MappingNode overviewNetwork = this.mappingNodeService.findByNetworkNameAndUser(name, user);
		if (overviewNetwork == null) {
			log.info("GET /overview: Network for user " + user + " with network name: " + name + " could not be found") ;
			// If the network does not exist at all, return 204 still in creation
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
					.header("reason", "Network with name " + name + " for user " + user + " could not be found")
					.build();
		}
		
		// 2. Is the given user or the public user authorized for this network?
		String networkUser = null;
		try {
			networkUser = this.configService.getUserNameAttributedToNetwork(overviewNetwork.getEntityUUID(), user);
		} catch (UserUnauthorizedException e) {
			return ResponseEntity.badRequest()
					.header("reason", e.getMessage())
					.build();
		}
		// 2. Is the network active?
		if (!overviewNetwork.isActive()) {
			// 2.a No, return 204
			log.info("GET /overview: Network for user " + networkUser + " with network name: " + name + " is not ready yet. Try again later.") ;
			return ResponseEntity.notFound().header("reason", "Network is not ready yet. Try again later.").build();
		} else {
			// 2.b Yes, return network
			log.info("GET /overview: Network for user " + user + " with network name: " + name + " is available, returning GraphML") ;
			Resource networkResource = this.networkResourceService.getNetwork(overviewNetwork.getEntityUUID(), true);
			return ResponseEntity.ok(networkResource);
		}
	}
}
