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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.AnnotationItem;
import org.tts.model.api.FilterOptions;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.api.NetworkOptions;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.FlatEdgeService;
import org.tts.service.FlatSpeciesService;
import org.tts.service.GraphBaseEntityService;
import org.tts.service.MyDrugService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.UtilityService;
import org.tts.service.WarehouseGraphService;
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
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	MappingNodeService mappingNodeService;
		
	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	OrganismService organismService;
	
	@Autowired
	SBMLSpeciesService sbmlSpeciesService;
	
	@Autowired
	Session session;
	
	@Autowired
	UtilityService utilityService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	Logger logger = LoggerFactory.getLogger(NetworkService.class);
	
	/**
	 * Takes the FlatEdges and adds the given annotation to nodes in those edges
	 * @param contextFlatEdges The <a href="#{@link}">{@link FlatEdges}</a>  entities to scan for <a href="#{@link}">{@link FlatSpecies}</a>  to annotate
	 * @param annotationName The name of the annotation
	 * @param annotationType The type of the annotation as a string
	 * @param nodeAnnotation Map of node symbols to annotation
	 */
	public void addInlineNodeAnnotation(Iterable<FlatEdge> flatEdges, String annotationName,
			String annotationType, Map<String, Object> nodeAnnotation) {
		Iterator<FlatEdge> flatEdgeIterator = flatEdges.iterator();
		while (flatEdgeIterator.hasNext()) {
			FlatEdge currentEdge = flatEdgeIterator.next();
			if(nodeAnnotation.containsKey(currentEdge.getInputFlatSpecies().getSymbol())) {
				this.graphBaseEntityService.addAnnotation(currentEdge.getInputFlatSpecies(), annotationName, annotationType, nodeAnnotation.get(currentEdge.getInputFlatSpecies().getSymbol()), false);
				nodeAnnotation.remove(currentEdge.getInputFlatSpecies().getSymbol());
			}
			if(nodeAnnotation.containsKey(currentEdge.getOutputFlatSpecies().getSymbol())) {
				this.graphBaseEntityService.addAnnotation(currentEdge.getOutputFlatSpecies(), annotationName, annotationType, nodeAnnotation.get(currentEdge.getOutputFlatSpecies().getSymbol()), false);
				nodeAnnotation.remove(currentEdge.getOutputFlatSpecies().getSymbol());
			}		
		}
	}

	/**
	 * Create a new <a href="#{@link}">{@link MappingNode}</a> and annotate the created mapping
	 * @param user The user that the new <a href="#{@link}">{@link MappingNode}</a> is associated with
	 * @param annotationItem The <a href="#{@link}">{@link AnnotationItem}</a> that holds the annotation to add
	 * @param networkEntityUUID The base network to derive the annotated <a href="#{@link}">{@link MappingNode}</a> from
	 * @return The new <a href="#{@link}">{@link MappingNode}</a>
	 */
	public MappingNode annotateNetwork(String user, @Valid AnnotationItem annotationItem, String networkEntityUUID) {
		
		MappingNode parent = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
		String newMappingName = parent.getMappingName() + "_annotate_with" + 
				(annotationItem.getNodeAnnotationName() != null ? "_nodeAnnotation:" + annotationItem.getNodeAnnotationName() : "") + 
				(annotationItem.getRelationAnnotationName() != null ? "_relationAnnotation:" + annotationItem.getRelationAnnotationName() : "");
		
		String activityName = "Add_annotation_to_mapping_" + networkEntityUUID + 
				(annotationItem.getNodeAnnotationName() != null ? "_nodeAnnotation:" + annotationItem.getNodeAnnotationName() : "") + 
				(annotationItem.getRelationAnnotationName() != null ? "_relationAnnotation:" + annotationItem.getRelationAnnotationName() : "");
		
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createMapping;
		NetworkMappingType mappingType = parent.getMappingType();
		
		
		MappingNode newMapping = this.createMappingPre(user, parent, newMappingName, activityName, activityType,
				mappingType);

		Iterable<FlatEdge> networkRelations = this.getNetworkRelations(networkEntityUUID);
		Iterable<FlatEdge> resettedEdges = resetFlatEdges(networkRelations);
		Iterator<FlatEdge> resettedEdgeIterator = resettedEdges.iterator();
		
		this.session.clear();
		
		boolean addNodeAnnotation = false;
		Map<String, Object> nodeSymbolsToAnnotation = annotationItem.getNodeAnnotation();
		String nodeAnnotationName = annotationItem.getNodeAnnotationName();
		String nodeAnnotationType = annotationItem.getNodeAnnotationType();
		if(nodeSymbolsToAnnotation != null && nodeSymbolsToAnnotation.size() > 0 && nodeAnnotationName != null && nodeAnnotationType != null) {
			addNodeAnnotation = true;
		}
		boolean addEdgeAnnotation = false;
		Map<String, Object> edgeSymbolsToAnnotation = annotationItem.getRelationAnnotation();
		String edgeAnnotationName = annotationItem.getRelationAnnotationName();
		String edgeAnnotationType = annotationItem.getRelationAnnotationType();
		if(edgeSymbolsToAnnotation != null && nodeSymbolsToAnnotation.size() > 0 && edgeAnnotationName != null && edgeAnnotationType != null) {
			addEdgeAnnotation = true;
		}
		Set<String> annotatedFlatSpeciesUUID = new HashSet<>();
		while (resettedEdgeIterator.hasNext()) {
			FlatEdge currentEdge = resettedEdgeIterator.next();
			if (addEdgeAnnotation && edgeSymbolsToAnnotation.containsKey(currentEdge.getSymbol())) {
				// add annotation to this edge
				this.graphBaseEntityService.addAnnotation(currentEdge, edgeAnnotationName, edgeAnnotationType, edgeSymbolsToAnnotation.get(currentEdge.getSymbol()), false);
			}
			if (addNodeAnnotation && !annotatedFlatSpeciesUUID.contains(currentEdge.getInputFlatSpecies().getEntityUUID())
						&& nodeSymbolsToAnnotation.containsKey(currentEdge.getInputFlatSpecies().getSymbol())) {
				this.graphBaseEntityService.addAnnotation(currentEdge.getInputFlatSpecies(), nodeAnnotationName, nodeAnnotationType, 
						nodeSymbolsToAnnotation.get(currentEdge.getInputFlatSpecies().getSymbol()), false);
			}
			if (addNodeAnnotation && !annotatedFlatSpeciesUUID.contains(currentEdge.getOutputFlatSpecies().getEntityUUID())
					&& nodeSymbolsToAnnotation.containsKey(currentEdge.getOutputFlatSpecies().getSymbol())) {
				this.graphBaseEntityService.addAnnotation(currentEdge.getOutputFlatSpecies(), nodeAnnotationName, nodeAnnotationType, 
					nodeSymbolsToAnnotation.get(currentEdge.getOutputFlatSpecies().getSymbol()), false);
			}
		}
		Iterable<FlatEdge> persistedEdges = this.flatEdgeService.save(resettedEdges, 1);
		this.connectContainsFlatEdgeSpecies(newMapping, persistedEdges);
		
		// update MappingNode
		newMapping.setMappingNodeSymbols(parent.getMappingNodeSymbols());
		newMapping.setMappingNodeTypes(parent.getMappingNodeTypes());
		newMapping.setMappingRelationSymbols(parent.getMappingRelationSymbols());
		newMapping.setMappingRelationTypes(parent.getMappingRelationTypes());
		newMapping.addWarehouseAnnotation("creationendtime", Instant.now());
		String newMappingEntityUUID = newMapping.getEntityUUID();
		newMapping.addWarehouseAnnotation("numberofnodes", this.getNumberOfNetworkNodes(newMappingEntityUUID));
		newMapping.addWarehouseAnnotation("numberofrelations", this.getNumberOfNetworkRelations(newMappingEntityUUID));
		
		if(addNodeAnnotation) {
			newMapping.addAnnotationType("node." + nodeAnnotationName,
					nodeAnnotationType);
		}
		if(addEdgeAnnotation) {
			newMapping.addAnnotationType("relation." + edgeAnnotationName, edgeAnnotationType);
		}
		
		return this.mappingNodeService.save(newMapping, 0);
	}

	/**
	 * Copy a network in the database and associate it to a user
	 * Takes all <a href="#{@link}">{@link FlatEdge}</a> entities and <a href="#{@link}">{@link FlatSpecies}</a> entities and creates copies of them in the database
	 * Adds the copied entities to the List of CONTAINED entities in the newly created <a href="#{@link}">{@link MappingNode}</a>
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to copy
	 * @param user The user to associate the newly created <a href="#{@link}">{@link MappingNode}</a> with
	 * @return The newly created <a href="#{@link}">{@link MappingNode}</a>
	 */
	public MappingNode copyNetwork(String networkEntityUUID, String user, String namePrefix) {
		// Activity
		MappingNode parent = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
		String newMappingName = namePrefix + "_" + parent.getMappingName();
		String activityName = "Create_" + namePrefix + "_" + networkEntityUUID;
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createMapping;
		NetworkMappingType mappingType = parent.getMappingType();
		// Create the new <a href="#{@link}">{@link MappingNode}</a> and link it to parent, activity and agent
		MappingNode newMapping = this.createMappingPre(user, parent, newMappingName, activityName, activityType,
				mappingType);
		// Get all FlatSpecies of the network to copy
		Iterable<FlatSpecies> networkSpecies = this.getNetworkNodes(networkEntityUUID);
		
		// Get and reset all the new <a href="#{@link}">{@link FlatEdge}</a> entities
		Iterable<FlatEdge> networkRelations = this.getNetworkRelations(networkEntityUUID);
		Iterable<FlatEdge> resettedEdges = resetFlatEdges(networkRelations);
		// reset any remaining unconnected species
		Iterable<FlatSpecies> unconnectedSpecies = resetFlatSpecies(networkSpecies);
		
		// clear the session to re-fetch all entities on request and not reuse old ids
		this.session.clear();
		
		// save the new Edges
		Iterable<FlatEdge> newFlatEdges = this.flatEdgeService.save(resettedEdges, 1);
		// save the unconnected Species
		Iterable<FlatSpecies> newFlatSpecies = this.flatSpeciesService.save(unconnectedSpecies, 0);
		
		connectContains(newMapping, newFlatEdges, newFlatSpecies);
		
		// update MappingNode
		newMapping.setMappingNodeSymbols(parent.getMappingNodeSymbols());
		newMapping.setMappingNodeTypes(parent.getMappingNodeTypes());
		newMapping.setMappingRelationSymbols(parent.getMappingRelationSymbols());
		newMapping.setMappingRelationTypes(parent.getMappingRelationTypes());
		newMapping.addWarehouseAnnotation("creationendtime", Instant.now());
		String newMappingEntityUUID = newMapping.getEntityUUID();
		newMapping.addWarehouseAnnotation("numberofnodes", this.getNumberOfNetworkNodes(newMappingEntityUUID));
		newMapping.addWarehouseAnnotation("numberofrelations", this.getNumberOfNetworkRelations(newMappingEntityUUID));
		return this.mappingNodeService.save(newMapping, 0);
	}
	
	/**
	 * Create a new <a href="#{@link}">{@link MappingNode}</a> that contains the given <a href="#{@link}">{@link FlatEdge}</a> entities as content
	 * @param user The user to associate with the network
	 * @param baseNetworkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> this mapping is derived from
	 * @param contextFlatEdges The <a href="#{@link}">{@link FlatEdge}</a> entities that make up the network
	 * @return The <a href="#{@link}">{@link MappingNode}</a> that was created and contains the network
	 */
	public MappingNode createMappingFromFlatEdges(String user, String baseNetworkEntityUUID, List<FlatEdge> flatEdges, String networkName) {
		MappingNode parent = this.mappingNodeService.findByEntityUUID(baseNetworkEntityUUID);
		String activityName = "Create_" + networkName;
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createContext;
		// Create the new <a href="#{@link}">{@link MappingNode}</a> and link it to parent, activity and agent
		MappingNode contextMappingNode = this.createMappingPre(user, parent, networkName, activityName, activityType, parent.getMappingType());
		Iterable<FlatEdge> resettedEdges = resetFlatEdges(flatEdges);
		// clear the session to re-fetch all entities on request and not reuse old ids
		this.session.clear();
		
		// save the new Edges
		Iterable<FlatEdge> newFlatEdges = this.flatEdgeService.save(resettedEdges, 1);
		
		connectContainsFlatEdgeSpecies(contextMappingNode, newFlatEdges);
		
		// updateMappingNode
		String networkEntityUUID = contextMappingNode.getEntityUUID();
		contextMappingNode.setMappingNodeSymbols(this.getNetworkNodeSymbols(networkEntityUUID));
		contextMappingNode.setMappingNodeTypes(this.getNetworkNodeTypes(networkEntityUUID));
		contextMappingNode.setMappingRelationSymbols(this.getNetworkRelationSymbols(networkEntityUUID));
		contextMappingNode.setMappingRelationTypes(this.getNetworkRelationTypes(networkEntityUUID));
		contextMappingNode.addWarehouseAnnotation("creationendtime", Instant.now());
		contextMappingNode.addWarehouseAnnotation("numberofnodes", this.getNumberOfNetworkNodes(networkEntityUUID));
		contextMappingNode.addWarehouseAnnotation("numberofrelations", this.getNumberOfNetworkRelations(networkEntityUUID));
		return this.mappingNodeService.save(contextMappingNode, 0);
	}

	/**
	 * Create necessary warehouse items for adding a new MappingNode
	 * 
	 * @param user The user to associate with the network
	 * @param parent The parent <a href="#{@link}">{@link MappingNode}</a> for this operation
	 * @param newMappingName The name of the new <a href="#{@link}">{@link MappingNode}</a> to create
	 * @param activityName The name of the <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> that creates this <a href="#{@link}">{@link MappingNode}</a>
	 * @param activityType The <a href="#{@link}">{@link ProvenanceGraphActivityType}</a> of the <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> that creates this <a href="#{@link}">{@link MappingNode}</a>
	 * @param mappingType The <a href="#{@link}">{@link NetworkMappingType}</a> of the new <a href="#{@link}">{@link MappingNode}</a>
	 * @return The new <a href="#{@link}">{@link MappingNode}</a>
	 */
	public MappingNode createMappingPre(String user, MappingNode parent, String newMappingName,
			String activityName, ProvenanceGraphActivityType activityType, NetworkMappingType mappingType) {
		// AgentNode
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", user);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(agentNodeProperties);
		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", activityType);
		
		activityNodeProvenanceProperties.put("graphactivityname", activityName);
		ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createMappingActivityNode, userAgentNode,
				ProvenanceGraphEdgeType.wasAssociatedWith);
		Instant startTime = Instant.now();
		
		// create new networkMapping that is the same as the parent one
		// need mapingNode (name: OldName + step)
		MappingNode newMapping = this.mappingNodeService.createMappingNode(parent, mappingType,
				newMappingName);
		newMapping.addWarehouseAnnotation("creationstarttime", startTime.toString());
		this.provenanceGraphService.connect(newMapping, parent, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(newMapping, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		this.provenanceGraphService.connect(newMapping, createMappingActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy);
		return newMapping;
	}
	
	
	/**
	 * Deactivate a <a href="#{@link}">{@link MappingNode}</a>
	 * @param mappingNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return true if the <a href="#{@link}">{@link MappingNode}</a> was inactivated, false if it didn't exist
	 */
	public boolean deactivateNetwork(String mappingNodeEntityUUID) {
		MappingNode networkNode = this.mappingNodeService.findByEntityUUID(mappingNodeEntityUUID);
		if (networkNode != null) {
			networkNode.setActive(false);
			this.mappingNodeService.save(networkNode, 0);
			return true;
		}
		return false;
	}

	/**
	 * Delete a <a href="#{@link}">{@link MappingNode}</a> and it's contents along with connected Provenance entities
	 * @param mappingNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return true if the <a href="#{@link}">{@link MappingNode}</a> and all connected entities were removed from the database, false if something went wrong
	 */
	public boolean deleteNetwork(String mappingNodeEntityUUID) {
		
		try {
			List<FlatSpecies> flatSpeciesToDelete = this.mappingNodeService.getMappingFlatSpecies(mappingNodeEntityUUID);
			if (flatSpeciesToDelete != null) {
				this.flatSpeciesService.deleteAll(flatSpeciesToDelete);
			}
			List<FlatSpecies> notDeletedFlatSpecies = this.mappingNodeService.getMappingFlatSpecies(mappingNodeEntityUUID);
			if (notDeletedFlatSpecies != null && notDeletedFlatSpecies.size() > 0) {
				return false;
			}
			// get the activity connected to the mappingNode with wasGeneratedBy
			ProvenanceEntity generatorActivity = this.provenanceGraphService.findByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, mappingNodeEntityUUID);
			if(generatorActivity != null) {
				this.provenanceGraphService.deleteProvenanceEntity(generatorActivity);
			}
			// get the user connected with wasAttributedTo
			ProvenanceEntity attributedToAgent = this.provenanceGraphService.findByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasAttributedTo, mappingNodeEntityUUID);
			if (attributedToAgent != null) {
				this.provenanceGraphService.deleteProvenanceEntity(attributedToAgent);
			}
			// then delete the mapping node
			this.provenanceGraphService.deleteProvenanceEntity(this.mappingNodeService.findByEntityUUID(mappingNodeEntityUUID));
		} catch (Exception e) {
			logger.warn("Could not cleanly delete Mapping with uuid: " + mappingNodeEntityUUID);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Filter a network according to the <a href="#{@link}">{@link FilterOptions}</a> given
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to filter
	 * @param filterOptions The <a href="#{@link}">{@link FilterOptions}</a> to apply
	 * @param user The user to associate the filtered network to
	 * @return The <a href="#{@link}">{@link MappingNode}</a> of the filtered network
	 */
	public MappingNode filterNetwork(String networkEntityUUID, FilterOptions filterOptions, String user) {
		// Activity
		MappingNode parent = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
		String newMappingName = "Filter_network_" + parent.getMappingName();
		String activityName = "Filter_network_" + networkEntityUUID;
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createMapping;
		NetworkMappingType mappingType = parent.getMappingType();
		// Create the new <a href="#{@link}">{@link MappingNode}</a> and link it to parent, activity and agent
		MappingNode newMapping = this.createMappingPre(user, parent, newMappingName, activityName, activityType,
				mappingType);

		// Lists to check for Filtering
		final List<String> relationTypesToBeIncluded = filterOptions.getRelationTypes();
		final List<String> nodeTypesToBeIncluded = filterOptions.getNodeTypes();
		final List<String> nodeSymbolsToBeIncluded = filterOptions.getNodeSymbols();
		final List<String> relationSymbolsToBeIncluded = filterOptions.getRelationSymbols();
		
		// Get the network contents
		Iterable<FlatEdge> networkRelations = this.getNetworkRelations(networkEntityUUID);
		
		Iterator<FlatEdge> relationIterator = networkRelations.iterator();
		while (relationIterator.hasNext()) {
			FlatEdge currentEdge = relationIterator.next();
			if (!relationSymbolsToBeIncluded.contains(currentEdge.getSymbol()) ||
				!relationTypesToBeIncluded.contains(currentEdge.getTypeString()) ||
				!nodeSymbolsToBeIncluded.contains(currentEdge.getInputFlatSpecies().getSymbol()) ||
				!nodeTypesToBeIncluded.contains(this.utilityService.translateSBOString(currentEdge.getInputFlatSpecies().getSboTerm())) ||
				!nodeSymbolsToBeIncluded.contains(currentEdge.getOutputFlatSpecies().getSymbol()) ||
				!nodeTypesToBeIncluded.contains(this.utilityService.translateSBOString(currentEdge.getOutputFlatSpecies().getSboTerm()))
				) {
				relationIterator.remove();
			} else {
				this.graphBaseEntityService.resetGraphBaseEntityProperties(currentEdge);
				this.graphBaseEntityService.resetGraphBaseEntityProperties(currentEdge.getInputFlatSpecies());
				this.graphBaseEntityService.resetGraphBaseEntityProperties(currentEdge.getOutputFlatSpecies());
			}
		}
		
		this.session.clear();
		
		// Persist the FlatEdge entities
		Iterable<FlatEdge> persistedFlatEdges = this.flatEdgeService.save(networkRelations, 1);
		// connect to MappingNode
		connectContainsFlatEdgeSpecies(newMapping, persistedFlatEdges);
		
		
		// update MappingNode
		newMapping.addWarehouseAnnotation("creationendtime", Instant.now());
		String newMappingEntityUUID = newMapping.getEntityUUID();
		newMapping.addWarehouseAnnotation("numberofnodes", this.getNumberOfNetworkNodes(newMappingEntityUUID));
		newMapping.addWarehouseAnnotation("numberofrelations", this.getNumberOfNetworkRelations(newMappingEntityUUID));
		newMapping.setMappingNodeSymbols(this.getNetworkNodeSymbols(newMappingEntityUUID));
		newMapping.setMappingNodeTypes(this.getNetworkNodeTypes(newMappingEntityUUID));
		newMapping.setMappingRelationSymbols(this.getNetworkRelationSymbols(newMappingEntityUUID));
		newMapping.setMappingRelationTypes(this.getNetworkRelationTypes(newMappingEntityUUID));
		
		return this.mappingNodeService.save(newMapping, 0);
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
						logger.debug("Found Species " + current.getsBaseName() + " with uuid: " + current.getEntityUUID());
						if(geneSpecies == null) {
							logger.debug("Using " + current.getsBaseName());
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
	 * Get the node symbols of a <a href="#{@link}">{@link MappingNode}</a> with entityUUID
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Set of Strings with node symbols
	 */
	public Set<String> getNetworkNodeSymbols(String entityUUID) {
		return this.getNetworkNodeSymbols(this.getNetworkNodes(entityUUID));
	}

	/**
	 * Get the node symbols of a collection of <a href="#{@link}">{@link FlatSpecies}</a> 
	 * @param networkNodes The collection of <a href="#{@link}">{@link FlatSpecies}</a> 
	 * @return Set of Strings with node symbols
	 */
	public Set<String> getNetworkNodeSymbols(Iterable<FlatSpecies> networkNodes) {
		Set<String> networkNodeNames = new HashSet<>();
		for (FlatSpecies node : networkNodes) {
			networkNodeNames.add(node.getSymbol());
		}
		return networkNodeNames;
	}
	
	/**
	 * Get the number of nodes in a <a href="#{@link}">{@link MappingNode}</a> with entityUUID
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return The number of nodes
	 */
	public int getNumberOfNetworkNodes(String networkEntityUUID) {
		return (int) StreamSupport.stream(this.getNetworkNodes(networkEntityUUID).spliterator(), false).count(); 
	}
	
	/**
	 * Get the node types of a <a href="#{@link}">{@link MappingNode}</a> with entityUUID
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Set of Strings with node types
	 */
	public Set<String> getNetworkNodeTypes(String networkEntityUUID) {
		return this.getNetworkNodeTypes(this.getNetworkNodes(networkEntityUUID));
	}
	
	/**
	 * Get the node types of a collection of <a href="#{@link}">{@link FlatSpecies}</a> 
	 * @param networkNodes The collection of <a href="#{@link}">{@link FlatSpecies}</a> 
	 * @return Set of Strings with node types
	 */
	public Set<String> getNetworkNodeTypes(Iterable<FlatSpecies> networkNodes) {
		Set<String> nodeTypes = new HashSet<>();
		for (FlatSpecies node : networkNodes) {
			nodeTypes.add(node.getSboTerm());
		}
		return nodeTypes;
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
	 * Get the relation symbols of a <a href="#{@link}">{@link MappingNode}</a> with entityUUID networkEntityUUID 
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Set of String with relation symbols
	 */
	public Set<String> getNetworkRelationSymbols(String networkEntityUUID) {
		return this.getNetworkRelationSymbols(this.getNetworkRelations(networkEntityUUID));
	}

	/**
	 * Get the relation symbols of a collection of <a href="#{@link}">{@link FlatEdge}</a> entities
	 * @param networkRelations The collection of <a href="#{@link}">{@link FlatEdge}</a> entities
	 * @return Set of String with relation symbols
	 */
	public Set<String> getNetworkRelationSymbols(Iterable<FlatEdge> networkRelations) {
		Set<String> relationSymbols = new HashSet<>();
		for (FlatEdge edge : networkRelations) {
			relationSymbols.add(edge.getSymbol());
		}
		return relationSymbols;
	}
		
	/**
	 * Get the relation types of a <a href="#{@link}">{@link MappingNode}</a> with entityUUID networkEntityUUID 
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Set of String with relation types
	 */
	public Set<String> getNetworkRelationTypes(String networkEntityUUID) {
		return this.getNetworkRelationTypes(this.getNetworkRelations(networkEntityUUID));	
	}
	
	/**
	 * Get the relation types of a collection of <a href="#{@link}">{@link FlatEdge}</a> entities
	 * @param networkRelations The collection of <a href="#{@link}">{@link FlatEdge}</a> entities
	 * @return Set of String with relation relations
	 */
	public Set<String> getNetworkRelationTypes(Iterable<FlatEdge> networkRelations) {
		Set<String> networkRelationTypes = new HashSet<>();
		for (FlatEdge edge : networkRelations) {
			networkRelationTypes.add(edge.getTypeString());
		}
		return networkRelationTypes;
	}
	
	/**
	 * Get the number of relations of a <a href="#{@link}">{@link MappingNode}</a> with entityUUID networkEntityUUID 
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return The number of relations
	 */
	public int getNumberOfNetworkRelations(String networkEntityUUID) {
		return (int) StreamSupport.stream(this.getNetworkRelations(networkEntityUUID).spliterator(), false).count(); 
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
	 * Connect a set of <a href="#{@link}">{@link FlatEdge}</a> entities (or rather the <a href="#{@link}">{@link FlatSpecies}</a> within) to a <a href="#{@link}">{@link MappingNode}</a>
	 * @param mappingNode The <a href="#{@link}">{@link MappingNode}</a> to connect to
	 * @param flatEdges The Iterable of <a href="#{@link}">{@link FlatEdge}</a> entities to connect
	 * @param flatSpecies Additionally connect those <a href="#{@link}">{@link FlatSpecies}</a> entities
	 */
	private void connectContains(MappingNode mappingNode, Iterable<FlatEdge> flatEdges,
			Iterable<FlatSpecies> flatSpecies) {
		// now connect the new entities to the MappingNode
		connectContainsFlatEdgeSpecies(mappingNode, flatEdges);
		// connect the unconnected species
		Iterator<FlatSpecies> newFlatSpeciesIterator = flatSpecies.iterator();
		while (newFlatSpeciesIterator.hasNext()) {
			this.warehouseGraphService.connect(mappingNode, newFlatSpeciesIterator.next(), WarehouseGraphEdgeType.CONTAINS);
		}
	}

	/**
	 * Connect a set of <a href="#{@link}">{@link FlatEdge}</a> entities (or rather the <a href="#{@link}">{@link FlatSpecies}</a> within) to a <a href="#{@link}">{@link MappingNode}</a>
	 * @param mappingNode The <a href="#{@link}">{@link MappingNode}</a> to connect to
	 * @param flatEdges The Iterable of <a href="#{@link}">{@link FlatEdge}</a> entities to connect
	 */
	private void connectContainsFlatEdgeSpecies(MappingNode mappingNode, Iterable<FlatEdge> flatEdges) {
		Iterator<FlatEdge> newFlatEdgeIterator = flatEdges.iterator();
		while (newFlatEdgeIterator.hasNext()) {
			FlatEdge currentEdge = newFlatEdgeIterator.next();
			this.warehouseGraphService.connect(mappingNode, currentEdge.getInputFlatSpecies(), WarehouseGraphEdgeType.CONTAINS);
			this.warehouseGraphService.connect(mappingNode, currentEdge.getOutputFlatSpecies(), WarehouseGraphEdgeType.CONTAINS);
		}
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
		item.setOrganismCode((this.organismService.findOrganismForWarehouseGraphNode(mapping.getEntityUUID())).getOrgCode());
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
			logger.info("Mapping " + mapping.getMappingName() + " (" + mapping.getEntityUUID()
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
	
	/** 
	 * Reset the entityUUID (draw a new UUID) and set id and version to null of the given <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param flatSpecies The <a href="#{@link}">{@link FlatSpecies}</a> to reset
	 * @return Iterable of resetted FlatSpecies
	 */
	private Iterable<FlatSpecies> resetFlatSpecies(Iterable<FlatSpecies> flatSpecies) {
		Iterator<FlatSpecies> speciesIterator = flatSpecies.iterator();
		while (speciesIterator.hasNext()) {
			this.graphBaseEntityService.resetGraphBaseEntityProperties(speciesIterator.next());
		}
		return flatSpecies;
	}

	/**
	 * Reset the entityUUID (draw a new UUID) and set id and version to null of the given <a href="#{@link}">{@link FlatEdges}</a> as well as the contained <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param flatEdges The <a href="#{@link}">{@link FlatEdge}</a> entities to reset
	 * @return Iterable of resetted FlatEdges
	 */
	private Iterable<FlatEdge> resetFlatEdges(Iterable<FlatEdge> flatEdges) {
		Iterator<FlatEdge> edgeIterator = flatEdges.iterator();
		while (edgeIterator.hasNext()) {
			FlatEdge currentEdge = edgeIterator.next();
			this.graphBaseEntityService.resetGraphBaseEntityProperties(currentEdge);
			this.graphBaseEntityService.resetGraphBaseEntityProperties(currentEdge.getInputFlatSpecies());
			this.graphBaseEntityService.resetGraphBaseEntityProperties(currentEdge.getOutputFlatSpecies());
		}
		return flatEdges;
	}
}
