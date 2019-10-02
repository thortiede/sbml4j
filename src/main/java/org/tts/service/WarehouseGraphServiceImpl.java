package org.tts.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.controller.WarehouseController;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Input.PathwayCollectionCreationItem;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.NodeNodeEdge;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.api.Output.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.OutputType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.common.Organism;
import org.tts.model.common.SBMLCompartment;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLQualSpeciesGroup;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.common.SBMLSpeciesGroup;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.full.SBMLReaction;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.simple.SBMLSimpleTransition;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.model.warehouse.WarehouseGraphEdge;
import org.tts.model.warehouse.WarehouseGraphNode;
import org.tts.repository.common.ContentGraphNodeRepository;
import org.tts.repository.flat.FlatSpeciesRepository;
import org.tts.repository.warehouse.DatabaseNodeRepository;
import org.tts.repository.warehouse.FileNodeRepository;
import org.tts.repository.warehouse.MappingNodeRepository;
import org.tts.repository.warehouse.PathwayCollectionNodeRepository;
import org.tts.repository.warehouse.PathwayNodeRepository;
import org.tts.repository.warehouse.WarehouseGraphEdgeRepository;
import org.tts.repository.warehouse.WarehouseGraphNodeRepository;

@Service
public class WarehouseGraphServiceImpl implements WarehouseGraphService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
	@Autowired
	WarehouseGraphNodeRepository warehouseGraphNodeRepository;
	
	@Autowired
	FileNodeRepository fileNodeRepository;
	
	@Autowired
	DatabaseNodeRepository databaseNodeRepository;
	
	@Autowired
	PathwayNodeRepository pathwayNodeRepository;
	
	@Autowired
	MappingNodeRepository mappingNodeRepository;

	@Autowired
	ContentGraphNodeRepository contentGraphNodeRepository;
	
	@Autowired
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;

	@Autowired
	WarehouseGraphEdgeRepository warehouseGraphEdgeRepository;

	@Autowired
	UtilityService utilityService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	PathwayCollectionNodeRepository pathwayCollectionNodeRepository;
	
	@Autowired
	FlatSpeciesRepository flatSpeciesRepository;
	
	@Override
	public DatabaseNode getDatabaseNode(String source, String sourceVersion, Organism org,
			Map<String, String> matchingAttributes) {
		List<DatabaseNode> databaseNodeList = this.databaseNodeRepository.findBySourceAndSourceVersion(source, sourceVersion);
		if (databaseNodeList.isEmpty()) {
			return null;
		} else if (databaseNodeList.size() == 1) {
			return databaseNodeList.get(0);
		} else {
			for (DatabaseNode database : databaseNodeList) {
				if (database.getOrganism().getOrgCode().equals(org.getOrgCode())) {
					return database;
				}
			}
			return null;
		}
	}

	@Override
	public DatabaseNode createDatabaseNode(String source, String sourceVersion, Organism org,
			Map<String, String> matchingAttributes) {
		DatabaseNode database = new DatabaseNode();
		sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(database);
		database.setOrganism(org);
		database.setSource(source);
		database.setSourceVersion(sourceVersion);
		//database.setMatchingAttributes(matchingAttributes);
		
		return this.databaseNodeRepository.save(database);
	}
	
	

	@Override
	public boolean fileNodeExists(FileNodeType fileNodeType, Organism org, String filename) {
		
		if (getFileNode(fileNodeType, org, filename) != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public FileNode createFileNode(FileNodeType fileNodeType, Organism org, String filename, byte[] filecontent) {
		FileNode newFileNode = new FileNode();
		this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newFileNode);
		newFileNode.setFileNodeType(fileNodeType);
		newFileNode.setFilename(filename);
		newFileNode.setOrganism(org);
		
		
		//try (BufferedReader br = new BufferedReader(new InputStreamReader(filecontent)) {
		if(filecontent != null) {
			newFileNode.setFilecontent( filecontent);
		} else {
			newFileNode.setFilecontent(new byte[0]);
		}
		return this.fileNodeRepository.save(newFileNode);
	}

	@Override
	public FileNode getFileNode(FileNodeType fileNodeType, Organism org, String originalFilename) {
		// TODO: Use all function arguments to check
		List<FileNode> filenodes = this.fileNodeRepository.findByFileNodeTypeAndFilename(fileNodeType, originalFilename);
		for (FileNode node : filenodes) {
			if(node.getOrganism().equals(org)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * connect two warehouse entities. 
	 * Always leaves the two entities connected with that edgetype, whether they already have been connected or not
	 * @param source the startNode of the edge
	 * @param target the endNode of the edge
	 * @param edgetype the WarehouseGraphEdgeType
	 * @return true if a new connection is made, false if the entities were already connected with that edgetype
	 */
	@Override
	public boolean connect(ProvenanceEntity source, ProvenanceEntity target,
			WarehouseGraphEdgeType edgetype) {

		if(this.warehouseGraphNodeRepository.areWarehouseEntitiesConnectedWithWarehouseGraphEdgeType(
									source.getEntityUUID(), 
									target.getEntityUUID(), 
									edgetype)) {
			return false;
		}
		
		WarehouseGraphEdge newEdge = new WarehouseGraphEdge();
		this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newEdge);
		newEdge.setWarehouseGraphEdgeType(edgetype);
		newEdge.setStartNode(source);
		newEdge.setEndNode(target);
		
		this.warehouseGraphEdgeRepository.save(newEdge, 0);
		return true;
		
	}

	@Override
	public PathwayNode createPathwayNode(String idString, String nameString, Organism org) {
		PathwayNode newPathwayNode = new PathwayNode();
		this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newPathwayNode);
		newPathwayNode.setOrganism(org);
		newPathwayNode.setPathwayIdString(idString);
		newPathwayNode.setPathwayNameString(nameString);
		return this.pathwayNodeRepository.save(newPathwayNode);
	}

	@Override
	public MappingNode createMappingNode(WarehouseGraphNode parent, NetworkMappingType type, String mappingName) {
		MappingNode newMappingNode = new MappingNode();
		this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newMappingNode);
		newMappingNode.setOrganism(parent.getOrganism());
		newMappingNode.setMappingType(type);
		newMappingNode.setMappingName(mappingName);
		return this.mappingNodeRepository.save(newMappingNode);
	}

	@Override
	public WarehouseGraphNode saveWarehouseGraphNodeEntity(WarehouseGraphNode node, int depth) {
		return this.warehouseGraphNodeRepository.save(node, depth);
	}
	
	@Override
	public List<PathwayInventoryItem> getListofPathwayInventory(String username) {
		List<PathwayInventoryItem> pathwayInventoryItemList = new ArrayList<>();
		for (PathwayNode pathwayNode : this.pathwayNodeRepository.findAllPathwaysAttributedToUser(username)) {
			PathwayInventoryItem item = new PathwayInventoryItem();
			item.setEntityUUID(pathwayNode.getEntityUUID());
			item.setPathwayId(pathwayNode.getPathwayIdString());
			item.setName(pathwayNode.getPathwayNameString());
			item.setOrganismCode(((Organism)this.warehouseGraphNodeRepository.findOrganismForWarehouseGraphNode(pathwayNode.getEntityUUID())).getOrgCode());
			//DatabaseNode sourceEntity = this.findSource(pathwayNode);
			//item.setSource(sourceEntity.getSource());
			//item.setSourceVersion(sourceEntity.getSourceVersion());
			item.setWarehouseGraphNodeType(WarehouseGraphNodeType.PATHWAY);
			List<ProvenanceEntity> pathwayNodes = this.warehouseGraphNodeRepository.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.CONTAINS, pathwayNode.getEntityUUID());
			//item.setNumberOfNodes(pathwayNodes.size());
			
			for (ProvenanceEntity node : pathwayNodes) {
				if (node.getClass() == SBMLSpecies.class || node.getClass() == SBMLSpeciesGroup.class) {
					item.addNodeType(this.utilityService.translateSBOString(((SBMLSBaseEntity)node).getsBaseSboTerm()));
					item.increaseNodeCounter();
				}
				else if (node.getClass() == SBMLSimpleTransition.class) {
					item.addTransitionType(this.utilityService.translateSBOString(((SBMLSBaseEntity)node).getsBaseSboTerm()));
					item.increaseTransitionCounter();
				} else if (node.getClass() == SBMLReaction.class) {
					item.increaseReactionCounter();
				} else if (node.getClass() == SBMLCompartment.class) {
					item.addCompartment(((SBMLCompartment)node).getsBaseName());
					item.increaseComparmentCounter();
				} else if(node.getClass() == SBMLQualSpecies.class || node.getClass() == SBMLQualSpeciesGroup.class) {
					// there is always a complementary SBMLSpecies or SBMLSpeciesGroup, so we can ignore those
					logger.debug("Found QualSpecies or QualSpeciesGroup, ignoring");
				} else {
					logger.warn("Node with entityUUID: " + node.getEntityUUID() + " has unexpected NodeType");
				}
			}
			// now add a link
			item.add(linkTo(methodOn(WarehouseController.class).getPathwayContents(username, pathwayNode.getEntityUUID())).withSelfRel());
			
			pathwayInventoryItemList.add(item);
		}
		
		
		return pathwayInventoryItemList;
	}

	@Override
	public List<ProvenanceEntity> getPathwayContents(String username, String entityUUID) {
		// again, for now ignore username
		return this.warehouseGraphNodeRepository.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.CONTAINS, entityUUID);
	}

	@Override
	public PathwayNode getPathwayNode(String username, String entityUUID) {
		// Do this if we want to restrict users to only see their own pathways.
		//return this.pathwayNodeRepository.findPathwayAttributedToUser(username, entityUUID);
		// for now, we ignore the username:
		return this.pathwayNodeRepository.findByEntityUUID(entityUUID);
	}

	@Override
	public WarehouseInventoryItem getWarehouseInventoryItem(WarehouseGraphNode warehouseGraphNode) {
		WarehouseInventoryItem item = new WarehouseInventoryItem();
		item.setEntityUUID(warehouseGraphNode.getEntityUUID());
		//item.setSource(findSource(warehouseGraphNode).getSource());
		//item.setSourceVersion(findSource(warehouseGraphNode).getSourceVersion());
		item.setOrganismCode(((Organism)this.warehouseGraphNodeRepository.findOrganismForWarehouseGraphNode(warehouseGraphNode.getEntityUUID())).getOrgCode());
		if(warehouseGraphNode.getClass() == MappingNode.class) {
			item.setWarehouseGraphNodeType(WarehouseGraphNodeType.MAPPING);
			item.setName(((MappingNode)warehouseGraphNode).getMappingName());
		}
		return item;
	}

	@Override
	public DatabaseNode getDatabaseNode(String databaseEntityUUID) {
		return this.databaseNodeRepository.findByEntityUUID(databaseEntityUUID);
	}

	
	

	private DatabaseNode findSource(ProvenanceEntity node) {
		if(node.getClass() == DatabaseNode.class) {
			return (DatabaseNode) node;
		} else {
			return findSource(this.provenanceGraphService.findByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasDerivedFrom, node.getEntityUUID()));
		}
	}

	@Override
	public PathwayCollectionNode createPathwayCollection(PathwayCollectionCreationItem pathwayCollectionCreationItem, DatabaseNode databaseNode,
			ProvenanceGraphActivityNode createPathwayCollectionGraphActivityNode, ProvenanceGraphAgentNode agentNode) {
		// create knowledgeGraph Node
		PathwayCollectionNode pathwayCollectionNode = this.createPathwayCollectionNode(pathwayCollectionCreationItem.getPathwayNodeIdString(), pathwayCollectionCreationItem.getName(), databaseNode.getOrganism());
		// TODO how to mark it as a collection?
		
		// connect provenance and to database node
		this.provenanceGraphService.connect(pathwayCollectionNode, databaseNode, ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(pathwayCollectionNode, createPathwayCollectionGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
		this.provenanceGraphService.connect(pathwayCollectionNode, agentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		// take all listed pathways
		for (String pathwayEntityUUID : pathwayCollectionCreationItem.getSourcePathwayEntityUUIDs()) {
			// add entities of pathway to knowledgegraphNode
			PathwayNode pathway = this.pathwayNodeRepository.findByEntityUUID(pathwayEntityUUID);
			if(pathway != null) {// && findSource(pathway).getEntityUUID() == databaseNode.getEntityUUID()) {
				this.provenanceGraphService.connect(pathwayCollectionNode, pathway, ProvenanceGraphEdgeType.hadMember);
				this.provenanceGraphService.connect(createPathwayCollectionGraphActivityNode, pathway, ProvenanceGraphEdgeType.used);
			}
		}
		return pathwayCollectionNode;
	}

	@Override
	public PathwayCollectionNode createPathwayCollectionNode(String pathwayCollectionName, String pathwayCollectionDescription,
			Organism organism) {
		for(PathwayCollectionNode pwc : this.pathwayCollectionNodeRepository.findAllByPathwayCollectionName(pathwayCollectionName)) {
			if(pwc.getOrganism().equals(organism) && pwc.getPathwayCollectionDescription().equals(pathwayCollectionDescription)) {
				return pwc;
			} 
		}
		PathwayCollectionNode pathwayCollectionNode = new PathwayCollectionNode();
		this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(pathwayCollectionNode);
		pathwayCollectionNode.setPathwayCollectionName(pathwayCollectionName);
		pathwayCollectionNode.setPathwayCollectionDescription(pathwayCollectionDescription);
		pathwayCollectionNode.setOrganism(organism);
		return this.pathwayCollectionNodeRepository.save(pathwayCollectionNode);
	
	}

	@Override
	public Map<String, Integer> buildPathwayFromCollection(PathwayNode pathwayNode, PathwayCollectionNode pathwayCollectionNode,
			ProvenanceGraphActivityNode buildPathwayFromCollectionActivityNode, ProvenanceGraphAgentNode agentNode) {
		
		int entityCounter = 0;
		int connectionCounter = 0;
		if(this.provenanceGraphService.areProvenanceEntitiesConnectedWithProvenanceEdgeType(pathwayNode.getEntityUUID(), 
																							pathwayCollectionNode.getEntityUUID(), 
																							ProvenanceGraphEdgeType.wasDerivedFrom)) {
			for(ProvenanceEntity pathwayEntity : this.provenanceGraphService.
											findAllByProvenanceGraphEdgeTypeAndStartNode(
													ProvenanceGraphEdgeType.hadMember, 
													pathwayCollectionNode.getEntityUUID())) {
				// get all element in the pathway (Connected with CONTAINS)
				for (ProvenanceEntity entity : this.warehouseGraphNodeRepository.
										findAllByWarehouseGraphEdgeTypeAndStartNode(
													WarehouseGraphEdgeType.CONTAINS, 
													pathwayEntity.getEntityUUID())) {
					// connect them to the new pathwayNode
					if(this.connect(pathwayNode, entity, WarehouseGraphEdgeType.CONTAINS)) {
						++connectionCounter;
					}
					++entityCounter;
					// Possibility to connect the activity to all entities here, but might just create ALOT of edges that do not help anyone.
					// might become interesting, if we introduce filtering on nodes here, aka. not build the full pathway, but just a subset of the pathways
					// that the collection holds
					// but that better be done from the pathway that is created here in a second step with a new activity.
				}
			}
		}
		Map<String, Integer> counterMap = new HashMap<>();
		counterMap.put("entity", entityCounter);
		counterMap.put("connection", connectionCounter);
		return counterMap;
		
	}

	@Override
	public List<String> getListofPathwayUUIDs() {
		List<String> uuids = new ArrayList<>();
		for (PathwayNode node : this.pathwayNodeRepository.findAll()) {
			uuids.add(node.getEntityUUID());
		}
		return uuids;
	}

	@Override
	public NetworkInventoryItem getNetworkInventoryItem(String entityUUID) {
		MappingNode mapping = this.mappingNodeRepository.findByEntityUUID(entityUUID);
		return getNetworkIventoryItem(mapping);
	}
	
	private NetworkInventoryItem getNetworkIventoryItem(MappingNode mapping) {
		NetworkInventoryItem item = new NetworkInventoryItem();
		item.setWarehouseGraphNodeType(WarehouseGraphNodeType.MAPPING);
		item.setEntityUUID(mapping.getEntityUUID());
		//item.setSource(findSource(mapping).getSource());
		//item.setSourceVersion(findSource(mapping).getSourceVersion());
		item.setName(mapping.getMappingName());
		item.setOrganismCode(((Organism)this.warehouseGraphNodeRepository.findOrganismForWarehouseGraphNode(mapping.getEntityUUID())).getOrgCode());
		item.setNetworkMappingType(mapping.getMappingType());
		if (mapping.getMappingNodeTypes() != null) {
			for (String sboTerm : mapping.getMappingNodeTypes()) {
				item.addNodeType(this.utilityService.translateSBOString(sboTerm));
			}
		}
		//item.setNodeTypes(mapping.getMappingNodeTypes());
		//item.setRelationTypes(mapping.getMappingRelationTypes());
		if (mapping.getMappingRelationTypes() != null) {
			for (String sboTerm : mapping.getMappingRelationTypes()) {
				item.addRelationType(this.utilityService.translateSBOString(sboTerm));
			}
		}
		
		try {
			Map<String, Object> warehouseMap = mapping.getWarehouse();
			
			item.setNumberOfNodes(Integer.valueOf(((String)warehouseMap.get("numberofnodes"))));
			item.setNumberOfRelations(Integer.valueOf((String)warehouseMap.get("numberofrelations")));
		} catch (Exception e) {
			logger.info("Mapping " + mapping.getMappingName() + " (" + mapping.getEntityUUID() + ") does not have the warehouse-Properties set");
		}
		for (OutputType type : OutputType.values()) {
			item.add(linkTo(methodOn(WarehouseController.class).getNetwork(item.getEntityUUID(), "standard", type.name())).withRel(type.name()));
		}
		// add link to the item detail:
		item.add(linkTo(methodOn(WarehouseController.class).getNetworkInventoryDetail(item.getEntityUUID())).withRel("InventoryItemDetail"));
		
		// add link to the item FilterOptions
		item.add(linkTo(methodOn(WarehouseController.class).getNetworkFilterOptions(item.getEntityUUID())).withRel("FilterOptions"));
		return item;
	}

	@Override
	public List<NetworkInventoryItem> getListOfNetworkInventoryItems(String username) {
		List<NetworkInventoryItem> inventory = new ArrayList<>();
		// get all mapping nodes
		List<String> usernames = new ArrayList<>();
		usernames.add(username);
		usernames.add("All");
		for (MappingNode mapping : this.mappingNodeRepository.findAllFromUsers(usernames)) {
			// create an inventory item for each of them and fill it.
		
			inventory.add(this.getNetworkIventoryItem(mapping));
		}
		return inventory;
		
	}

	@Override
	public NodeEdgeList flatSpeciesListToNEL(List<FlatSpecies> flatSpeciesList) {
		Set<String> nodeSymbols = new HashSet<>();
		for (FlatSpecies node : flatSpeciesList) {
			nodeSymbols.add(node.getSymbol());
		}
		NodeEdgeList nel = new NodeEdgeList();
		for (FlatSpecies node1 : flatSpeciesList) {
			//FlatSpecies node1 = this.flatSpeciesRepository.findByEntityUUID(species.getEntityUUID());
			node1.getAllRelatedSpecies().forEach((relationType, node2List) -> {
				for (FlatSpecies node2 : node2List) {
					if(nodeSymbols.contains(node2.getSymbol())) {
					//logger.info("Working on Node " + species.getSymbol() + " connected with " + relationType + " to " + node2.getSymbol());
						nel.addListEntry(node1.getSymbol(), node1.getSimpleModelEntityUUID(), node2.getSymbol(), node2.getSimpleModelEntityUUID(), this.utilityService.translateSBOString(relationType));
					}
				}
			});
		}
		nel.setName("Context transient");
		nel.setListId(-1L);
		return nel;
		
	}
	
	@Override
	public NodeEdgeList getNetwork(String mappingNodeEntityUUID, String method) {
		//MappingNode mapping = this.mappingNodeRepository.findByEntityUUID(mappingNodeEntityUUID);
		Instant start = Instant.now();
		NodeEdgeList mappingNEL = new NodeEdgeList();
		Instant mappingNelTime = Instant.now();
		Instant insideLoggedTime;
		Instant insideRetrievedTime;
		Instant insideretrievedAndSetTime;
		if (method.equals("directed")) {
			logger.info("Using directed method to get MappingContent for mapping " + mappingNodeEntityUUID);
			insideLoggedTime = Instant.now();
			List<NodeNodeEdge> mappingList = this.mappingNodeRepository.getMappingContentDirected(mappingNodeEntityUUID);
			insideRetrievedTime = Instant.now();
			mappingNEL.setNodeNodeEdgeList(mappingList);
			insideretrievedAndSetTime = Instant.now();
			mappingNEL.setName(mappingNodeEntityUUID);
			mappingNEL.setListId(-1L);
			
		} else if (method.equals("undirected")) {
			logger.info("Using undirected method to get MappingContent for mapping " + mappingNodeEntityUUID);
			insideLoggedTime = Instant.now();
			List<NodeNodeEdge> mappingList = this.mappingNodeRepository.getMappingContentUnDirected(mappingNodeEntityUUID);
			insideRetrievedTime = Instant.now();
			mappingNEL.setNodeNodeEdgeList(mappingList);
			insideretrievedAndSetTime = Instant.now();
			mappingNEL.setName(mappingNodeEntityUUID);
			mappingNEL.setListId(-1L);
			
		} else {
			logger.info("Using standard method to get MappingContent for mapping " + mappingNodeEntityUUID);
			insideLoggedTime = Instant.now();
			List<FlatSpecies> mappingFlatSpecies = this.mappingNodeRepository.getMappingFlatSpecies(mappingNodeEntityUUID);
			insideRetrievedTime = Instant.now();
			logger.info("Gathered " + mappingFlatSpecies.size() + " Flat Species");
			
			for (FlatSpecies species : mappingFlatSpecies) {
				FlatSpecies node1 = this.flatSpeciesRepository.findByEntityUUID(species.getEntityUUID());
				node1.getAllRelatedSpecies().forEach((relationType, node2List) -> {
					for (FlatSpecies node2 : node2List) {
						//logger.info("Working on Node " + species.getSymbol() + " connected with " + relationType + " to " + node2.getSymbol());
						mappingNEL.addListEntry(node1.getSymbol(), node1.getSimpleModelEntityUUID(), node2.getSymbol(), node2.getSimpleModelEntityUUID(), this.utilityService.translateSBOString(relationType));
					}
				});
			}
			insideretrievedAndSetTime = Instant.now();
		}
		Instant end = Instant.now();
		logger.info("Timing: " + Duration.between(start, mappingNelTime).toString() + 
				" / " + Duration.between(mappingNelTime, insideLoggedTime).toString() + 
				" / " + Duration.between(insideLoggedTime, insideRetrievedTime).toString() + 
				" / " + Duration.between(insideRetrievedTime, insideretrievedAndSetTime).toString() + 
				" / " + Duration.between(insideretrievedAndSetTime, end).toString());
		return mappingNEL;
	}

	@Override
	public List<String> getNetworkNodeSymbols(String entityUUID) {
		return this.getNetworkNodeSymbols(this.getNetworkNodes(entityUUID));
	}
	
	@Override
	public List<String> getNetworkNodeSymbols(List<FlatSpecies> networkNodes) {
		List<String> networkNodeNames = new ArrayList<>();
		for (FlatSpecies node : networkNodes) {
			networkNodeNames.add(node.getSymbol());
		}
		return networkNodeNames;
	}
	
	@Override
	public List<FlatSpecies> getNetworkNodes(String entityUUID) {
		
		return this.flatSpeciesRepository.findAllNetworkNodes(entityUUID);
	}

	@Override
	public MappingNode getMappingNode(String entityUUID) {
		return this.mappingNodeRepository.findByEntityUUID(entityUUID);
	}

	@Override
	public List<FlatSpecies> copyFlatSpeciesList(List<FlatSpecies> original) {
		List<FlatSpecies> newSpeciesList = new ArrayList<>(original.size());
		Map<String, FlatSpecies> oldUUIDToNewSpeciesMap = new HashMap<>();
		for(int i = 0; i != original.size(); i++) {
			//Map<String, List<String>> relationTypeOldUUIDListMap = new HashMap<>();
			FlatSpecies oldSpecies = original.get(i);
			FlatSpecies newSpecies = new FlatSpecies();
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newSpecies);
			newSpecies.setAnnotation(oldSpecies.getAnnotation());
			newSpecies.setLabels(oldSpecies.getLabels());
			newSpecies.setProvenance(oldSpecies.getProvenance());
			newSpecies.setSboTerm(oldSpecies.getSboTerm());
			newSpecies.setSimpleModelEntityUUID(oldSpecies.getSimpleModelEntityUUID());
			newSpecies.setSymbol(oldSpecies.getSymbol());
			//newSpecies.addRelatedSpecies(oldSpecies.getAllRelatedSpecies());
			oldUUIDToNewSpeciesMap.put(oldSpecies.getEntityUUID(), newSpecies);
			newSpeciesList.add(i, newSpecies);
		}
		for(int j = 0; j!= original.size(); j++) {
			FlatSpecies oldSpecies = original.get(j);
			FlatSpecies newSpecies = newSpeciesList.get(j);
			oldSpecies.getAllRelatedSpecies().forEach((relation, speciesList)-> {
				for (FlatSpecies oldRelatedSpecies : speciesList) {
					newSpecies.addRelatedSpecies(oldUUIDToNewSpeciesMap.get(oldRelatedSpecies.getEntityUUID()), relation);
				}
			});
		}
		
		return newSpeciesList;
	}

	@Override
	public List<FlatSpecies> copyAndFilterFlatSpeciesList(List<FlatSpecies> originalFlatSpeciesList, FilterOptions options) {
		final List<String> relationTypes = options.getRelationTypes();
		/*List<String> nodeTypes =  new ArrayList<>();
		for (String type : options.getNodeTypes()) {
			nodeTypes.add(this.utilityService.translateToSBOString(type));
		}*/
		List<String> nodeTypes = options.getNodeTypes();
		List<String> nodeSymbols = options.getNodeSymbols();
		List<FlatSpecies> newSpeciesList = new ArrayList<>(originalFlatSpeciesList.size());
		Map<String, FlatSpecies> oldUUIDToNewSpeciesMap = new HashMap<>();
		Set<String> oldSkippedSpecies = new HashSet<>();
		Set<String> newConnectedSpecies = new HashSet<>();
		for(int i = 0; i != originalFlatSpeciesList.size(); i++) {
			//Map<String, List<String>> relationTypeOldUUIDListMap = new HashMap<>();
			FlatSpecies oldSpecies = originalFlatSpeciesList.get(i);
			if(nodeSymbols != null && (!nodeSymbols.contains(oldSpecies.getSymbol()) || (nodeTypes != null && !nodeTypes.contains(this.utilityService.translateSBOString(oldSpecies.getSboTerm()))))) {
				oldSkippedSpecies.add(oldSpecies.getEntityUUID());
				newSpeciesList.add(i, null);
				continue;
			}
			FlatSpecies newSpecies = new FlatSpecies();
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newSpecies);
			newSpecies.setAnnotation(oldSpecies.getAnnotation());
			newSpecies.setLabels(oldSpecies.getLabels());
			newSpecies.setProvenance(oldSpecies.getProvenance());
			newSpecies.setSboTerm(oldSpecies.getSboTerm());
			newSpecies.setSimpleModelEntityUUID(oldSpecies.getSimpleModelEntityUUID());
			newSpecies.setSymbol(oldSpecies.getSymbol());
			//newSpecies.addRelatedSpecies(oldSpecies.getAllRelatedSpecies());
			oldUUIDToNewSpeciesMap.put(oldSpecies.getEntityUUID(), newSpecies);
			newSpeciesList.add(i, newSpecies);
		}
		for(int j = 0; j!= originalFlatSpeciesList.size(); j++) {
			FlatSpecies oldSpecies = originalFlatSpeciesList.get(j);
			FlatSpecies newSpecies = newSpeciesList.get(j);
			if(!(newSpecies == null)) {
				oldSpecies.getAllRelatedSpecies().forEach((relation, speciesList)-> {
					if(relationTypes.contains(this.utilityService.translateSBOString(relation))) {
						for (FlatSpecies oldRelatedSpecies : speciesList) {
							if (!oldSkippedSpecies.contains(oldRelatedSpecies.getEntityUUID())) {
								newSpecies.addRelatedSpecies(oldUUIDToNewSpeciesMap.get(oldRelatedSpecies.getEntityUUID()), relation);
								newConnectedSpecies.add(newSpecies.getEntityUUID());
								newConnectedSpecies.add(oldUUIDToNewSpeciesMap.get(oldRelatedSpecies.getEntityUUID()).getEntityUUID());
							}
						}
					}
				});
				
			}
		}
		// check for dangling species (that did not get connected with a relation)
		for (int i = 0; i != newSpeciesList.size(); i++) {
			FlatSpecies species = newSpeciesList.get(i);
			if(species != null && !newConnectedSpecies.contains(species.getEntityUUID())) {
				logger.debug("Removing Species with parent " + originalFlatSpeciesList.get(i).getEntityUUID());
				// be sure to keep the list the same length
				newSpeciesList.set(i, null);
			}
		}
		return newSpeciesList;
	}

	@Override
	public FilterOptions getFilterOptions(WarehouseGraphNodeType nodeType, String entityUUID) {
		if(nodeType.equals(WarehouseGraphNodeType.MAPPING)) {
			return this.getFilterOptions(this.mappingNodeRepository.findByEntityUUID(entityUUID));
		} else {
			return null; // TODO
		}
		
	}

	private FilterOptions getFilterOptions(MappingNode mappingNode) {
		FilterOptions filterOptions = new FilterOptions();
		filterOptions.setMappingUuid(mappingNode.getEntityUUID());
		filterOptions.setNetworkType(mappingNode.getMappingType());
		if(mappingNode.getMappingNodeSymbols() == null) {
			filterOptions.setNodeSymbols(getNetworkNodeSymbols(mappingNode.getEntityUUID()));
		} else {
			filterOptions.setNodeSymbols(new ArrayList<>(mappingNode.getMappingNodeSymbols()));
		}
		List<String> nodeTypes = new ArrayList<>();
		for (String sboTerm : mappingNode.getMappingNodeTypes()) {
			nodeTypes.add(this.utilityService.translateSBOString(sboTerm));
		}
		filterOptions.setNodeTypes(nodeTypes);
		List<String> relationTypes = new ArrayList<>();
		for (String sboTerm : mappingNode.getMappingRelationTypes()) {
			relationTypes.add(this.utilityService.translateSBOString(sboTerm));
		}
		filterOptions.setRelationTypes(relationTypes);
		return filterOptions;
	}

	@Override
	public List<FlatSpecies> createNetworkContext(List<FlatSpecies> oldSpeciesList, String parentUUID, FilterOptions options) {
		List<String> sourceSpeciesForContext = options.getNodeSymbols();
		String startNodeSymbol = sourceSpeciesForContext.get(0);
		String startNodeEntityUUID = "";
		for (FlatSpecies oldSpecies : oldSpeciesList) {
			if (oldSpecies.getSymbol() != null && oldSpecies.getSymbol().equals(startNodeSymbol)) {
				startNodeEntityUUID = oldSpecies.getEntityUUID();
				break;
			}
		}
		
		List<FlatSpecies> contextSpeciesList = findNetworkContext(startNodeEntityUUID, options);
		
		return contextSpeciesList;
	}

	/**
	 * @param startNodeEntityUUID
	 * @param relationTypesApocString
	 * @param minPathLength
	 * @param maxPathLength
	 * @return
	 */
	@Override
	public List<FlatSpecies> findNetworkContext(String startNodeEntityUUID, FilterOptions options) {
		String relationTypesApocString = "";
		for (String relationType : options.getRelationTypes()) {
			relationTypesApocString += relationType;
			relationTypesApocString += "|";
		}
		relationTypesApocString = relationTypesApocString.substring(0, relationTypesApocString.length()-1);
		int minPathLength = options.getMinSize();
		int maxPathLength = options.getMaxSize();
		List<FlatSpecies> contextSpeciesList = this.flatSpeciesRepository.findNetworkContext(startNodeEntityUUID, relationTypesApocString, minPathLength, maxPathLength);
		return contextSpeciesList;
	}

	@Override
	public String getMappingEntityUUID(String baseNetworkEntityUUID, String geneSymbol, int minSize, int maxSize) {
		MappingNode mappingNode = this.mappingNodeRepository.getByBaseNetworkEntityUUIDAndGeneSymbolAndMinSizeAndMaxSize(baseNetworkEntityUUID, geneSymbol, minSize, maxSize);
		return mappingNode != null ? mappingNode.getEntityUUID() : null;
		
	}

	@Override
	public boolean mappingForPathwayExists(String entityUUID) {
		List<ProvenanceEntity> possibleMappings = (List<ProvenanceEntity>) this.provenanceGraphService.findAllByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType.wasDerivedFrom, entityUUID);
		if(possibleMappings.size() < 1) {
			return false;
		} else {
			for (ProvenanceEntity entity : possibleMappings) {
				if(entity.getClass().equals(MappingNode.class)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public String findStartNode(String baseNetworkUUID, String geneSymbol) {
		return this.flatSpeciesRepository.findStartNodeEntityUUID(baseNetworkUUID, geneSymbol);
	}

	
}
