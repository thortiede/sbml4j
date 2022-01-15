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
package org.sbml4j.service;

import java.util.List;

import org.sbml4j.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.provenance.ProvenanceGraphEdge;
import org.sbml4j.model.warehouse.WarehouseGraphEdge;
import org.sbml4j.model.warehouse.WarehouseGraphNode;
import org.sbml4j.repository.warehouse.WarehouseGraphEdgeRepository;
import org.sbml4j.repository.warehouse.WarehouseGraphNodeRepository;
import org.sbml4j.service.warehouse.OrganismService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling Warehouse specific tasks
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class WarehouseGraphService {

	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	OrganismService organismService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	WarehouseGraphEdgeRepository warehouseGraphEdgeRepository;
	
	@Autowired
	WarehouseGraphNodeRepository warehouseGraphNodeRepository;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
/************************************************************* Repository Methods *************************************************/		
	
	/**
	 * Save a <a href="#{@link}">{@link WarehouseGraphNode}</a>
	 * @param node The <a href="#{@link}">{@link WarehouseGraphNode}</a> to save
	 * @param depth The depth to use
	 * @return The persisted <a href="#{@link}">{@link WarehouseGraphNode}</a>
	 */
	public WarehouseGraphNode saveWarehouseGraphNodeEntity(WarehouseGraphNode node, int depth) {
		return this.warehouseGraphNodeRepository.save(node, depth);
	}
	
	/**
	 * Find the List of <a href="#{@link}">{@link ProvenanceEntity}</a> that are connected to the given startnode with the given relation type
	 * @param type The <a href="#{@link}">{@link WarehouseGraphEdgeType}</a>
	 * @param startNodeEntityUUID The entityUUID of the startNode of the <a href="#{@link}">{@link WarehouseGraphEdge}</a>
	 * @return List of <a href="#{@link}">{@link ProvenanceEntity}</a> that are connected to startNode by type
	 */
	public List<ProvenanceEntity> findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType type, String startNodeEntityUUID) {
		return this.warehouseGraphNodeRepository.findAllByWarehouseGraphEdgeTypeAndStartNode(type, startNodeEntityUUID);
	}
	
	/**
	 * Find the List of <a href="#{@link}">{@link ProvenanceEntity}</a> that are connected to the given endNode with the given relation type
	 * @param type The <a href="#{@link}">{@link WarehouseGraphEdgeType}</a>
	 * @param endNodeEntityUUID The entityUUID of the endNode of the <a href="#{@link}">{@link WarehouseGraphEdge}</a>
	 * @return List of <a href="#{@link}">{@link ProvenanceEntity}</a> that are connected to endNode by type
	 */
	public List<ProvenanceEntity> findAllByWarehouseGraphEdgeTypeAndEndNode(WarehouseGraphEdgeType type, String endNodeEntityUUID) {
		return this.warehouseGraphNodeRepository.findAllByWarehouseGraphEdgeTypeAndStartNode(type, endNodeEntityUUID);
	}
	
	
