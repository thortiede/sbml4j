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
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.PathwaysApi;
import org.tts.model.api.PathwayCollectionCreationItem;
import org.tts.model.api.PathwayInventoryItem;
import org.tts.model.api.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.service.NetworkMappingService;
import org.tts.service.PathwayService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.WarehouseGraphService;
import org.tts.service.warehouse.DatabaseNodeService;
import org.tts.service.warehouse.PathwayCollectionNodeService;

/**
 * Controller for handling Pathway requests
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class PathwaysApiController implements PathwaysApi {
	
	@Autowired
	PathwayCollectionNodeService pathwayCollectionNodeService;

	@Autowired
	DatabaseNodeService databaseNodeService;
	
	@Autowired
	NetworkMappingService networkMappingService;
	
	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	
	Logger log = LoggerFactory.getLogger(PathwaysApiController.class);
	
	/**
	 * Create a <a href="#{@link}">{@link PathayNode}</a>  as a collection from the submitted pathways
	 * 
	 * @param body The <a href="#{@link}">{@link PathayCollectionCreationItem}</a> to create the collection with
	 * @param user The user to associate with the created pathway
	 * @return The UUID of the created Pathway
	 */
	@Override
	public ResponseEntity<UUID> createPathwayCollection(@Valid PathwayCollectionCreationItem body, String user) {
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", user);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(agentNodeProperties);

		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createKnowledgeGraph);
		String activityName = "Create_KnowledgeGraph_for_" + body.getDatabaseUUID().toString();
		activityNodeProvenanceProperties.put("graphactivityname", activityName);

		ProvenanceGraphActivityNode createKnowledgeGraphActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, userAgentNode,
				ProvenanceGraphEdgeType.wasAssociatedWith);

		// get databaseNode
		DatabaseNode database = this.databaseNodeService
				.findByEntityUUID(body.getDatabaseUUID().toString());
		this.provenanceGraphService.connect(createKnowledgeGraphActivityNode, database, ProvenanceGraphEdgeType.used);

		// create a pathwayCollectionNode
		PathwayCollectionNode pathwayCollectionNode = this.pathwayCollectionNodeService.createPathwayCollection(
				body, database, createKnowledgeGraphActivityNode, userAgentNode);

		// then create the pathway to the collection by iterating over all pathways in
		// the collection and adding them to the pathway via CONTAINS
		PathwayNode pathwayNode = this.pathwayService.createPathwayNode(
				"CollectionPathway_of_" + pathwayCollectionNode.getEntityUUID(),
				"CollectionPathway_of_" + pathwayCollectionNode.getPathwayCollectionName(), database.getOrganism());
		this.provenanceGraphService.connect(pathwayNode, pathwayCollectionNode, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(pathwayNode, createKnowledgeGraphActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy);
		this.provenanceGraphService.connect(pathwayNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);

		pathwayNode = this.pathwayCollectionNodeService.buildPathwayFromCollection(
				pathwayNode, pathwayCollectionNode, createKnowledgeGraphActivityNode, userAgentNode);
		return new ResponseEntity<UUID>(UUID.fromString(pathwayNode.getEntityUUID()), HttpStatus.CREATED);
   }
   
	/**
	 * Get a list of <a href="#{@link}">{@link PathwayInventoryItem}</a>  for all Pathways associated to a user
	 * 
	 * @param user The user for which the pathways are to be fetched
	 * @param hideCollections Hide collection pathways from the output (true), or include them (false)
	 * @return List of <a href="#{@link}">{@link PathwayInventoryItem}</a> of the pathways for user user
	 */
   @Override
	public ResponseEntity<List<PathwayInventoryItem>> listAllPathways(String user, @Valid Boolean hideCollections) {
	   log.debug("Serving PathwayInventory for user " + user);
		return new ResponseEntity<List<PathwayInventoryItem>>(
				this.pathwayService.getListofPathwayInventory(user, hideCollections), HttpStatus.OK);
	}
   
   /**
    * Get a list of UUIDs of Pathways for user user
    * 
    * @param user The user for which the pathwayUUIDs are to be fetched
    * @param hideCollections Hide collection pathways from the output (true), or include them (false)
    * @Return A List of UUIDs of <a href="#{@link}">{@link PathwayNode}</a> 
    */
   @Override
	public ResponseEntity<List<UUID>> listAllPathwayUUIDs(String user, @Valid Boolean hideCollections) {
	   return new ResponseEntity<List<UUID>>(this.pathwayService.getListofPathwayUUIDs(user, hideCollections), HttpStatus.OK);
	}
   
   
   
   /**
    * Endpoint for mapping a Pathway to a <a href="#{@link}">{@link MappingNode}</a>
    * @param user The user which requests this mapping and is associated with it
    * @param uuid The UUID of the <a href="#{@link}">{@link PathwayNpde}</a> to derive the mapping from
    * @param mappingType The <a href="#{@link}">{@link NetworkMappingType}</a> for thw new NetworkMapping
    * @return The <a href="#{@link}">{@link WarehouseInventoryItem}</a> of the created Mapping
    */
   @Override
	public ResponseEntity<WarehouseInventoryItem> mapPathway(String user, UUID uuid,
			@NotNull @Valid String mappingType) {
	// Need Agent
			Map<String, Object> agentNodeProperties = new HashMap<>();
			agentNodeProperties.put("graphagentname", user);
			agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
			ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
					.createProvenanceGraphAgentNode(agentNodeProperties);

			// Need Activity
			Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
			activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.createMapping);
			String mappingName = "Pathway_" + uuid.toString() + "_to_" + mappingType;
			activityNodeProvenanceProperties.put("graphactivityname", mappingName);
			// activityNodeProvenanceProperties.put("createdate", createDate);

			ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService
					.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
			this.provenanceGraphService.connect(createMappingActivityNode, userAgentNode,
					ProvenanceGraphEdgeType.wasAssociatedWith);

			// need the pathwayNode
			PathwayNode pathway = this.pathwayService.findByEntityUUID(uuid.toString());
			if (pathway == null) {
				return ResponseEntity.badRequest().header("reason", "UUID did not denote a valid pathway, or pathway could not be accessed by user").build();
			}
			this.provenanceGraphService.connect(createMappingActivityNode, pathway, ProvenanceGraphEdgeType.used);
			// create the mappingNode
			MappingNode mappingNode;
			try {
				mappingNode = this.networkMappingService.createMappingFromPathway(pathway,
						NetworkMappingType.valueOf(mappingType), createMappingActivityNode,
						userAgentNode);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage());
				e.printStackTrace();
				return ResponseEntity.badRequest().header("reason", "Failed to create Mapping from pathway").build();
			}

			// build the inventoryItem
			WarehouseInventoryItem mappingInventoryItem = this.warehouseGraphService.getWarehouseInventoryItem(mappingNode);

			// return the inventoryItem
			return new ResponseEntity<WarehouseInventoryItem>(mappingInventoryItem, HttpStatus.CREATED);
	}

}
