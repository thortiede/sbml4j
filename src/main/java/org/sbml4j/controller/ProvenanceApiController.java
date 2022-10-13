/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.sbml4j.Exception.UserUnauthorizedException;
import org.sbml4j.api.ProvenanceApi;
import org.sbml4j.model.api.provenance.ProvenanceInfoItem;
import org.sbml4j.model.base.GraphBaseEntity;
import org.sbml4j.model.base.GraphEnum.Operation;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.model.warehouse.PathwayNode;
import org.sbml4j.model.warehouse.WarehouseGraphNode;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.ProvenanceGraphService;
import org.sbml4j.service.WarehouseGraphService;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * Controller class for all things relating provenance
 * 
 * @author Thorsten Tiede
 *
 * @since 1.3
 */
@Controller
public class ProvenanceApiController implements ProvenanceApi {
	
	Logger log = LoggerFactory.getLogger(ProvenanceApiController.class);
	
	@Autowired
	ConfigService configService;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	

	@Override
	public ResponseEntity<ProvenanceInfoItem> getProvenance(UUID UUID, String user, @Valid String format) {
		String uuid = UUID.toString();
		log.info("Serving GET /prov/" + uuid + (user != null ? " for user " + user : ""));
		// 1. Does the warehouse entity with given uuid exist, or rather is it a warehouse entity?
		
		GraphBaseEntity warehouseNodeForUUID = this.graphBaseEntityService.findByEntityUUID(uuid);
		if (warehouseNodeForUUID == null) {
			return ResponseEntity.badRequest().header("reason", "No entity found in database that has provided UUID.").build();
		}
		if (!(warehouseNodeForUUID instanceof WarehouseGraphNode)) {
			return ResponseEntity.badRequest().header("reason", "Provided UUID does not belong to an entity that provides Provenance").build();
		}
		
		if (warehouseNodeForUUID instanceof MappingNode || warehouseNodeForUUID instanceof PathwayNode) {
			// 2. Is the given user or the public usser authorized for this network?
			try {
				this.configService.getUserNameAttributedToNetwork(uuid, user);
			} catch (UserUnauthorizedException e) {
				return ResponseEntity.badRequest()
						.header("reason", e.getMessage())
						.build();
			}
		}
		// 3. Create the ProvenanceInfoItem
		//ProvenanceInfoItem item = new ProvenanceInfoItem();
		// and Fill the ProvenanceInfoItem with provenance content
		ProvenanceInfoItem item = this.provenanceGraphService.getProvenanceInfoItem(uuid);
		
		
		return ResponseEntity.ok(item);
	}

	@Override
	public ResponseEntity<ProvenanceInfoItem> putProvenance(UUID UUID, @NotNull @Valid String name,
			@Valid Map<String, Object> requestBody, String user) {
		String uuid = UUID.toString();
		Operation op = Operation.PUT;
		String endpoint = "/prov/" + uuid;
		
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : ""));
		// 1. Does the warehouse entity with given uuid exist, or rather is it a warehouse entity?
		
		GraphBaseEntity graphBaseEntity = this.graphBaseEntityService.findByEntityUUID(uuid);
		if (graphBaseEntity == null) {
			return ResponseEntity.badRequest().header("reason", "No entity found in database that has provided UUID.").build();
		}
		if (!(graphBaseEntity instanceof WarehouseGraphNode)) {
			return ResponseEntity.badRequest().header("reason", "Provided UUID does not belong to an entity that can receive ProvenanceAnnotation").build();
		}
		WarehouseGraphNode warehouseGraphNodeEntity = (WarehouseGraphNode) graphBaseEntity;
		if (graphBaseEntity instanceof MappingNode || graphBaseEntity instanceof PathwayNode) {
			// 2. Is the given user or the public usser authorized for this network?
			try {
				this.configService.getUserNameAttributedToNetwork(uuid, user);
			} catch (UserUnauthorizedException e) {
				return ResponseEntity.badRequest()
						.header("reason", e.getMessage())
						.build();
			}
		}
		Map<String, Map<String, Object>> provenanceAnnotationMap = new HashMap<>();
		provenanceAnnotationMap.put(name, requestBody);
		
		if (!this.provenanceGraphService.addProvenanceAnnotationMap(warehouseGraphNodeEntity, provenanceAnnotationMap)) {
			return ResponseEntity.badRequest().header("reason", "Unable to add provenance annotation to mapping").build(); // TODO: Give better reason
		} else {
			return ResponseEntity.ok(this.provenanceGraphService.getProvenanceInfoItem(uuid));
		}
		
	}
	
	
	
	
	
	
	
	
	
}
