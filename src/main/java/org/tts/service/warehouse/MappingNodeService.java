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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.AnnotationItem;
import org.tts.model.api.FilterOptions;
import org.tts.model.api.NetworkOptions;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.WarehouseGraphNode;
import org.tts.repository.warehouse.MappingNodeRepository;
import org.tts.service.GraphBaseEntityService;

/**
 * Service for handling requests to and from the MappingNodeRepository
 * and for handling anything MappingNode related
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class MappingNodeService {

	@Autowired
	MappingNodeRepository mappingNodeRepository;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	/**
	 * Creates a new <a href="#{@link}">{@link MappingNode}</a>
	 * @param parent The  <a href="#{@link}">{@link WarehouseGraphNode}</a> that the new <a href="#{@link}">{@link MappingNode}</a> is derived from
	 * @param type The <a href="#{@link}">{@link NetworkMappingType}</a> of the <a href="#{@link}">{@link MappingNode}</a>
	 * @param mappingName The name of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return The newly created and persisted <a href="#{@link}">{@link MappingNode}</a>
	 */
	public MappingNode createMappingNode(WarehouseGraphNode parent, NetworkMappingType type, String mappingName) {
		MappingNode newMappingNode = new MappingNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newMappingNode);
		newMappingNode.setOrganism(parent.getOrganism());
		newMappingNode.setMappingType(type);
		newMappingNode.setMappingName(mappingName);
		newMappingNode.addWarehouseAnnotation("creationstarttime", Instant.now());
		return this.save(newMappingNode, 0);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link MappingNode}</a> that are associated with the user user
	 * @param users List of users that are associated with the <a href="#{@link}">{@link MappingNode}</a> to get
	 * @param activeOnly Only retrieve active <a href="#{@link}">{@link MappingNode}</a> (true), or inactive also (false)
	 * @return List of <a href="#{@link}">{@link MappingNode}</a> that are associated with users
	 */
	public List<MappingNode> findAllFromUser(String user, boolean activeOnly) {
		List<String> users = new ArrayList<>();
		users.add(user);
		return this.findAllFromUsers(users, activeOnly);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link MappingNode}</a> that are associated with the user user
	 * @param users List of users that are associated with the <a href="#{@link}">{@link MappingNode}</a> to get
	 * @param activeOnly Only retrieve active <a href="#{@link}">{@link MappingNode}</a> (true), or inactive also (false)
	 * @return List of <a href="#{@link}">{@link MappingNode}</a> that are associated with users
	 */
	public List<MappingNode> findAllFromUsers(List<String> users, boolean activeOnly) {
		if (activeOnly) {
			return this.mappingNodeRepository.findAllActiveFromUsers(users);
		} else {
			return this.mappingNodeRepository.findAllFromUsers(users);
		}
	}
	
	/**
	 * Find a <a href="#{@link}">{@link MappingNode}</a> by its entityUUID
	 * @param entityUUID The UUID String of the <a href="#{@link}">{@link MappingNode}</a> to find
	 * @return The <a href="#{@link}">{@link MappingNode}</a> with entityUUID entityUUID
	 */
	public MappingNode findByEntityUUID(String entityUUID) {
		return this.mappingNodeRepository.findByEntityUUID(entityUUID);
	}

	/**
	 * Get the <a href="#{@link}">{@link AnnotationItem}</a> for the <a href="#{@link}">{@link MappingNode}</a> with entityUUID entityUUID
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return The <a href="#{@link}">{@link AnnotationItem}</a> for the <a href="#{@link}">{@link MappingNode}</a>
	 */
	public AnnotationItem getAnnotationItem(String entityUUID) {
		AnnotationItem annotationItem = new AnnotationItem();
		Map<String, Object> defaultNodeAnnotation = new HashMap<String, Object>();
		for (String nodeSymbol : this.findByEntityUUID(entityUUID).getMappingNodeSymbols()) {
			defaultNodeAnnotation.put(nodeSymbol, null);
		}
		annotationItem.setNodeAnnotation(defaultNodeAnnotation);
		
		Map<String, Object> defaultRelationAnnotation = new HashMap<String, Object>();
		for (String relationSymbol : this.findByEntityUUID(entityUUID).getMappingRelationSymbols()) {
			defaultRelationAnnotation.put(relationSymbol, null);
		}
		annotationItem.setRelationAnnotation(defaultRelationAnnotation);
		return annotationItem;
	}
	
	/**
	 * Get the <a href="#{@link}">{@link FilterOptions}</a> for the <a href="#{@link}">{@link MappingNode}</a> with entityUUID entityUUID
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return The <a href="#{@link}">{@link FilterOptions}</a> for the <a href="#{@link}">{@link MappingNode}</a>
	 */
	public FilterOptions getFilterOptions(String entityUUID) {
		FilterOptions filterOptions = new FilterOptions();
		MappingNode mappingNode = this.findByEntityUUID(entityUUID);
		filterOptions.setNodeSymbols(new ArrayList<>(mappingNode.getMappingNodeSymbols()));
		filterOptions.setNodeTypes(new ArrayList<>(mappingNode.getMappingNodeTypes()));
		filterOptions.setRelationSymbols(new ArrayList<>(mappingNode.getMappingRelationSymbols()));
		filterOptions.setRelationTypes(new ArrayList<>(mappingNode.getMappingRelationTypes()));
		return filterOptions;
	}
	
	/**
	 * Get <a href="#{@link}">{@link NetworkOptions}</a> for <a href="#{@link}">{@link MappingNode}</a> with entityUUID entityUUID
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to get the <a href="#{@link}">{@link NetworkOptions}</a> for
	 * @return The <a href="#{@link}">{@link NetworkOptions}</a> for the <a href="#{@link}">{@link MappingNode}</a> with entityUUID entityUUID
	 */
	public NetworkOptions getNetworkOptions(String entityUUID) {
		NetworkOptions networkOptions = new NetworkOptions();
		AnnotationItem annotationItem = this.getAnnotationItem(entityUUID);
		networkOptions.setAnnotation(annotationItem);
		FilterOptions filterOptions = this.getFilterOptions(entityUUID);
		networkOptions.setFilter(filterOptions);
		return networkOptions;
	}
	
	/**
	 * Get all <a href="#{@link}">{@link FlatSpecies}</a> of a Mapping
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> which contains the FlatSpecies to get
	 * @return List of <a href="#{@link}">{@link FlatSpecies}</a> that are in the Mapping
	 */
	public List<FlatSpecies> getMappingFlatSpecies(String entityUUID) {
		return this.mappingNodeRepository.getMappingFlatSpecies(entityUUID);
	}
	
	/**
	 * Check whether a <a href="#{@link}">{@link MappingNode}</a> is Attributed to a <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a> of user
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to check
	 * @param user The user to which <a href="#{@link}">{@link MappingNode}</a> should be attributed to
	 * @return true if the <a href="#{@link}">{@link MappingNode}</a> is attributed to user, false otherwise
	 */
	public boolean isMappingNodeAttributedToUser(String entityUUID, String user) {
		return this.mappingNodeRepository.isMappingNodeAttributedToUser(entityUUID, user);
	}
	
	/**
	 * Persist a <a href="#{@link}">{@link MappingNode}</a>
	 * @param node The <a href="#{@link}">{@link MappingNode}</a> to persist
	 * @param depth The depth to use for persisting (0 means only this node, 1 means also the connected nodes, larger values propagate through child nodes)
	 * @return The persisted MappingNode
	 */
	public MappingNode save(MappingNode node, int depth) {
		return this.mappingNodeRepository.save(node, depth);
	}
}
