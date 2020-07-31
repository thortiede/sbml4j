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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.VcfApi;
import org.tts.config.VcfConfig;
import org.tts.model.api.Drivergenes;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.service.WarehouseGraphService;

/**
 * Controller for handling of VCF analysis tasks
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class VcfApiController implements VcfApi {

	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	VcfConfig vcfConfig;
	
	/**
	 * Create a network context for the genes in Drivergenes and annotate the given nodes with "Drivergene"
	 */
	@Override
	public ResponseEntity<NetworkInventoryItem> createOverviewNetwork(@Valid Drivergenes drivergenes, String user) {
		if (drivergenes.getGenes() == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		
		ResponseEntity<String> contextMappingResponse = this.warehouseGraphService.postContext(user
				, (drivergenes.getBaseNetworkUUID() != null) ? drivergenes.getBaseNetworkUUID().toString() : this.vcfConfig.getVcfDefaultProperties().getBaseNetworkUUID()
				, drivergenes.getGenes()
				, this.vcfConfig.getVcfDefaultProperties().getMaxSize()
				, this.vcfConfig.getVcfDefaultProperties().getMaxSize()
				, this.vcfConfig.getVcfDefaultProperties().isTerminateAtDrug()
				, this.vcfConfig.getVcfDefaultProperties().getDirection()
				, user + "_" + drivergenes.toSingleLineString());
		
		
		if(contextMappingResponse.getStatusCode().is2xxSuccessful()) {
			// add inlineAnnotation
			String networkEntityUUID = this.warehouseGraphService.addAnnotationToNetwork(contextMappingResponse.getBody(), drivergenes.getGenes());
			return new ResponseEntity<NetworkInventoryItem>(this.warehouseGraphService.getNetworkInventoryItem(networkEntityUUID), HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}