/************************************************************* Connect Warehouse Entities ************************************************/	

	/**
	 * connect two warehouse entities. Always leaves the two entities connected with
	 * that edgetype, whether they already have been connected or not
	 * 
	 * @param source   the startNode of the edge
	 * @param targetUUID   the UUID of the endNode of the edge
	 * @param edgetype the WarehouseGraphEdgeType
	 * @return true if a new connection is made, false if the entities were already
	 *         connected with that edgetype
	 */
	
	public boolean connect(ProvenanceEntity source, String targetUUID, WarehouseGraphEdgeType edgetype) {
		return this.connect(source, this.provenanceGraphService.getByEntityUUID(targetUUID), edgetype);
	}

	/**
	 * connect two warehouse entities. Always leaves the two entities connected with
	 * that edgetype, whether they already have been connected or not
	 * 
	 * @param sourceUUID  the UUID of the startNode of the edge
	 * @param target   the endNode of the edge
	 * @param edgetype the WarehouseGraphEdgeType
	 * @return true if a new connection is made, false if the entities were already
	 *         connected with that edgetype
	 */
	
	public boolean connect(String sourceUUID, ProvenanceEntity target, WarehouseGraphEdgeType edgetype) {
		return this.connect(this.provenanceGraphService.getByEntityUUID(sourceUUID), target, edgetype);
	}
	
	
	/**
	 * connect two warehouse entities. Always leaves the two entities connected with
	 * that edgetype, whether they already have been connected or not
	 * 
	 * @param source   the startNode of the edge
	 * @param target   the endNode of the edge
	 * @param edgetype the WarehouseGraphEdgeType
	 * @return true if a new connection is made, false if the entities were already
	 *         connected with that edgetype
	 */
	
	public boolean connect(ProvenanceEntity source, ProvenanceEntity target, WarehouseGraphEdgeType edgetype) {
		return this.connect(source, target, edgetype, true);
	}
	
	/**
	 * connect two warehouse entities. Always leaves the two entities connected with
	 * that edgetype, whether they already have been connected or not
	 * 
	 * @param source   the startNode of the edge
	 * @param target   the endNode of the edge
	 * @param edgetype the WarehouseGraphEdgeType
	 * @param doCheck  check if the two entities are already connected (yes), or skip the check (false)
	 * @return true if a new connection is made, false if the entities were already
	 *         connected with that edgetype
	 */
	
	public boolean connect(ProvenanceEntity source, ProvenanceEntity target, WarehouseGraphEdgeType edgetype, boolean doCheck) {
		if (doCheck && this.warehouseGraphNodeRepository.areWarehouseEntitiesConnectedWithWarehouseGraphEdgeType(
				source.getEntityUUID(), target.getEntityUUID(), edgetype)) {
			return false;
		}

		WarehouseGraphEdge newEdge = new WarehouseGraphEdge();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newEdge);
		newEdge.setWarehouseGraphEdgeType(edgetype);
		newEdge.setStartNode(source);
		newEdge.setEndNode(target);
		logger.debug("Connecting " +source.getEntityUUID() + " and " + target.getEntityUUID() + " with " + edgetype.toString());
		this.warehouseGraphEdgeRepository.save(newEdge, 0);
		return true;

	}
	
	/**
	 * connect a set of source provenance entities with one target entity. The entities are not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param sourceEntities the startNodes of the edges
	 * @param target the endNode of the edge
	 * @param edgetype the warehouseGraphEdgeType
	 */
	
	public void connect (Iterable<ProvenanceEntity> sourceEntities, ProvenanceEntity target, WarehouseGraphEdgeType edgetype) {
		for (ProvenanceEntity source : sourceEntities) {
			this.connect(source, target, edgetype);
		}
	}
	
	/**
	 * connect a source entity with a set of target provenance entities. The entities are not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param sourceEntities the startNodes of the edges
	 * @param target the endNode of the edge
	 * @param edgetype the warehouseGraphEdgeType
	 */
	
	public void connect (ProvenanceEntity source, Iterable<ProvenanceEntity> targetEntities, WarehouseGraphEdgeType edgetype) {
		for (ProvenanceEntity target : targetEntities) {
			this.connect(source, target, edgetype);
		}
	}


	
/************************************************************* Provenance related tasks ***********************************************/		

	/**
	 * Get the Number of <a href="#{@link}">{@link WarehouseGraphNode}</a> that are connected to the <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a>
	 * with graphAgentName by the <a href="#{@link}">{@link ProvenanceGraphEdge}</a> with provenanceGraphEdgeType wasAttributedTo
	 * @param graphAgentName The name of the <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a>
	 * @return The number of <a href="#{@link}">{@link WarehouseGraphNode}</a>
	 */
	public int getNumberOfWarehouseGraphNodesAttributedProvAgent(String graphAgentName) {
		return this.warehouseGraphNodeRepository.getNumberOfWarehouseGraphNodesAttributedProvAgent(graphAgentName);
	}

}
