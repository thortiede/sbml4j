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

package org.tts.service.networks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.NetworkOptions;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.FlatEdgeService;
import org.tts.service.FlatSpeciesService;
import org.tts.service.UtilityService;
import org.tts.service.SimpleSBML.SBMLSpeciesService;
import org.tts.service.warehouse.MappingNodeService;
import org.tts.service.warehouse.OrganismService;

/**
 * 
 * Handles requests for network Mappings containing nodes and relationships.
 * It delegates the tasks to <a href="#{@link}">{@link FlatSpeciesService}</a>, 
 * <a href="#{@link}">{@link FlatEdgeService}</a> for direct actions 
 * and uses <a href="#{@link}">{@link SBMLSpeciesService}</a> 
 * to lookup missing information and enrich the flatNetworks.
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class NetworkService {

	@Autowired
	FlatSpeciesService flatSpeciesService;
	
	@Autowired
	FlatEdgeService flatEdgeService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	SBMLSpeciesService sbmlSpeciesService;
	
	@Autowired
	OrganismService organismService;
	
	@Autowired
	UtilityService utilityService;
	
	Logger log = LoggerFactory.getLogger(NetworkService.class);
	
	/**
	 * Retrieve <a href="#{@link}">{@link FlatSpecies}</a> entities for a list of entityUUIDs
	 * 
	 * @param nodeUUIDs List of entityUUIDs for the nodes to get
	 * @return List of <a href="#{@link}">{@link FlatSpecies}</a>
	 */
	public List<FlatSpecies> getNetworkNodes(List<String> nodeUUIDs) {
		List<FlatSpecies> species = new ArrayList<>();
		for (String nodeUUID : nodeUUIDs) {
			species.add(this.flatSpeciesService.findByEntityUUID(nodeUUID));
		}
		return species;
	}
	
	/**
	 * Retrieve <a href="#{@link}">{@link FlatSpecies}</a> entities for a <a href="#{@link}">{@link MappingNode}</a> with entityUUID networkEntityUUID
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Iterable of <a href="#{@link}">{@link FlatSpecies}</a> entities
	 */
	public Iterable<FlatSpecies> getNetworkNodes(String networkEntityUUID) {
		return this.mappingNodeService.getMappingFlatSpecies(networkEntityUUID);
	}
	
	/**
	 * Retrieve <a href="#{@link}">{@link FlatEdge}</a> entities of a <a href="#{@link}">{@link MappingNode}</a> with entityUUID networkEntityUUID 
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Iterable of <a href="#{@link}">{@link FlatSpecies}</a> entities
	 */
	public Iterable<FlatEdge> getNetworkRelations(String networkEntityUUID) {
		return this.flatEdgeService.getNetworkFlatEdges(networkEntityUUID);
	}
	
	/**
	 * Returns the geneSet for nodeUUIDs in network with UUID networkEntityUUID.
	 * Searches for direct connections between pairs of <a href="#{@link}">{@link FlatSpecies}</a> in the List nodeUUIDs
	 * 
	 * @param networkEntityUUID The entityUUID of the network to search in
	 * @param nodeUUIDs List of entityUUIDs of <a href="#{@link}">{@link FlatSpecies}</a> to include
	 * @return Iterable of <a href="#{@link}">{@link FlatEdge}</a> connecting the searched <a href="#{@link}">{@link FlatSpecies}</a>
	 */
	public Iterable<FlatEdge> getGeneSet(String networkEntityUUID, List<String> nodeUUIDs) {
		return this.flatEdgeService.getGeneSet(networkEntityUUID, nodeUUIDs);
	}
	
	/**
	 * Retrieve the entityUUID of a <a href="#{@link}">{@link FlatSpecies}</a> for a symbol.
	 * Searches the nodeSymbol in the Network given. If not found directly, searches the full model
	 * for occurrences of this symbol and finds the <a href="#{@link}">{@link FlatSpecies}</a> 
	 * that is derived from it. The symbol on the <a href="#{@link}">{@link FlatSpecies}</a> might differ,
	 * but it is guaranteed, that the <a href="#{@link}">{@link FlatSpecies}</a> - entityUUID returned
	 * is derived from a <a href="#{@link}">{@link SBMLSpecies}</a> that has that symbol connected to it 
	 * through a <a href="#{@link}">{@link BiomodelsQualifier}</a> relationship.
	 * 
	 * @param networkEntityUUID The entityUUID of the network to search in
	 * @param nodeSymbol The symbol to find
	 * @return entityUUID of <a href="#{@link}">{@link FlatSpecies}</a> for this symbol
	 */
	public String getFlatSpeciesEntityUUIDOfSymbolInNetwork(String networkEntityUUID, String nodeSymbol) {
		String flatSpeciesEntityUUID = this.findEntityUUIDForSymbolInNetwork(networkEntityUUID, nodeSymbol);
		
		// 0. Check whether we have that geneSymbol directly in the network
		if (flatSpeciesEntityUUID == null) {
			// 1. Find SBMLSpecies with geneSymbol (or SBMLSpecies connected to externalResource with geneSymbol)
			SBMLSpecies geneSpecies = this.sbmlSpeciesService.findBysBaseName(nodeSymbol);
			String simpleModelGeneEntityUUID = null;
			if(geneSpecies != null) {
				simpleModelGeneEntityUUID = geneSpecies.getEntityUUID();
			} else {
				Iterable<SBMLSpecies> bqSpecies = this.sbmlSpeciesService.findByBQConnectionTo(nodeSymbol, "KEGG");
				if (bqSpecies == null) {
					// we could not find that gene
					return null;
				} else {
					Iterator<SBMLSpecies> bqSpeciesIterator = bqSpecies.iterator();
					while (bqSpeciesIterator.hasNext()) {
						SBMLSpecies current = bqSpeciesIterator.next();
						log.debug("Found Species " + current.getsBaseName() + " with uuid: " + current.getEntityUUID());
						if(geneSpecies == null) {
							log.debug("Using " + current.getsBaseName());
							geneSpecies = current;
							break;
						}
					}
					if(geneSpecies != null) {
						simpleModelGeneEntityUUID = geneSpecies.getEntityUUID();
					} else {
						return null;
					}
				}
			}
			if (simpleModelGeneEntityUUID == null) {
				return null;
			}
			// 2. Find FlatSpecies in network with networkEntityUUID to use as startPoint for context search
			//geneSymbolSpecies = this.flatSpeciesRepository.findBySimpleModelEntityUUIDInNetwork(simpleModelGeneEntityUUID, networkEntityUUID);
			FlatSpecies geneSymbolSpecies = this.flatSpeciesService.findBySimpleModelEntityUUID(networkEntityUUID, simpleModelGeneEntityUUID);
			if (geneSymbolSpecies == null) {
				//return "Could not find derived FlatSpecies to SBMLSpecies (uuid:" + simpleModelGeneEntityUUID + ") in network with uuid: " + networkEntityUUID;
				return null;
			}
			flatSpeciesEntityUUID = geneSymbolSpecies.getEntityUUID();
		}
		return flatSpeciesEntityUUID;
	}
	
	
	/**
	 * Get the <a href="#{@link}">{@link NetworkOptions}</a> for the <a href="#{@link}">{@link MappingNode}</a> with entityUUId networkEntityUuid
	 * @param networkEntityUuid The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @param user The user that is attributed to the <a href="#{@link}">{@link MappingNode}</a>
	 * @return The <a href="#{@link}">{@link NetworkOptions}</a> with default values for this network, if the user is attributed to the <a href="#{@link}">{@link MappingNode}</a>, an empty <a href="#{@link}">{@link NetworkOptions}</a> otherwise
	 */
	public NetworkOptions getNetworkOptions(String networkEntityUuid, String user) {
		if (this.mappingNodeService.isMappingNodeAttributedToUser(networkEntityUuid, user)) {
			return this.mappingNodeService.getNetworkOptions(networkEntityUuid);
		} else {
			return new NetworkOptions();
		}
	}
	
	/**
	 * Finds the entityUUID of the <a href="#{@link}">{@link FlatSpecies}</a> with geneSymbol in network.
	 * Only returns a entityUUID when there is a <a href="#{@link}">{@link FlatSpecies}</a> in network that 
	 * does have the symbol geneSymbol 
	 * 
	 * @param networkEntityUUID The network to search in
	 * @param geneSymbol The symbol to search
	 * @return UUID of the <a href="#{@link}">{@link FlatSpecies}</a> with symbol geneSymbol
	 */
	public String findEntityUUIDForSymbolInNetwork(String networkEntityUUID, String geneSymbol) {
		return this.flatSpeciesService.findEntityUUIDForSymbolInNetwork(networkEntityUUID, geneSymbol);
	}

