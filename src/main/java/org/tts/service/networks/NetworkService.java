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
package org.tts.service.networks;

import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;
import org.tts.Exception.NetworkAlreadyExistsException;
import org.tts.model.api.AnnotationItem;
import org.tts.model.api.FilterOptions;
import org.tts.model.api.NetworkInventoryItem;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.service.ConfigService;
import org.tts.service.ContextService;
import org.tts.service.CsvService;
import org.tts.service.FlatEdgeService;
import org.tts.service.FlatSpeciesService;
import org.tts.service.GraphBaseEntityService;
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
	ConfigService configService;
	
	@Autowired
	ContextService contextService;
	
	@Autowired
	CsvService csvService;
	
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
	
	Logger log = LoggerFactory.getLogger(NetworkService.class);
	
	/**
	 * Adds data contained in the provided Multipart files to the network as annotations on nodes
	 * Currently only node-annotations are possible
	 * @param user The user that the new <a href="#{@link}">{@link MappingNode}</a> is associated with
	 * @param data The Multipart files containing the data
	 * @param type String representing the type of data being added. Will become a node label on affected nodes.
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to add the data to
	 * @param networkname An optional name for the resulting network, or prefix to the name (if prefixName is true)
	 * @param prefixName Whether to prefix the existing network name (true) with the given networkname or replace it with networkname (false)
	 * @param derive Whether to create a copy of the existing network (true), or add data to the network without copying it (false).
	 * @return The <a href="#{@link}">{@link MappingNode}</a> which holds the <a href="#{@link}">{@link FlatSpecies}</a> having the data added to them.
	 * @throws NetworkAlreadyExistsException If a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists for the given user
	 * @throws IOException If an input file cannot be read
	 */
	public MappingNode addCsvDataToNetwork(	String user,
											List<MultipartFile> data,
											String type,
											String networkEntityUUID,
											String networkname,
											boolean prefixName,
											boolean derive) throws NetworkAlreadyExistsException, IOException{
		

		Map<String, List<Map<String, String>>> annotationMap;
		MappingNode newNetwork = getCopiedOrNamedMappingNode(user, networkEntityUUID, networkname, prefixName,
				derive, type + "_on_");
		String annotatedNetworkEntityUUID = newNetwork.getEntityUUID();
		for (MultipartFile file : data) {
			log.debug("Processing file " + file.getOriginalFilename());
			annotationMap = this.csvService.parseCsv(file); // can throw IOException
			Map<String, FlatSpecies> annotatedSpecies = new HashMap<>();
			for(String geneSymbol :annotationMap.keySet()) {
				List<FlatSpecies> currentList = this.getFlatSpeciesOfSymbolInNetwork(annotatedNetworkEntityUUID, geneSymbol);
				if (currentList == null)
					continue;
				for (FlatSpecies current : currentList) {
					List<Map<String, String>> currentAnnotation = annotationMap.get(geneSymbol);
					if (currentAnnotation == null) {
						// just a safe guard
						log.warn("Failed to retrieve annotation element for symbol: " + current.getSymbol() + " although it should be present (annotation element might be null)");
						continue;
					}
					
					int annotationNum = 1;
					boolean addedAnnotation = false;
					for (Map<String, String> individualAnnotationMap : currentAnnotation) {
						// just for excluding other species for now
						/*if (individualAnnotationMap.get("Species") == null 
								|| (individualAnnotationMap.get("Species") != null 
									&& !individualAnnotationMap.get("Species").startsWith("Humans"))
								) {
							continue;
						}*/
						for (String key : individualAnnotationMap.keySet()) {
							String value = individualAnnotationMap.get(key);
							//if ("NA".equals(value)) {
							//	continue;
							//}
							this.graphBaseEntityService.addAnnotation(current, type + "_" + annotationNum + "_" + key.replace('.', '_'), "string", value, false);
						}
						annotationNum++;
						addedAnnotation = true;
					}
					if (addedAnnotation) {
						this.graphBaseEntityService.addAnnotation(current, type, "boolean", true, false);
						current.addLabel(type);
						annotatedSpecies.putIfAbsent(current.getEntityUUID(), current);
					}
					
					
				}
			}
			log.info("Added annotation to " + annotatedSpecies.size() + " nodes in network with uuid: " + annotatedNetworkEntityUUID + " from file " + file.getOriginalFilename());
			this.flatSpeciesService.save(annotatedSpecies.values(), 0);
		}
		this.updateMappingNodeMetadata(newNetwork);
		return newNetwork;
	}
	
	
	
	/**
	 * Takes the FlatEdges and adds the given annotation to nodes in those edges
	 * @param contextFlatEdges The <a href="#{@link}">{@link FlatEdges}</a> entities to scan for <a href="#{@link}">{@link FlatSpecies}</a>  to annotate
	 * @param annotationName The name of the annotation
	 * @param annotationType The type of the annotation as a string
	 * @param nodeAnnotation Map of node symbols to annotation
	 */
	public void addNodeAnnotation(Iterable<FlatEdge> flatEdges, String annotationName,
			String annotationType, Map<String, Object> nodeAnnotation) {
		log.info("Adding node annotation " + annotationName);
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
	 * Takes the FlatEdges and adds the given annotation to them
	 * @param flatEdges The <a href="#{@link}">{@link FlatEdges}</a> entities to add annotations to
	 * @param annotationName The name of the annotation
	 * @param annotationType The type of the annotation as a string
	 * @param relationAnnotation Map of relation symbols to annotation
	 */
	public void addRelationAnnotation(Iterable<FlatEdge> flatEdges, String annotationName,
			String annotationType, Map<String, Object> relationAnnotation) {
		log.info("Adding relation annotation " + annotationName);
		Iterator<FlatEdge> flatEdgeIterator = flatEdges.iterator();
		while (flatEdgeIterator.hasNext()) {
			FlatEdge currentEdge = flatEdgeIterator.next();
			if (relationAnnotation.containsKey(currentEdge.getSymbol())) {
				this.graphBaseEntityService.addAnnotation(currentEdge, annotationName, annotationType, relationAnnotation.get(currentEdge.getSymbol()), false);
				relationAnnotation.remove(currentEdge.getSymbol());
			}
		}
	}

	/**
	 * Annotate network entities and create a new <a href="#{@link}">{@link MappingNode}</a> if requested
	 * @param user The user that the new <a href="#{@link}">{@link MappingNode}</a> is associated with
	 * @param annotationItem The <a href="#{@link}">{@link AnnotationItem}</a> that holds the annotation to add
	 * @param networkEntityUUID The base network to derive the annotated <a href="#{@link}">{@link MappingNode}</a> from
	 * @param derive whether to derive a new network and put annotation on it (true), or directly add annotation on given network (false)
	 * @return The new <a href="#{@link}">{@link MappingNode}</a>
	 * @throws NetworkAlreadyExistsException If a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists for the given user
	 */
	public MappingNode annotateNetwork(	String user, 
										AnnotationItem annotationItem,
										String networkEntityUUID,
										String networkname,
										boolean prefixName,
										boolean derive) throws NetworkAlreadyExistsException {
		if (derive) {
			log.info("Deriving network from " + networkEntityUUID + " and adding annotation.");
		} else {
			log.info("Annotating network with uuid " + networkEntityUUID + " directly");
		}
		
		StringBuilder prefixSB = new StringBuilder();
		prefixSB.append("Annotate_with");
		if (annotationItem.getNodeAnnotationName() != null && annotationItem.getNodeAnnotationType() != null) {
			prefixSB.append("_node.");
			prefixSB.append(annotationItem.getNodeAnnotationName());
			prefixSB.append(".");
			prefixSB.append(annotationItem.getNodeAnnotationType());
		}
		if (annotationItem.getRelationAnnotationName() != null && annotationItem.getRelationAnnotationType() != null) {
			prefixSB.append("_relation.");
			prefixSB.append(annotationItem.getRelationAnnotationName());
			prefixSB.append(".");
			prefixSB.append(annotationItem.getRelationAnnotationType());
		}
		String prefixString = prefixSB.toString();
		MappingNode mappingToAnnotate = getCopiedOrNamedMappingNode(user, networkEntityUUID, networkname, prefixName,
				derive, prefixString);

		Iterable<FlatEdge> networkRelations = this.getNetworkRelations(mappingToAnnotate.getEntityUUID());
		if (annotationItem.getNodeAnnotationName() != null && annotationItem.getNodeAnnotationType() != null) {
			addNodeAnnotation(networkRelations, 
					annotationItem.getNodeAnnotationName(), annotationItem.getNodeAnnotationType(), annotationItem.getNodeAnnotation());
			mappingToAnnotate.addAnnotationType("node." + annotationItem.getNodeAnnotationName(),
					annotationItem.getNodeAnnotationType());
		}
		if (annotationItem.getRelationAnnotationName() != null && annotationItem.getRelationAnnotationType() != null) {
			addRelationAnnotation(networkRelations, 
					annotationItem.getRelationAnnotationName(), annotationItem.getRelationAnnotationType(), annotationItem.getRelationAnnotation());
			mappingToAnnotate.addAnnotationType("relation." + annotationItem.getRelationAnnotationName(), 
					annotationItem.getRelationAnnotationType());
		}
		this.flatEdgeService.save(networkRelations, 1);
		return this.mappingNodeService.save(mappingToAnnotate, 0);
	}

	/**
	 * Create a new <a href="#{@link}">{@link MappingNode}</a> and annotate the created mapping
	 * @param user The user that the new <a href="#{@link}">{@link MappingNode}</a> is associated with
	 * @param annotationItem The <a href="#{@link}">{@link AnnotationItem}</a> that holds the annotation to add
	 * @param networkEntityUUID The base network to derive the annotated <a href="#{@link}">{@link MappingNode}</a> from
	 * @return The new <a href="#{@link}">{@link MappingNode}</a>
	 */
	@Deprecated
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
		newMapping.addWarehouseAnnotation("creationendtime", Instant.now().toString());
		String newMappingEntityUUID = newMapping.getEntityUUID();
		newMapping.addWarehouseAnnotation("numberofnodes", String.valueOf(this.getNumberOfNetworkNodes(newMappingEntityUUID)));
		newMapping.addWarehouseAnnotation("numberofrelations", String.valueOf(this.getNumberOfNetworkRelations(newMappingEntityUUID)));
		
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
	 * @param name The name for the new network
	 * @param prefixName Whether to use the given name as prefix to the old name (true) or replace the old name with the given name (false)
	 * @return The newly created <a href="#{@link}">{@link MappingNode}</a>
	 * @throws NetworkAlreadyExistsException if a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists for the given user
	 */
	public MappingNode copyNetwork(String networkEntityUUID, String user, String name, boolean prefixName) throws NetworkAlreadyExistsException {
		log.info("Copying network with uuid: " + networkEntityUUID);
		
		// Activity
		MappingNode parent = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
		String newMappingName = buildMappingName(name, prefixName, parent.getMappingName());
		
		// Check the MappingName against existing names for that user
		// The name should be unique per user
		// if user already exists AND a mapping with that name already exists, then error
		// if user doesn't exist in the first place, it is fine and we do not need to check for the network name
		if (this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user) != null
				&& this.mappingNodeService.findByNetworkNameAndUser(newMappingName, user) != null) {
			throw new NetworkAlreadyExistsException(1, "Network with name " + newMappingName + " already exists for user " + user + ". The UUID is: " + this.mappingNodeService.findByNetworkNameAndUser(newMappingName, user).getEntityUUID());
		}
		
		String activityName = "Create_" + newMappingName;
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
		newMapping.addWarehouseAnnotation("creationendtime", Instant.now().toString());
		String newMappingEntityUUID = newMapping.getEntityUUID();
		newMapping.addWarehouseAnnotation("numberofnodes", String.valueOf(this.getNumberOfNetworkNodes(newMappingEntityUUID)));
		newMapping.addWarehouseAnnotation("numberofrelations", String.valueOf(this.getNumberOfNetworkRelations(newMappingEntityUUID)));
		newMapping.setActive(true);
		return this.mappingNodeService.save(newMapping, 0);
	}
	
	/**
	 * Fill the <a href="#{@link}">{@link MappingNode}</a> with copies of the given <a href="#{@link}">{@link FlatEdge}</a> entities as content
	 * @param graphAgent The <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a> to associate with the network
	 * @param baseNetworkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> this mapping is derived from
	 * @param flatEdges The <a href="#{@link}">{@link FlatEdge}</a> entities that make up the network
	 * @param mappingNodeForFlatEdges The <a href="#{@link}">{@link MappingNode}</a>  to use for the new Mapping
	 * @return The <a href="#{@link}">{@link MappingNode}</a> that was created and contains the network
	 */
	public MappingNode createMappingFromFlatEdges(ProvenanceGraphAgentNode graphAgent, String baseNetworkEntityUUID, List<FlatEdge> flatEdges, MappingNode mappingNodeForFlatEdges) {
		log.info("Creating mapping from FlatEdges from baseNetwork with uuid: " + baseNetworkEntityUUID + " using mappingNode " + mappingNodeForFlatEdges.getMappingName() + "(" + mappingNodeForFlatEdges.getEntityUUID() + ")");
		
		Iterable<FlatEdge> resettedEdges = resetFlatEdges(flatEdges);
		// clear the session to re-fetch all entities on request and not reuse old ids
		this.session.clear();
		
		// save the new Edges
		log.info("Persisting new mapping contents in database...");
		Iterable<FlatEdge> newFlatEdges = this.flatEdgeService.save(resettedEdges, 1);
		log.info("Writing Provenance Information of new mapping..");
		connectContainsFlatEdgeSpecies(mappingNodeForFlatEdges, newFlatEdges);
		log.info("Writing metadata..");
		updateMappingNodeMetadata(mappingNodeForFlatEdges);
		log.info("Activating newly created mapping");
		return this.mappingNodeService.save(mappingNodeForFlatEdges, 0);
	}
	
	
	/**
	 * Create a new <a href="#{@link}">{@link MappingNode}</a> that contains the given <a href="#{@link}">{@link FlatEdge}</a> entities as content
	 * @param user The user to associate with the network
	 * @param baseNetworkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> this mapping is derived from
	 * @param flatEdges The <a href="#{@link}">{@link FlatEdge}</a> entities that make up the network
	 * @param networkName The name of the newly created <a href="#{@link}">{@link MappingNode}</a>
	 * @return The <a href="#{@link}">{@link MappingNode}</a> that was created and contains the network
	 */
	public MappingNode createMappingFromFlatEdges(ProvenanceGraphAgentNode graphAgent, String baseNetworkEntityUUID, List<FlatEdge> flatEdges, String networkName) {
		log.info("Creating mapping from FlatEdges from baseNetwork with uuid: " + baseNetworkEntityUUID);
		MappingNode parent = this.mappingNodeService.findByEntityUUID(baseNetworkEntityUUID);
		String activityName = "Create_" + networkName;
		ProvenanceGraphActivityType activityType = ProvenanceGraphActivityType.createContext;
		// Create the new <a href="#{@link}">{@link MappingNode}</a> and link it to parent, activity and agent
		MappingNode mappingNodeForFlatEdges = this.createMappingPre(graphAgent, parent, networkName, activityName, activityType, parent.getMappingType());
		Iterable<FlatEdge> resettedEdges = resetFlatEdges(flatEdges);
		// clear the session to re-fetch all entities on request and not reuse old ids
		this.session.clear();
		
		// save the new Edges
		log.info("Persisting new mapping contents in database...");
		Iterable<FlatEdge> newFlatEdges = this.flatEdgeService.save(resettedEdges, 1);
		log.info("Writing Provenance Information of new mapping..");
		connectContainsFlatEdgeSpecies(mappingNodeForFlatEdges, newFlatEdges);
		log.info("Writing metadata..");
		updateMappingNodeMetadata(mappingNodeForFlatEdges);
		log.info("Activating newly created mapping");
		return this.mappingNodeService.save(mappingNodeForFlatEdges, 0);
	}

	
	/**
	 * Create necessary warehouse items for adding a new MappingNode when aganetNode is already present
	 * @param graphAgent The ProvenanceGraphAgentNode to be associated with the new Mapping
	 * @param parent The parent <a href="#{@link}">{@link MappingNode}</a> for this operation
	 * @param newMappingName The name of the new <a href="#{@link}">{@link MappingNode}</a> to create
	 * @param activityName The name of the <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> that creates this <a href="#{@link}">{@link MappingNode}</a>
	 * @param activityType The <a href="#{@link}">{@link ProvenanceGraphActivityType}</a> of the <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> that creates this <a href="#{@link}">{@link MappingNode}</a>
	 * @param mappingType The <a href="#{@link}">{@link NetworkMappingType}</a> of the new <a href="#{@link}">{@link MappingNode}</a>
	 * @return The new <a href="#{@link}">{@link MappingNode}</a>
	 */
	public MappingNode createMappingPre(ProvenanceGraphAgentNode graphAgent, MappingNode parent, String newMappingName,
			String activityName, ProvenanceGraphActivityType activityType, NetworkMappingType mappingType) {
		// Need Activity
		Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
		activityNodeProvenanceProperties.put("graphactivitytype", activityType);
		
		activityNodeProvenanceProperties.put("graphactivityname", activityName);
		ProvenanceGraphActivityNode createMappingActivityNode = this.provenanceGraphService
				.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
		this.provenanceGraphService.connect(createMappingActivityNode, graphAgent,
				ProvenanceGraphEdgeType.wasAssociatedWith);
				
		// create new networkMapping that is the same as the parent one
		// need mapingNode (name: OldName + step)
		log.info("Creating MappingNode " + newMappingName);
		MappingNode newMapping = this.mappingNodeService.createMappingNode(parent, mappingType,
				newMappingName);
		this.provenanceGraphService.connect(newMapping, parent, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(newMapping, graphAgent, ProvenanceGraphEdgeType.wasAttributedTo);
		this.provenanceGraphService.connect(newMapping, createMappingActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy);
		log.info("New Mapping has uuid: " + newMapping.getEntityUUID());
		return newMapping;
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
		log.info("Creating preconditions for new mapping with parent: " + parent.getEntityUUID());
		// AgentNode
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService
				.createProvenanceGraphAgentNode(user, ProvenanceGraphAgentType.User);
		return this.createMappingPre(userAgentNode, parent, newMappingName, activityName, activityType, mappingType);
		
	}
	
	
	/**
	 * Wrapper for creating a new network consisting of the calculated context for the given geneNames on the parentMapping
	 * @param user The user for which the context is calculated
	 * @param geneNames The List of Strings representing the geneSymols to calculate the context for
	 * @param parentMapping The <a href="#{@link}">{@link MappingNode}</a> the context should be calculated in
	 * @param minSize The minimal size of the neighborhood around each given gene
	 * @param maxSize The maximal size of the neighborhood around each given gene
	 * @param terminateAt The node label that should terminate the neighborhood search
	 * @param direction The direction in which to search (along directed edges (downstream), against directed edges (upstream)or both (both)
	 * @param networkname The name to give the newly created <a href="#{@link}">{@link MappingNode}</a>
	 * @param prefixName Whether to prefix the given networkname to the parentMapping name
	 * @return The created <a href="#{@link}">{@link MappingNode}</a> holding the context network
	 * @throws NetworkAlreadyExistsException if a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists for the given user
	 * @throws Exception if something else fails during context creation (message gives reason)
	 */
	public MappingNode createContextNetwork(String user, List<String> geneNames, MappingNode parentMapping, Integer minSize,
			Integer maxSize, String terminateAt, String direction, String weightproperty, String networkname, Boolean prefixName)
			throws NetworkAlreadyExistsException,Exception {
		// 1. Determine the name of the network
		String contextNetworkName;
		if (networkname != null) {
			contextNetworkName = this.buildMappingName(networkname, prefixName, parentMapping.getMappingName());
		} else {
			// Create a default networkname to prefix or use as name for the new network
			StringBuilder defaultName = new StringBuilder();
			defaultName.append("Context");
			for (String gene : geneNames) {
				defaultName.append("_");
				defaultName.append(gene);
			}
			if (prefixName) {
				defaultName.append("_in_");
				defaultName.append(parentMapping.getMappingName());
			}
			contextNetworkName = defaultName.toString();
		}
		// 2. Check if that name already exists
		// if user already exists AND a mapping with that name already exists, then error
		// if user doesn't exist in the first place, it is fine and we do not need to check for the network name
		if (this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user) != null
				&& this.mappingNodeService.findByNetworkNameAndUser(contextNetworkName, user) != null) {
			throw new NetworkAlreadyExistsException(1, "Network with name " + contextNetworkName + " already exists for user " + user + ". The UUID is: " + this.mappingNodeService.findByNetworkNameAndUser(contextNetworkName, user).getEntityUUID());
		}
		
		// 3. We need a graphAgent for creating the new network
		ProvenanceGraphAgentNode agent = this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user);
		if(agent == null) { // && !this.configService.isPublicUser(user)) {
			// create a new agent for this operation
			agent = this.provenanceGraphService.createProvenanceGraphAgentNode(user, ProvenanceGraphAgentType.User);
		} //else {
			//throw new Exception("Failed to assign user to context mapping.");
		//}
		// 4. get the context
		List<FlatEdge> contextFlatEdges = this.contextService.getNetworkContextFlatEdges(parentMapping.getEntityUUID(), geneNames, minSize, maxSize, terminateAt, direction, weightproperty);
		if(contextFlatEdges == null) {
			throw new Exception("Failed to gather context from parent network");
		}
		// 5. Create the mapping holding the context
		MappingNode contextNetwork = this.createMappingFromFlatEdges(agent, parentMapping.getEntityUUID(), contextFlatEdges, contextNetworkName);
		
		return contextNetwork;
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
			if (attributedToAgent != null && this.warehouseGraphService.getNumberOfWarehouseGraphNodesAttributedProvAgent(((ProvenanceGraphAgentNode) attributedToAgent).getGraphAgentName()) == 1) {
				this.provenanceGraphService.deleteProvenanceEntity(attributedToAgent);
			}
			// then delete the mapping node
			this.provenanceGraphService.deleteProvenanceEntity(this.mappingNodeService.findByEntityUUID(mappingNodeEntityUUID));
		} catch (Exception e) {
			log.warn("Could not cleanly delete Mapping with uuid: " + mappingNodeEntityUUID);
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
	 * @throws NetworkAlreadyExistsException If a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists for the given user
	 */
	public MappingNode filterNetwork(	String user,
										FilterOptions filterOptions,
										String networkEntityUUID,
										String networkname, 
										boolean prefixName) throws NetworkAlreadyExistsException {
		// Activity
		MappingNode parent = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
		String newMappingName = buildMappingName(networkname, prefixName, parent.getMappingName());
		// check if name already exists for user
		// if user already exists AND a mapping with that name already exists, then error
		// if user doesn't exist in the first place, it is fine and we do not need to check for the network name
		if (this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user) != null
				&& this.mappingNodeService.findByNetworkNameAndUser(newMappingName, user) != null) {
			throw new NetworkAlreadyExistsException(1, "Mapping with name " + newMappingName + " already exists for user " + user + ". The UUID is: " + this.mappingNodeService.findByNetworkNameAndUser(newMappingName, user).getEntityUUID()); 
		}
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
		newMapping.addWarehouseAnnotation("creationendtime", Instant.now().toString());
		String newMappingEntityUUID = newMapping.getEntityUUID();
		newMapping.addWarehouseAnnotation("numberofnodes", String.valueOf(this.getNumberOfNetworkNodes(newMappingEntityUUID)));
		newMapping.addWarehouseAnnotation("numberofrelations", String.valueOf(this.getNumberOfNetworkRelations(newMappingEntityUUID)));
		newMapping.setMappingNodeSymbols(this.getNetworkNodeSymbols(newMappingEntityUUID));
		newMapping.setMappingNodeTypes(this.getNetworkNodeTypes(newMappingEntityUUID));
		newMapping.setMappingRelationSymbols(this.getNetworkRelationSymbols(newMappingEntityUUID));
		newMapping.setMappingRelationTypes(this.getNetworkRelationTypes(newMappingEntityUUID));
		newMapping.setActive(true);
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
	 * Finds the entityUUID of the <a href="#{@link}">{@link FlatSpecies}</a> with geneSymbol in network.
	 * Only returns a entityUUID when there is a <a href="#{@link}">{@link FlatSpecies}</a> in network that 
	 * does have the symbol geneSymbol 
	 * 
	 * @param networkEntityUUID The network to search in
	 * @param geneSymbol The symbol to search
	 * @return UUID of the <a href="#{@link}">{@link FlatSpecies}</a> with symbol geneSymbol
	 */
	public Iterable<FlatSpecies> findAllEntityForSymbolInNetwork(String networkEntityUUID, String geneSymbol) {
		return this.flatSpeciesService.findAllNetworkNodesForSymbol(networkEntityUUID, geneSymbol);
	}

	/**
	 * Copy the <a href="#{@link}">{@link MappingNode}</a> with UUID networkEntityUUID or set name of <a href="#{@link}">{@link MappingNode}</a>
	 * according to input parameters
	 * @param user The user associated with the new <a href="#{@link}">{@link MappingNode}</a>
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to be copied or altered
	 * @param networkname An optional name for the resulting network, or prefix to the name (if prefixName is true)
	 * @param prefixName Whether to prefix the existing network name (true) with the given prefixString or replace it with networkname (false)
	 * @param derive Whether to create a copy of the existing network (true), or add data to the network without copying it (false).
	 * @param prefixString The String to prefix the existing name with, if applicable
	 * @return The copied or named <a href="#{@link}">{@link MappingNode}</a> 
	 * @throws NetworkAlreadyExistsException If a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists for the given user
	 */
	public MappingNode getCopiedOrNamedMappingNode(String user, String networkEntityUUID, String networkname,
			boolean prefixName, boolean derive, String prefixString) throws NetworkAlreadyExistsException {
		MappingNode copiedOrNamedMappingNode = null;
		if (derive) {
			copiedOrNamedMappingNode = this.copyNetwork(networkEntityUUID, user, networkname != null ? networkname : prefixString, prefixName);
			//return this.annotateNetwork(user, annotationItem, networkEntityUUID);
		} else {
			copiedOrNamedMappingNode = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
			String newMappingName;
			if (networkname == null && prefixName) {
				newMappingName = prefixString + "_" + copiedOrNamedMappingNode.getMappingName();
			} else if (networkname != null && !prefixName) {
				newMappingName = networkname;
			} else if (networkname != null && prefixName) {
				newMappingName = networkname.endsWith("_") 
						? networkname + copiedOrNamedMappingNode.getMappingName() 
						: networkname + "_" + copiedOrNamedMappingNode.getMappingName();
			} else {
				newMappingName = copiedOrNamedMappingNode.getMappingName();
			}
			// Check the MappingName against existing names for that user
			// The name should be unique per user
			// if user already exists AND a mapping with that name already exists, then error
			// if user doesn't exist in the first place, it is fine and we do not need to check for the network name
			if (!newMappingName.equals(copiedOrNamedMappingNode.getMappingName()) 
					&& (this.provenanceGraphService.findProvenanceGraphAgentNode(ProvenanceGraphAgentType.User, user) != null
							&& this.mappingNodeService.findByNetworkNameAndUser(newMappingName, user) != null)) {
				throw new NetworkAlreadyExistsException(1, "Mapping with name " + newMappingName + " already exists for user " + user + ". The UUID is: " + this.mappingNodeService.findByNetworkNameAndUser(newMappingName, user).getEntityUUID());
			}
			copiedOrNamedMappingNode.setMappingName(newMappingName);
		}
		return copiedOrNamedMappingNode;
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
	 * Retrieve the entityUUIDs of a <a href="#{@link}">{@link FlatSpecies}</a> for a symbol.
	 * Searches the nodeSymbol in the Network given. If not found directly, searches the full model
	 * for occurrences of this symbol and finds the <a href="#{@link}">{@link FlatSpecies}</a> 
	 * that is derived from it. The symbol on the <a href="#{@link}">{@link FlatSpecies}</a> might differ,
	 * but it is guaranteed, that the <a href="#{@link}">{@link FlatSpecies}</a> - entityUUID returned
	 * is derived from a <a href="#{@link}">{@link SBMLSpecies}</a> that has that symbol connected to it 
	 * through a <a href="#{@link}">{@link BiomodelsQualifier}</a> relationship.
	 * 
	 * @param networkEntityUUID The entityUUID of the network to search in
	 * @param nodeSymbol The symbol to find
	 * @return List of entityUUID of <a href="#{@link}">{@link FlatSpecies}</a> for this symbol
	 */
	@Deprecated
	public List<String> getFlatSpeciesEntityUUIDOfSymbolInNetwork(String networkEntityUUID, String nodeSymbol) {
		List<String> foundEntityUUIDs= new ArrayList<>();
		// 0. Check whether we have that geneSymbol directly in the network
		String flatSpeciesEntityUUID = this.findEntityUUIDForSymbolInNetwork(networkEntityUUID, nodeSymbol);
		if (flatSpeciesEntityUUID == null) {
			// could not find the symbol, check secondary names
			List<FlatSpecies> secondaryNameFlatSpecies = this.flatSpeciesService.findAllBySecondaryName(networkEntityUUID, nodeSymbol);
			if (secondaryNameFlatSpecies != null && !secondaryNameFlatSpecies.isEmpty()) {
				secondaryNameFlatSpecies.forEach(f -> foundEntityUUIDs.add(f.getEntityUUID()));
				return foundEntityUUIDs;
			} else {
				return null;
			}
		} else {
			foundEntityUUIDs.add(flatSpeciesEntityUUID);
			return foundEntityUUIDs;
		}
	}
	
	/**
	 * Retrieve <a href="#{@link}">{@link FlatSpecies}</a> entities for a symbol.
	 * Searches the nodeSymbol in the Network given. If not found directly, searches the full model
	 * for occurrences of this symbol and finds the <a href="#{@link}">{@link FlatSpecies}</a> 
	 * that is derived from it. The symbol on the <a href="#{@link}">{@link FlatSpecies}</a> might differ,
	 * but it is guaranteed, that the <a href="#{@link}">{@link FlatSpecies}</a> - entityUUID returned
	 * is derived from a <a href="#{@link}">{@link SBMLSpecies}</a> that has that symbol connected to it 
	 * through a <a href="#{@link}">{@link BiomodelsQualifier}</a> relationship.
	 * 
	 * @param networkEntityUUID The entityUUID of the network to search in
	 * @param nodeSymbol The symbol to find
	 * @return List of <a href="#{@link}">{@link FlatSpecies}</a> for this symbol
	 */
	public List<FlatSpecies> getFlatSpeciesOfSymbolInNetwork(String networkEntityUUID, String nodeSymbol) {
		List<FlatSpecies> foundEntities= new ArrayList<>();
		// 0. Check whether we have that geneSymbol directly in the network
		Iterable<FlatSpecies> flatSpeciesForSymbol= this.findAllEntityForSymbolInNetwork(networkEntityUUID, nodeSymbol);
		if (flatSpeciesForSymbol != null && flatSpeciesForSymbol.iterator().hasNext()) {
			flatSpeciesForSymbol.forEach(foundEntities::add);
		}
		if (!foundEntities.isEmpty()) {
			return foundEntities;
		} else {
			// could not find the symbol, check secondary names
			List<FlatSpecies> secondaryNameFlatSpecies = this.flatSpeciesService.findAllBySecondaryName(networkEntityUUID, nodeSymbol);
			if (secondaryNameFlatSpecies != null && !secondaryNameFlatSpecies.isEmpty()) {
				secondaryNameFlatSpecies.forEach(foundEntities::add);
				return foundEntities;
			} else {
				return null;
			}
		}
		
	}
	
	/**
	 * Get List of <a href="#{@link}">{@link FlatEdge}</a> entities that make up the gene context in a network around a set of input genes
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @param genes A List of gene names to find the context for
	 * @param minSize The minimal step size to take for a context path
	 * @param maxSize The maximal step size to take for a context path
	 * @param terminateAtDrug Whether the context should always terminate in a Drug node (yes), or in any node (false). Attn: Requires MyDrug Nodes in the Network
	 * @param direction The direction to expand the context to (one of upstream, downstream, both)
	 * @return List of <a href="#{@link}">{@link FlatEdge}</a> entities that make up the context
	 */
	public List<FlatEdge> getNetworkContextFlatEdges(String networkEntityUUID, List<String> genes, int minSize,
			int maxSize, String terminateAt, String direction, String weightproperty) {
		return this.contextService.getNetworkContextFlatEdges(networkEntityUUID, genes, minSize, maxSize, terminateAt, direction, weightproperty);
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
	 * Get the node labels of a <a href="#{@link}">{@link MappingNode}</a> with entityUUID
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Set of Strings with node labels
	 */
	public Set<String> getNetworkNodeLabels(String networkEntityUUID) {
		return this.getNetworkNodeLabels(this.getNetworkNodes(networkEntityUUID));
	}

	/**
	 * Get the node labels of a collection of <a href="#{@link}">{@link FlatSpecies}</a> 
	 * @param networkNodes The collection of <a href="#{@link}">{@link FlatSpecies}</a> 
	 * @return Set of Strings with node labels
	 */
	public Set<String> getNetworkNodeLabels(Iterable<FlatSpecies> networkNodes) {
		Set<String> nodeLabels = new HashSet<>();
		for (FlatSpecies node : networkNodes) {
			nodeLabels.addAll(node.getLabels());
		}
		return nodeLabels;
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
	 * Create a NetworkInventoryItem for the network represented by the <a href="#{@link}">{@link MappingNode}</a> with entityUUID
	 * 
	 * @param entityUUID The entityUUID of the MappingNode to get the <a href="#{@link}">{@link NetworkInventoryItem}</a> for
	 * @return <a href="#{@link}">{@link NetworkInventoryItem}</a>
	 */
	public NetworkInventoryItem getNetworkInventoryItem(String entityUUID) {
		MappingNode mapping = this.mappingNodeService.findByEntityUUID(entityUUID);
		return getNetworkIventoryItem(mapping);
	}

	public void updateMappingNodeMetadata(MappingNode mappingNodeForFlatEdges) {
		// updateMappingNode
		String networkEntityUUID = mappingNodeForFlatEdges.getEntityUUID();
		mappingNodeForFlatEdges.setMappingNodeSymbols(this.getNetworkNodeSymbols(networkEntityUUID));
		mappingNodeForFlatEdges.setMappingNodeTypes(this.getNetworkNodeTypes(networkEntityUUID));
		mappingNodeForFlatEdges.setMappingRelationSymbols(this.getNetworkRelationSymbols(networkEntityUUID));
		mappingNodeForFlatEdges.setMappingRelationTypes(this.getNetworkRelationTypes(networkEntityUUID));
		mappingNodeForFlatEdges.addWarehouseAnnotation("creationendtime", Instant.now().toString());
		mappingNodeForFlatEdges.addWarehouseAnnotation("numberofnodes", String.valueOf(this.getNumberOfNetworkNodes(networkEntityUUID)));
		mappingNodeForFlatEdges.addWarehouseAnnotation("numberofrelations", String.valueOf(this.getNumberOfNetworkRelations(networkEntityUUID)));
		mappingNodeForFlatEdges.setActive(true);
	}

	/**
	 * Build a mapping name according to given options
	 * @param nameOrPrefix The name or prefix to set as name
	 * @param isPrefixName Whether the given name is prefix or full name
	 * @param currentName The name the prefix should be prefixed to, if applicable
	 * @return The resulting name 
	 */
	private String buildMappingName(String nameOrPrefix, boolean isPrefixName, String currentName) {
		String newMappingName;
		if (isPrefixName) {
			newMappingName = (nameOrPrefix.endsWith("_") ? nameOrPrefix : nameOrPrefix + "_") + currentName;
		} else {
			newMappingName = nameOrPrefix;
		}
		return newMappingName;
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
			this.warehouseGraphService.connect(mappingNode, newFlatSpeciesIterator.next(), WarehouseGraphEdgeType.CONTAINS, false);
		}
	}

	/**
	 * Connect a set of <a href="#{@link}">{@link FlatEdge}</a> entities (or rather the <a href="#{@link}">{@link FlatSpecies}</a> within) to a <a href="#{@link}">{@link MappingNode}</a>
	 * @param mappingNode The <a href="#{@link}">{@link MappingNode}</a> to connect to
	 * @param flatEdges The Iterable of <a href="#{@link}">{@link FlatEdge}</a> entities to connect
	 */
	private void connectContainsFlatEdgeSpecies(MappingNode mappingNode, Iterable<FlatEdge> flatEdges) {
		Iterator<FlatEdge> newFlatEdgeIterator = flatEdges.iterator();
		Set<String> seenSpecies = new HashSet<>();
		while (newFlatEdgeIterator.hasNext()) {
			FlatEdge currentEdge = newFlatEdgeIterator.next();
			if (!seenSpecies.contains(currentEdge.getInputFlatSpecies().getEntityUUID())) {
				this.warehouseGraphService.connect(mappingNode, currentEdge.getInputFlatSpecies(), WarehouseGraphEdgeType.CONTAINS, false);
				seenSpecies.add(currentEdge.getInputFlatSpecies().getEntityUUID());
			}
			if (!seenSpecies.contains(currentEdge.getOutputFlatSpecies().getEntityUUID())) {
				this.warehouseGraphService.connect(mappingNode, currentEdge.getOutputFlatSpecies(), WarehouseGraphEdgeType.CONTAINS, false);
				seenSpecies.add(currentEdge.getOutputFlatSpecies().getEntityUUID());
			}
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
		item.setNetworkMappingType(mapping.getMappingType().toString());
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
			item.setNumberOfNodes(Integer.valueOf((String) warehouseMap.get("numberofnodes")));
			item.setNumberOfRelations(Integer.valueOf((String) warehouseMap.get("numberofrelations")));
		} catch (NullPointerException e) {
			log.info("Mapping " + mapping.getMappingName() + " (" + mapping.getEntityUUID()
					+ ") does not have the warehouse-Properties set");
		} catch (Exception e) {
			log.info("Mapping " + mapping.getMappingName() + " (" + mapping.getEntityUUID()
					+ ") could not deliver number of nodes or relations due to:" + e.toString());
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
