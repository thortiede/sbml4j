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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.VcfApi;
import org.tts.config.VcfConfig;
import org.tts.model.api.Drivergenes;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
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
public class VcfApiController implements VcfApi {

	@Autowired
	ContextService contextService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	VcfConfig vcfConfig;
	
	/**
	 * Create a network context for the genes in Drivergenes and annotate the given nodes with "Drivergene"
	 */
	@Override
	public ResponseEntity<NetworkInventoryItem> createOverviewNetwork(@Valid Drivergenes drivergenes, String user) {
		// 0. Does user exist?
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) {
			return ResponseEntity.badRequest().header("reason", "User " + user + " does not exist").build();
		}
		// 1. BaseNetworkUUID provided?
		String networkEntityUUID = drivergenes.getBaseNetworkUUID().toString();
		if (networkEntityUUID == null) {
			networkEntityUUID = this.vcfConfig.getVcfDefaultProperties().getBaseNetworkUUID();
		}
		// 2. Does the network exist?
		if (this.mappingNodeService.findByEntityUUID(networkEntityUUID) == null) {
			return ResponseEntity.notFound().build();
		}
		// 3. We skip the check whether the user can access the baseNetwork for now. 
		// For this scenario, the user can take any network and create a driver gene network from it
		List<String> geneNames = drivergenes.getGenes();
		if (geneNames == null) {
			return ResponseEntity.badRequest().header("reason", "Genes not provided in body").build();
		}
		// 4. Get the FlatEdges making up the context
		List<FlatEdge> contextFlatEdges = this.contextService.getNetworkContextFlatEdges(
											  networkEntityUUID
											, geneNames 
											, this.vcfConfig.getVcfDefaultProperties().getMinSize()
											, this.vcfConfig.getVcfDefaultProperties().getMaxSize()
											, this.vcfConfig.getVcfDefaultProperties().isTerminateAtDrug()
											, this.vcfConfig.getVcfDefaultProperties().getDirection());
		if(contextFlatEdges == null) {
			return ResponseEntity.badRequest().header("reason", "Could not get network context").build();
		}
		Map<String, Object> nodeAnnotation = new HashMap<>();
		for (String geneName : geneNames) {
			nodeAnnotation.put(geneName, true);
		}
		// 5. Add the drivergen annotation inline
		this.networkService.addInlineNodeAnnotation(contextFlatEdges, "drivergene", "boolean", nodeAnnotation);
		// 6. Create a networkName for the new network file
		StringBuilder networkName = new StringBuilder();
		networkName.append("drivergene-network");
		for (String gene : geneNames) {
			networkName.append("_");
			networkName.append(gene);
		}
		networkName.append("_IN_");
		networkName.append(networkEntityUUID);
		// 7. Create a new mapping with the FlatEdges
		MappingNode drivergeneNetwork = this.networkService.createMappingFromFlatEdges(user, networkEntityUUID, contextFlatEdges, networkName.toString());
		// 8. Return the InventoryItem of the new Network
		return new ResponseEntity<NetworkInventoryItem>(this.networkService.getNetworkInventoryItem(drivergeneNetwork.getEntityUUID()), HttpStatus.CREATED);
	}
}
