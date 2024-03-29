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
package org.sbml4j.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.sbml4j.model.api.pathway.PathwayInventoryItem;
import org.sbml4j.model.base.GraphEnum.WarehouseGraphEdgeType;
import org.sbml4j.model.full.SBMLReaction;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.sbml.SBMLCompartment;
import org.sbml4j.model.sbml.SBMLSBaseEntity;
import org.sbml4j.model.sbml.SBMLSpecies;
import org.sbml4j.model.sbml.SBMLSpeciesGroup;
import org.sbml4j.model.sbml.ext.qual.SBMLQualSpecies;
import org.sbml4j.model.sbml.ext.qual.SBMLQualSpeciesGroup;
import org.sbml4j.model.sbml.simple.SBMLSimpleReaction;
import org.sbml4j.model.sbml.simple.ext.qual.SBMLSimpleTransition;
import org.sbml4j.model.warehouse.Organism;
import org.sbml4j.model.warehouse.PathwayCollectionNode;
import org.sbml4j.model.warehouse.PathwayNode;
import org.sbml4j.repository.warehouse.PathwayNodeRepository;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.sbml4j.service.networks.NetworkService;
import org.sbml4j.service.warehouse.OrganismService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class PathwayService {

	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	OrganismService organismService;
	
	@Autowired
	PathwayNodeRepository pathwayNodeRepository;
	
	@Autowired
	UtilityService utilityService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	private final Logger logger = LoggerFactory.getLogger(NetworkService.class);
	
	/**
	 * Create a new <a href="#{@link}">{@link PathwayNode}</a>
	 * @param idString The idString to set
	 * @param nameString The nameString to set
	 * @param org The organism this <a href="#{@link}">{@link PathwayNode}</a> is for
	 * @return The newly created and persisted <a href="#{@link}">{@link PathwayNode}</a>
	 */
	public PathwayNode createPathwayNode(String idString, String nameString, Organism org) {
		PathwayNode newPathwayNode = new PathwayNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newPathwayNode);
		newPathwayNode.setOrganism(org);
		newPathwayNode.setPathwayIdString(idString);
		newPathwayNode.setPathwayNameString(nameString);
		return this.pathwayNodeRepository.save(newPathwayNode);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link PathwayNode}</a> names for a <a href="#{@link}">{@link FlatSpecies}</a> in a <a href="#{@link}">{@link MappingNode}</a>
	 * @param flatSpeciesEntityUUID The entityUUID of the <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param mappingNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return List of String as Names of pathways
	 */
		
	/**
	 * Find all distinct Species SBO Terms included in a <a href="#{@link}">{@link PathwayNode}</a>
	 * @param pathwayNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return Iterable of Strings containing the SBO Terms
	 */
	public Iterable<String> getAllDistinctSpeciesSboTermsOfPathway(String pathwayNodeEntityUUID) {
		return this.pathwayNodeRepository.getAllDistinctSpeciesSboTermsOfPathway(pathwayNodeEntityUUID);
	}

	/**
	 * Find all distinct Relation SBO Terms included in a <a href="#{@link}">{@link PathwayNode}</a>
	 * @param pathwayNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return Iterable of Strings containing the SBO Terms
	 */
	public Iterable<String> getAllDistinctTransitionSboTermsOfPathway(String pathwayNodeEntityUUID) {
		return this.pathwayNodeRepository.getAllDistinctTransitionSboTermsOfPathway(pathwayNodeEntityUUID);
	}

	
	/**
	 * Get a List of <a href="#{@link}">{@link PathwayNode}</a> that are connected to an <a href="#{@link}">{@link SBMLSBaseEntity}</a>
	 * @param sBaseEntityUUID The UUID of the <a href="#{@link}">{@link SBMLSBaseEntity}</a>
	 * @return List of <a href="#{@link}">{@link PathwayNode}</a> that are connected to the <a href="#{@link}">{@link SBMLSBaseEntity}</a>
	 */
	public List<PathwayNode> getPathwayNodesOfSBase(UUID sBaseEntityUUID) {
		return this.pathwayNodeRepository.getPathwayNodesOfSBase(sBaseEntityUUID.toString());
	}
	
	
	/**
	 * Get a List of UUIDs of pathways associated with a user
	 * @param user The user the pathways are associated with
	 * @param hideCollections Do not include <a href="#{@link}">{@link PathwayCollectionNode}</a> entities (true), or include them (false)
	 * @return List of UUIDs of Pathways associated with the user
	 */
	public List<String> getListofPathwayUUIDs(List<String> users, boolean hideCollections) {
		List<String> uuids = new ArrayList<>();
		Iterable<PathwayNode> pathways;
		for (String user : users) {
			if(hideCollections) {
				pathways = this.pathwayNodeRepository.findNonCollectionPathwaysAttributedToUser(user);
			} else {
				pathways = this.pathwayNodeRepository.findAllPathwaysAttributedToUser(user);
			}
			for (PathwayNode pathwayNode : pathways) {
				uuids.add(pathwayNode.getEntityUUID());
			}
		}
		return uuids;
	}

	/**
	 * Find a <a href="#{@link}">{@link PathwayNode}</a> by its entityUUID
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a> 
	 * @return The found <a href="#{@link}">{@link PathwayNode}</a> 
	 */
	public PathwayNode findByEntityUUID(String entityUUID) {
		return this.pathwayNodeRepository.findByEntityUUID(entityUUID);
	}
	
	/**
	 * Find the number of connecting nodes that share a pathway connection with two input nodes
	 * @param pathway1UUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a> of node 1
	 * @param pathway2UUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a> of node 2
	 * @param geneOfPathway1 The symbol of the node 1
	 * @param genesOfPathway2 The symbol of the node 2
	 * @return number of connection nodes
	 */
	public int findNumberOfConnectingGenesForTwoPathwaysOfGeneAndGeneSet(String pathway1UUID, String pathway2UUID,
			String geneOfPathway1, Set<String> genesOfPathway2) {
		return this.pathwayNodeRepository.findNumberOfConnectingGenesForTwoPathwaysOfGeneAndGeneSet(pathway1UUID, pathway2UUID, geneOfPathway1, genesOfPathway2);
	}
	
	/**
	 * Get a List of entityUUIDs of <a href="#{@link}">{@link SBMLSBaseEntity}</a> that are in a pathway
	 * @param pathwayUUID The entiyUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return List of Strings of entityUUIDs
	 */
	List<String> getSBaseUUIDsOfPathwayNodes(String pathwayUUID) {
		return this.pathwayNodeRepository.getSBaseUUIDsOfPathwayNodes(pathwayUUID);
	}
	
