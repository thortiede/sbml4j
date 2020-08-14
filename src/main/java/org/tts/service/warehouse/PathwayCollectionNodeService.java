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
package org.tts.service.warehouse;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.PathwayCollectionCreationItem;
import org.tts.model.common.Organism;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.repository.warehouse.PathwayCollectionNodeRepository;
import org.tts.service.GraphBaseEntityService;
import org.tts.service.PathwayService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.WarehouseGraphService;

/**
 * Service for handling creation and querying of <a href="#{@link}">{@link PathwayCollectionNode}</a> entities
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class PathwayCollectionNodeService {
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	PathwayCollectionNodeRepository pathwayCollectionNodeRepository;
	
	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	private final Logger logger = LoggerFactory.getLogger(PathwayCollectionNodeService.class);
	
	/**
	 * Create a <a href="#{@link}">{@link PathwayCollectionNode}</a> and connect it to the member <a href="#{@link}">{@link PathwayNode}</a> entities
	 * @param pathwayCollectionCreationItem The <a href="#{@link}">{@link pathwayCollectionCreationItem}</a> that defines the collection
	 * @param databaseNode The <a href="#{@link}">{@link DatabaseNode}</a> for the pathways
	 * @param createPathwayCollectionGraphActivityNode The <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> which creates this collection
	 * @param agentNode The <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a> which creates this collection
	 * @return Created, persisted and connected <a href="#{@link}">{@link PathwayCollectionNode}</a>
	 */
	public PathwayCollectionNode createPathwayCollection(PathwayCollectionCreationItem pathwayCollectionCreationItem,
			DatabaseNode databaseNode, ProvenanceGraphActivityNode createPathwayCollectionGraphActivityNode,
			ProvenanceGraphAgentNode agentNode) {
		
		PathwayCollectionNode pathwayCollectionNode = this.createPathwayCollectionNode(
				pathwayCollectionCreationItem.getName(), pathwayCollectionCreationItem.getDescription(),
				databaseNode.getOrganism());

		// connect provenance and to database node
		this.provenanceGraphService.connect(pathwayCollectionNode, databaseNode,
				ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(pathwayCollectionNode, createPathwayCollectionGraphActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy);
		this.provenanceGraphService.connect(pathwayCollectionNode, agentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		// take all listed pathways
		for (UUID pathwayEntityUUID : pathwayCollectionCreationItem.getSourcePathwayUUIDs()) {
			// add entities of pathway to knowledgegraphNode
			PathwayNode pathway = this.pathwayService.findByEntityUUID(pathwayEntityUUID.toString());
			if (pathway != null) {// && findSource(pathway).getEntityUUID() == databaseNode.getEntityUUID()) {
				this.provenanceGraphService.connect(pathwayCollectionNode, pathway, ProvenanceGraphEdgeType.hadMember);
				this.provenanceGraphService.connect(createPathwayCollectionGraphActivityNode, pathway,
						ProvenanceGraphEdgeType.used);
			}
		}
		return pathwayCollectionNode;
	}

	/**
	 * Create the actual <a href="#{@link}">{@link PathwayCollectionNode}</a> without connecting it
	 * @param pathwayCollectionName The name of the collection
	 * @param pathwayCollectionDescription The description of the collection
	 * @param organism The <a href="#{@link}">{@link Organism}</a> that this collection is for
	 * @return Created and persisted <a href="#{@link}">{@link PathwayCollectionNode}</a>
	 */
	public PathwayCollectionNode createPathwayCollectionNode(String pathwayCollectionName,
			String pathwayCollectionDescription, Organism organism) {
		for (PathwayCollectionNode pwc : this.pathwayCollectionNodeRepository
				.findAllByPathwayCollectionName(pathwayCollectionName)) {
			if (pwc.getOrganism().equals(organism)
					&& pwc.getPathwayCollectionDescription().equals(pathwayCollectionDescription)) {
				return pwc;
			}
		}
		PathwayCollectionNode pathwayCollectionNode = new PathwayCollectionNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(pathwayCollectionNode);
		pathwayCollectionNode.setPathwayCollectionName(pathwayCollectionName);
		pathwayCollectionNode.setPathwayCollectionDescription(pathwayCollectionDescription);
		pathwayCollectionNode.setOrganism(organism);
		return this.pathwayCollectionNodeRepository.save(pathwayCollectionNode);

	}

	/**
	 * Connects all contained pathwayelements of <a href="#{@link}">{@link PathwayNode}</a> entities in the <a href="#{@link}">{@link PathwayCollectionNode}</a> to the given <a href="#{@link}">{@link PathwayNode}</a>
	 * @param pathwayNode The <a href="#{@link}">{@link PathwayNode}</a> to connect the entities to
	 * @param pathwayCollectionNode The <a href="#{@link}">{@link PathwayCollectionNode}</a> that has the <a href="#{@link}">{@link PathwayNode}</a> entities as Members to take the entites from
	 * @param buildPathwayFromCollectionActivityNode The <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> which creates the pathway from the collection
	 * @param agentNode The <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a> which creates the pathway from the collection
	 * @return The connected <a href="#{@link}">{@link PathwayNode}</a>
	 */ 
	public PathwayNode buildPathwayFromCollection(PathwayNode pathwayNode,
			PathwayCollectionNode pathwayCollectionNode,
			ProvenanceGraphActivityNode buildPathwayFromCollectionActivityNode, ProvenanceGraphAgentNode agentNode) {

		int entityCounter = 0;
		int connectionCounter = 0;
		if (this.provenanceGraphService.areProvenanceEntitiesConnectedWithProvenanceEdgeType(
				pathwayNode.getEntityUUID(), pathwayCollectionNode.getEntityUUID(),
				ProvenanceGraphEdgeType.wasDerivedFrom)) {
			for (ProvenanceEntity pathwayEntity : this.provenanceGraphService
					.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.hadMember,
							pathwayCollectionNode.getEntityUUID())) {
				// get all element in the pathway (Connected with CONTAINS)
				for (ProvenanceEntity entity : this.warehouseGraphService
						.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.CONTAINS,
								pathwayEntity.getEntityUUID())) {
					// connect them to the new pathwayNode
					if (this.warehouseGraphService.connect(pathwayNode, entity, WarehouseGraphEdgeType.CONTAINS)) {
						++connectionCounter;
					}
					++entityCounter;
					// Possibility to connect the activity to all entities here, but might just
					// create ALOT of edges that do not help anyone.
					// might become interesting, if we introduce filtering on nodes here, aka. not
					// build the full pathway, but just a subset of the pathways
					// that the collection holds
					// but that better be done from the pathway that is created here in a second
					// step with a new activity.
				}
			}
		}
		logger.info("Created pathwayCollection with " + String.valueOf(entityCounter) + " entities and " + String.valueOf(connectionCounter) + " connections");
		return pathwayNode;

	}


}
