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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.OverviewApi;
import org.tts.config.OverviewNetworkConfig;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.OverviewNetworkItem;
import org.tts.model.flat.FlatEdge;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.ContextService;
import org.tts.service.ProvenanceGraphService;
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

	@Autowired
	ContextService contextService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	OverviewNetworkConfig overviewNetworkConfig;
	
	/**
	 * Create a network context for the genes in OverviewNetworkItem and annotate the given nodes with the given annotation
	 */
	@Override
	public ResponseEntity<NetworkInventoryItem> createOverviewNetwork(@Valid OverviewNetworkItem overviewNetworkItem, String user) {
		
		// 1. BaseNetworkUUID provided?
		String networkEntityUUID;
		if (overviewNetworkItem.getBaseNetworkUUID() == null) {
			networkEntityUUID = this.overviewNetworkConfig.getOverviewNetworkDefaultProperties().getBaseNetworkUUID();
		} else {
			networkEntityUUID = overviewNetworkItem.getBaseNetworkUUID().toString();
		}
		// 2. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(networkEntityUUID) == null) {
			return ResponseEntity.notFound().build();
		}
		// 3. We skip the check whether the user can access the baseNetwork for now. 
		// For this scenario, the user can take any network and create a driver gene network from it
		List<String> geneNames = overviewNetworkItem.getGenes();
		if (geneNames == null) {
			return ResponseEntity.badRequest().header("reason", "Genes not provided in body").build();
		}
		// 4. Get the FlatEdges making up the context
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
		Map<String, Object> nodeAnnotation = new HashMap<>();
		for (String geneName : geneNames) {
			nodeAnnotation.put(geneName, true);
		}
		// 5. Add the annotation inline
		this.networkService.addInlineNodeAnnotation(contextFlatEdges, overviewNetworkItem.getAnnotationName(), "boolean", nodeAnnotation);
		// 6. Create a networkName for the new network file
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
			// 7. Create a new mapping with the FlatEdges
		MappingNode overviewNetwork = this.networkService.createMappingFromFlatEdges(user, networkEntityUUID, contextFlatEdges, networkName);
		// 8. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(overviewNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
}