/************************************************************* NetworkInventoryItem **********************************************/	
	
	/**
	 * Create a NetworkInventoryItem for the network represented by the <a href="#{@link}">{@link MappingNode}</a> with entityUUID
	 * 
	 * @param entityUUID The entityUUID of the MappingNode to get the <a href="#{@link}">{@link NetworkInventoryItem}</a> for
	 * @return <a href="#{@link}">{@link NetworkInventoryItem}</a>
	 */
	public NetworkInventoryItem getNetworkInventoryItem(String entityUUID) {
		MappingNode mapping = this.mappingNodeService.findByEntityUUID(entityUUID);
		return getNetworkIventoryItem(mapping);
	}

	/**
	 * Create a NetworkInventoryItem for the network represented by the <a href="#{@link}">{@link MappingNode}</a> mapping
	 * 
	 * @param mapping The MappingNode to get the <a href="#{@link}">{@link NetworkInventoryItem}</a> for
	 * @return <a href="#{@link}">{@link NetworkInventoryItem}</a>
	 */
	private NetworkInventoryItem getNetworkIventoryItem(MappingNode mapping) {
		NetworkInventoryItem item = new NetworkInventoryItem();
		//item.setWarehouseGraphNodeType(WarehouseGraphNodeType.MAPPING);
		item.setUUID(UUID.fromString(mapping.getEntityUUID()));
		//item.setActive(mapping.isActive());
		// item.setSource(findSource(mapping).getSource());
		// item.setSourceVersion(findSource(mapping).getSourceVersion());
		item.setName(mapping.getMappingName());
		item.setOrganismCode((this.organismService.findOrgansimForWarehouseGraphNode(mapping.getEntityUUID())).getOrgCode());
		item.setNetworkMappingType(mapping.getMappingType());
		if (mapping.getMappingNodeTypes() != null) {
			for (String sboTerm : mapping.getMappingNodeTypes()) {
				item.addNodeTypesItem(this.utilityService.translateSBOString(sboTerm));
			}
		}
		if (mapping.getMappingRelationTypes() != null) {
			for (String type : mapping.getMappingRelationTypes()) {
				item.addRelationTypesItem(type);
			}
		}

		try {
			Map<String, Object> warehouseMap = mapping.getWarehouse();
			item.setNumberOfNodes(Integer.valueOf(((String) warehouseMap.get("numberofnodes"))));
			item.setNumberOfRelations(Integer.valueOf((String) warehouseMap.get("numberofrelations")));
		} catch (Exception e) {
			log.info("Mapping " + mapping.getMappingName() + " (" + mapping.getEntityUUID()
					+ ") does not have the warehouse-Properties set");
		}
		
		/*
		 * // add link to network retrieval String user = ((ProvenanceGraphAgentNode)
		 * this.provenanceGraphService .findByProvenanceGraphEdgeTypeAndStartNode(
		 * ProvenanceGraphEdgeType.wasAttributedTo, mapping.getEntityUUID()))
		 * .getGraphAgentName();
		 * item.add(linkTo(methodOn(WarehouseController.class).getNetwork(user,
		 * item.getUUID().toString(), false))
		 * .withRel("Retrieve Network contents as GraphML"));
		 * 
		 * // add link to the item FilterOptions
		 * item.add(linkTo(methodOn(WarehouseController.class).getNetworkFilterOptions(
		 * item.getUUID().toString())) .withRel("FilterOptions"));
		 * 
		 * // add link to deleting the item (setting isactive = false
		 * item.add(linkTo(methodOn(WarehouseController.class).deactivateNetwork(item.
		 * getUUID().toString())) .withRel("Delete Mapping").withType("DELETE"));
		 */
		return item;
	}
}
