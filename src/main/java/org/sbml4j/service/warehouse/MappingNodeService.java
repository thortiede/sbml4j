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
package org.sbml4j.service.warehouse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml4j.model.api.network.AnnotationItem;
import org.sbml4j.model.api.network.FilterOptions;
import org.sbml4j.model.api.network.NetworkOptions;
import org.sbml4j.model.base.GraphEnum.NetworkMappingType;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.provenance.ProvenanceGraphEdge;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.model.warehouse.WarehouseGraphNode;
import org.sbml4j.repository.warehouse.MappingNodeRepository;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.GraphBaseEntityService;
import org.sbml4j.service.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	ConfigService configService;
	
	@Autowired
	MappingNodeRepository mappingNodeRepository;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	UtilityService utilityService;
	
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
		newMappingNode.setActive(false);
		newMappingNode.addWarehouseAnnotation("creationstarttime", Instant.now().toString());
		return this.save(newMappingNode, 1);
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
	 * Find a  <a href="#{@link}">{@link MappingNode}</a>  by its mappingName and the associated user
	 * Uses the config parameter sbml4j.network.allow-inactive-duplicates to determine if only active networks should be searched for
	 * If sbml4j.network.allow-inactive-duplicates=true duplicate names are allowed as long as only one version is set to isActive
	 * This might be desired if sbml4j.network.hard-delete=False, as then delete operations only set the isActive Flag to False
	 * @param name The mappingName attribute of the  <a href="#{@link}">{@link MappingNode}</a>  to find 
	 * @param user The name of the  <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a> associated with the mapping
	 * @return The <a href="#{@link}">{@link MappingNode}</a> found, if it exists, null otherwise. 
	 */
	public MappingNode findByNetworkNameAndUser(String name, String user) {
		if (this.configService.isAllowInactiveDuplicates()) {
			return this.mappingNodeRepository.findActiveByMappingNameAndUser(name, user);
		} else {
			return this.mappingNodeRepository.findByMappingNameAndUser(name, user);
		}
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
		Set<String> mappingNodeSBOTerms = mappingNode.getMappingNodeTypes();
		for (String sboTerm : mappingNodeSBOTerms) {
			filterOptions.addNodeTypesItem(this.utilityService.translateSBOString(sboTerm));
		}
		filterOptions.setRelationSymbols(new ArrayList<>(mappingNode.getMappingRelationSymbols()));
		filterOptions.setRelationTypes(new ArrayList<>(mappingNode.getMappingRelationTypes()));
		return filterOptions;
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
	 * Get the Number of <a href="#{@link}">{@link MappingNode}</a> that are connected to the <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a>
	 * with graphAgentName by the <a href="#{@link}">{@link ProvenanceGraphEdge}</a> with provenanceGraphEdgeType wasAttributedTo
	 * @param graphAgentName The name of the <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a>
	 * @return The number of <a href="#{@link}">{@link MappingNode}</a>
	 */
	public int getNumberOfMappingNodesAttributedProvAgent(String graphAgentName) {
		return this.mappingNodeRepository.getNumberOfMappingNodesAttributedProvAgent(graphAgentName);
	}
	
	
	/**
	 * Check whether a <a href="#{@link}">{@link MappingNode}</a> can be accessed by a <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a>,
	 * including the configured public user
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to check
	 * @param user The user who <a href="#{@link}">{@link MappingNode}</a> should have access to the <a href="#{@link}">{@link MappingNode}</a>
	 * @return true if the <a href="#{@link}">{@link MappingNode}</a> is accessible for the user (or the public user), false otherwise
	 */
	public boolean isMappingNodeAccesibleForUsers(String entityUUID, String user) {
	return this.mappingNodeRepository.isMappingNodeAccesibleForUsers(entityUUID, this.configService.getUserList(user));
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