/************************************************************* PathwayInventory ***********************************************/	
	
	/**
	 * Get a list of <a href="#{@link}">{@link PathwayInventoryItem}</a> for Pathways associated to a user
	 * @param user The user to find the pathways for
	 * @param hideCollections Hide Collection pathways (true) or show them (false)
	 * @return List of <a href="#{@link}">{@link PathwayInventoryItem}</a> for user
	 */
	public List<PathwayInventoryItem> getListofPathwayInventory(List<String> users, boolean hideCollections) {
		List<PathwayInventoryItem> pathwayInventoryItemList = new ArrayList<>();
		Iterable<PathwayNode> pathways;
		for (String user : users) {
			if(hideCollections) {
				pathways = this.pathwayNodeRepository.findNonCollectionPathwaysAttributedToUser(user);
			} else {
				pathways = this.pathwayNodeRepository.findAllPathwaysAttributedToUser(user);
			}
			for (PathwayNode pathwayNode : pathways) {
				PathwayInventoryItem item = getPathwayInventoryItem(pathwayNode);
				pathwayInventoryItemList.add(item);
			}
		}

		return pathwayInventoryItemList;
	}

	/**
	 * Get a <a href="#{@link}">{@link PathwayInventoryItem}</a> for a <a href="#{@link}">{@link PathwayNode} for which the entityUUID is provided</a>
	 * @param PathwayNodeEntityUUID The entityUUID of the PathwayNode
	 * @return The <a href="#{@link}">{@link PathwayInventoryItem}</a>
	 */
	public PathwayInventoryItem getPathwayInventoryItem(String PathwayNodeEntityUUID) {
		PathwayNode pathway = this.findByEntityUUID(PathwayNodeEntityUUID);
		return this.getPathwayInventoryItem(pathway);
	}
	
	/**
	 * Get a <a href="#{@link}">{@link PathwayInventoryItem}</a> for a <a href="#{@link}">{@link PathwayNode}</a>
	 * @param user The user name as String that the <a href="#{@link}">{@link PathwayNode}</a> is associated with
	 * @param pathwayNode The <a href="#{@link}">{@link PathwayNode}</a> to get the <a href="#{@link}">{@link PathwayInventoryItem}</a> for
	 * @return The <a href="#{@link}">{@link PathwayInventoryItem}</a>
	 */
	public PathwayInventoryItem getPathwayInventoryItem(PathwayNode pathwayNode) {
		PathwayInventoryItem item = new PathwayInventoryItem();
		item.setUUID(UUID.fromString(pathwayNode.getEntityUUID()));
		item.setPathwayId(pathwayNode.getPathwayIdString());
		item.setName(pathwayNode.getPathwayNameString());
		item.setOrganismCode(this.organismService
				.findOrganismForWarehouseGraphNode(pathwayNode.getEntityUUID()).getOrgCode());
		List<ProvenanceEntity> pathwayNodes = this.warehouseGraphService
				.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.CONTAINS,
						pathwayNode.getEntityUUID());
		int nodeCounter = 0;
		int qualNodeCounter = 0;
		int transitionCounter = 0;
		int reactionCounter = 0;
		List<String> compartmentList = new ArrayList<>();
		for (ProvenanceEntity node : pathwayNodes) {
			if (node.getClass() == SBMLSpecies.class || node.getClass() == SBMLSpeciesGroup.class) {
				String nodeType = this.utilityService.translateSBOString(((SBMLSBaseEntity) node).getsBaseSboTerm());
				if (item.getNodeTypes() == null || !item.getNodeTypes().contains(nodeType)) {
					item.addNodeTypesItem(nodeType);
				}
				nodeCounter++;
			} else if (node.getClass() == SBMLSimpleTransition.class) {
				String transitionType = this.utilityService.translateSBOString(((SBMLSBaseEntity) node).getsBaseSboTerm());
				if (item.getTransitionTypes() == null || !item.getTransitionTypes().contains(transitionType)) {
					item.addTransitionTypesItem(transitionType);
				}
				transitionCounter++;
			} else if (node.getClass() == SBMLReaction.class || node.getClass() == SBMLSimpleReaction.class) {
				reactionCounter++;
			} else if (node.getClass() == SBMLCompartment.class) {
				compartmentList.add(((SBMLCompartment) node).getsBaseName());
			} else if (node.getClass() == SBMLQualSpecies.class || node.getClass() == SBMLQualSpeciesGroup.class) {
				qualNodeCounter++;
			} else {
				logger.warn("Node with entityUUID: " + node.getEntityUUID() + " has unexpected NodeType");
			}
		}
		item.setCompartments(compartmentList);
		
		if (nodeCounter==0 &&qualNodeCounter > 0) {
			item.setNumberOfNodes(qualNodeCounter);
		} else {		
			item.setNumberOfNodes(nodeCounter);
		}
		item.setNumberOfTransitions(transitionCounter);
		item.setNumberOfReactions(reactionCounter);
	
		return item;
	}
}
