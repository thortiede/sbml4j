package org.tts.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.WarehouseGraphEdge;
import org.tts.model.warehouse.WarehouseGraphNode;
import org.tts.repository.warehouse.WarehouseGraphEdgeRepository;
import org.tts.repository.warehouse.WarehouseGraphNodeRepository;
import org.tts.service.warehouse.OrganismService;

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



	
/************************************************************* WarehouseInventoryItem ***********************************************/	
	
	/**
	 * Get a <a href="#{@link}">{@link WarehouseInventoryItem}</a> for a <a href="#{@link}">{@link WarehouseGraphNode}</a>
	 * @param warehouseGraphNode The entity to get the <a href="#{@link}">{@link WarehouseInventoryItem}</a> for
	 * @return The <a href="#{@link}">{@link WarehouseInventoryItem}</a>
	 */
	public WarehouseInventoryItem getWarehouseInventoryItem(WarehouseGraphNode warehouseGraphNode) {
		WarehouseInventoryItem item = new WarehouseInventoryItem();
		item.setUUID(UUID.fromString(warehouseGraphNode.getEntityUUID()));
		// item.setSource(findSource(warehouseGraphNode).getSource());
		// item.setSourceVersion(findSource(warehouseGraphNode).getSourceVersion());
		item.setOrganismCode((this.organismService
				.findOrganismForWarehouseGraphNode(warehouseGraphNode.getEntityUUID())).getOrgCode());
		if (warehouseGraphNode.getClass() == MappingNode.class) {
			item.setName(((MappingNode) warehouseGraphNode).getMappingName());
		}
		return item;
	}

}
