package org.tts.service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.controller.WarehouseController;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Input.PathwayCollectionCreationItem;
import org.tts.model.api.Output.FlatMappingReturnType;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.api.Output.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.common.GraphEnum.MappingStep;
import org.tts.model.common.GraphEnum.NetworkMappingType;
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
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.full.SBMLReaction;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.simple.SBMLSimpleReaction;
import org.tts.model.simple.SBMLSimpleTransition;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.model.warehouse.WarehouseGraphEdge;
import org.tts.model.warehouse.WarehouseGraphNode;
import org.tts.repository.common.ContentGraphNodeRepository;
import org.tts.repository.flat.FlatEdgeRepository;
import org.tts.repository.flat.FlatNetworkMappingRepository;
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

	@Autowired
	FlatNetworkMappingRepository flatNetworkMappingRepository;

	@Autowired
	FlatEdgeRepository flatEdgeRepository;
	
	@Autowired
	FlatEdgeService flatEdgeService;

	@Autowired
	Session session;

	@Override
	public DatabaseNode getDatabaseNode(String source, String sourceVersion, Organism org,
			Map<String, String> matchingAttributes) {
		List<DatabaseNode> databaseNodeList = this.databaseNodeRepository.findBySourceAndSourceVersion(source,
				sourceVersion);
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
		// database.setMatchingAttributes(matchingAttributes);

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

		// try (BufferedReader br = new BufferedReader(new
		// InputStreamReader(filecontent)) {
		// TODO: What to do with big files (like Recon3D being more than 26 MB). They slow down queries on the database alot
		//if (filecontent != null) {
		//	newFileNode.setFilecontent(filecontent);
		//} else {
		//	newFileNode.setFilecontent(new byte[0]);
		//}
		return this.fileNodeRepository.save(newFileNode);
	}

	@Override
	public FileNode getFileNode(FileNodeType fileNodeType, Organism org, String originalFilename) {
		// TODO: Use all function arguments to check
		List<FileNode> filenodes = this.fileNodeRepository.findByFileNodeTypeAndFilename(fileNodeType,
				originalFilename);
		for (FileNode node : filenodes) {
			if (node.getOrganism().equals(org)) {
				return node;
			}
		}
		return null;
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
	@Override
	public boolean connect(ProvenanceEntity source, ProvenanceEntity target, WarehouseGraphEdgeType edgetype) {

		if (this.warehouseGraphNodeRepository.areWarehouseEntitiesConnectedWithWarehouseGraphEdgeType(
				source.getEntityUUID(), target.getEntityUUID(), edgetype)) {
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
	
	/**
	 * connect a set of source provenance entities with one target entity. The entities are not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param sourceEntities the startNodes of the edges
	 * @param target the endNode of the edge
	 * @param edgetype the warehouseGraphEdgeType
	 */
	@Override
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
	@Override
	public void connect (ProvenanceEntity source, Iterable<ProvenanceEntity> targetEntities, WarehouseGraphEdgeType edgetype) {
		for (ProvenanceEntity target : targetEntities) {
			this.connect(source, target, edgetype);
		}
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
			item.setOrganismCode(((Organism) this.warehouseGraphNodeRepository
					.findOrganismForWarehouseGraphNode(pathwayNode.getEntityUUID())).getOrgCode());
			// DatabaseNode sourceEntity = this.findSource(pathwayNode);
			// item.setSource(sourceEntity.getSource());
			// item.setSourceVersion(sourceEntity.getSourceVersion());
			item.setWarehouseGraphNodeType(WarehouseGraphNodeType.PATHWAY);
			List<ProvenanceEntity> pathwayNodes = this.warehouseGraphNodeRepository
					.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.CONTAINS,
							pathwayNode.getEntityUUID());
			// item.setNumberOfNodes(pathwayNodes.size());

			for (ProvenanceEntity node : pathwayNodes) {
				if (node.getClass() == SBMLSpecies.class || node.getClass() == SBMLSpeciesGroup.class) {
					item.addNodeType(
							this.utilityService.translateSBOString(((SBMLSBaseEntity) node).getsBaseSboTerm()));
					item.increaseNodeCounter();
				} else if (node.getClass() == SBMLSimpleTransition.class) {
					item.addTransitionType(
							this.utilityService.translateSBOString(((SBMLSBaseEntity) node).getsBaseSboTerm()));
					item.increaseTransitionCounter();
				} else if (node.getClass() == SBMLReaction.class || node.getClass() == SBMLSimpleReaction.class) {
					item.increaseReactionCounter();
				} else if (node.getClass() == SBMLCompartment.class) {
					item.addCompartment(((SBMLCompartment) node).getsBaseName());
					item.increaseComparmentCounter();
				} else if (node.getClass() == SBMLQualSpecies.class || node.getClass() == SBMLQualSpeciesGroup.class) {
					// there is always a complementary SBMLSpecies or SBMLSpeciesGroup, so we can
					// ignore those
					logger.debug("Found QualSpecies or QualSpeciesGroup, ignoring");
				} else {
					logger.warn("Node with entityUUID: " + node.getEntityUUID() + " has unexpected NodeType");
				}
			}
			// now add a link
			item.add(linkTo(
					methodOn(WarehouseController.class).getPathwayContents(username, pathwayNode.getEntityUUID()))
							.withSelfRel());

			pathwayInventoryItemList.add(item);
		}

		return pathwayInventoryItemList;
	}

	@Override
	public List<ProvenanceEntity> getPathwayContents(String username, String entityUUID) {
		// again, for now ignore username
		return this.warehouseGraphNodeRepository
				.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.CONTAINS, entityUUID);
	}

	@Override
	public PathwayNode getPathwayNode(String username, String entityUUID) {
		// Do this if we want to restrict users to only see their own pathways.
		// return this.pathwayNodeRepository.findPathwayAttributedToUser(username,
		// entityUUID);
		// for now, we ignore the username:
		return this.pathwayNodeRepository.findByEntityUUID(entityUUID);
	}

	@Override
	public WarehouseInventoryItem getWarehouseInventoryItem(WarehouseGraphNode warehouseGraphNode) {
		WarehouseInventoryItem item = new WarehouseInventoryItem();
		item.setEntityUUID(warehouseGraphNode.getEntityUUID());
		// item.setSource(findSource(warehouseGraphNode).getSource());
		// item.setSourceVersion(findSource(warehouseGraphNode).getSourceVersion());
		item.setOrganismCode(((Organism) this.warehouseGraphNodeRepository
				.findOrganismForWarehouseGraphNode(warehouseGraphNode.getEntityUUID())).getOrgCode());
		if (warehouseGraphNode.getClass() == MappingNode.class) {
			item.setWarehouseGraphNodeType(WarehouseGraphNodeType.MAPPING);
			item.setName(((MappingNode) warehouseGraphNode).getMappingName());
		}
		return item;
	}

	@Override
	public DatabaseNode getDatabaseNode(String databaseEntityUUID) {
		return this.databaseNodeRepository.findByEntityUUID(databaseEntityUUID);
	}

	private DatabaseNode findSource(ProvenanceEntity node) {
		if (node.getClass() == DatabaseNode.class) {
			return (DatabaseNode) node;
		} else {
			return findSource(this.provenanceGraphService.findByProvenanceGraphEdgeTypeAndStartNode(
					ProvenanceGraphEdgeType.wasDerivedFrom, node.getEntityUUID()));
		}
	}

	@Override
	public PathwayCollectionNode createPathwayCollection(PathwayCollectionCreationItem pathwayCollectionCreationItem,
			DatabaseNode databaseNode, ProvenanceGraphActivityNode createPathwayCollectionGraphActivityNode,
			ProvenanceGraphAgentNode agentNode) {
		// create knowledgeGraph Node
		PathwayCollectionNode pathwayCollectionNode = this.createPathwayCollectionNode(
				pathwayCollectionCreationItem.getPathwayNodeIdString(), pathwayCollectionCreationItem.getName(),
				databaseNode.getOrganism());
		// TODO how to mark it as a collection?

		// connect provenance and to database node
		this.provenanceGraphService.connect(pathwayCollectionNode, databaseNode,
				ProvenanceGraphEdgeType.wasDerivedFrom);
		this.provenanceGraphService.connect(pathwayCollectionNode, createPathwayCollectionGraphActivityNode,
				ProvenanceGraphEdgeType.wasGeneratedBy);
		this.provenanceGraphService.connect(pathwayCollectionNode, agentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		// take all listed pathways
		for (String pathwayEntityUUID : pathwayCollectionCreationItem.getSourcePathwayEntityUUIDs()) {
			// add entities of pathway to knowledgegraphNode
			PathwayNode pathway = this.pathwayNodeRepository.findByEntityUUID(pathwayEntityUUID);
			if (pathway != null) {// && findSource(pathway).getEntityUUID() == databaseNode.getEntityUUID()) {
				this.provenanceGraphService.connect(pathwayCollectionNode, pathway, ProvenanceGraphEdgeType.hadMember);
				this.provenanceGraphService.connect(createPathwayCollectionGraphActivityNode, pathway,
						ProvenanceGraphEdgeType.used);
			}
		}
		return pathwayCollectionNode;
	}

	@Override
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
		this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(pathwayCollectionNode);
		pathwayCollectionNode.setPathwayCollectionName(pathwayCollectionName);
		pathwayCollectionNode.setPathwayCollectionDescription(pathwayCollectionDescription);
		pathwayCollectionNode.setOrganism(organism);
		return this.pathwayCollectionNodeRepository.save(pathwayCollectionNode);

	}

	@Override
	public Map<String, Integer> buildPathwayFromCollection(PathwayNode pathwayNode,
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
				for (ProvenanceEntity entity : this.warehouseGraphNodeRepository
						.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.CONTAINS,
								pathwayEntity.getEntityUUID())) {
					// connect them to the new pathwayNode
					if (this.connect(pathwayNode, entity, WarehouseGraphEdgeType.CONTAINS)) {
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
		item.setActive(mapping.isActive());
		// item.setSource(findSource(mapping).getSource());
		// item.setSourceVersion(findSource(mapping).getSourceVersion());
		item.setName(mapping.getMappingName());
		item.setOrganismCode(((Organism) this.warehouseGraphNodeRepository
				.findOrganismForWarehouseGraphNode(mapping.getEntityUUID())).getOrgCode());
		item.setNetworkMappingType(mapping.getMappingType());
		if (mapping.getMappingNodeTypes() != null) {
			for (String sboTerm : mapping.getMappingNodeTypes()) {
				item.addNodeType(this.utilityService.translateSBOString(sboTerm));
			}
		}
		// item.setNodeTypes(mapping.getMappingNodeTypes());
		// item.setRelationTypes(mapping.getMappingRelationTypes());
		if (mapping.getMappingRelationTypes() != null) {
			for (String type : mapping.getMappingRelationTypes()) {
				item.addRelationType(type);
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
		 * for (OutputType type : OutputType.values()) { item.add(linkTo(
		 * methodOn(WarehouseController.class).getNetwork(item.getEntityUUID(),
		 * "standard", type.name())) .withRel(type.name())); }
		 */
		// add link to the item detail:
		item.add(linkTo(methodOn(WarehouseController.class).getNetworkInventoryDetail(item.getEntityUUID()))
				.withRel("InventoryItemDetail"));

		// add link to the item FilterOptions
		item.add(linkTo(methodOn(WarehouseController.class).getNetworkFilterOptions(item.getEntityUUID()))
				.withRel("FilterOptions"));

		// add link to deleting the item (setting isactive = false
		item.add(linkTo(methodOn(WarehouseController.class).deactivateNetwork(item.getEntityUUID()))
				.withRel("Delete Mapping").withType("DELETE"));
		return item;
	}

	@Override
	public List<NetworkInventoryItem> getListOfNetworkInventoryItems(String username, boolean isActiveOnly) {
		List<NetworkInventoryItem> inventory = new ArrayList<>();
		// get all mapping nodes
		List<String> usernames = new ArrayList<>();
		usernames.add(username);
		usernames.add("All");
		List<MappingNode> mappings = new ArrayList<>();
		if (isActiveOnly) {
			mappings = this.mappingNodeRepository.findAllActiveFromUsers(usernames);
		} else {
			mappings = this.mappingNodeRepository.findAllFromUsers(usernames);
		}
		for (MappingNode mapping : mappings) {
			// create an inventory item for each of them and fill it.

			inventory.add(this.getNetworkIventoryItem(mapping));
		}
		return inventory;

	}

	/*
	 * @Override public NodeEdgeList flatSpeciesListToNEL(List<FlatSpecies>
	 * flatSpeciesList, String networkEntityUUID) { Set<String> nodeSymbols = new
	 * HashSet<>(); for (FlatSpecies node : flatSpeciesList) {
	 * nodeSymbols.add(node.getSymbol()); } NodeEdgeList nel = new NodeEdgeList();
	 * for (FlatSpecies node1 : flatSpeciesList) { //FlatSpecies node1 =
	 * this.flatSpeciesRepository.findByEntityUUID(species.getEntityUUID());
	 * node1.getAllRelatedSpecies().forEach((relationType, node2List) -> { for
	 * (FlatEdge node2Edge : node2List) {
	 * if(nodeSymbols.contains(node2Edge.getOutputFlatSpecies().getSymbol())) {
	 * //logger.info("Working on Node " + species.getSymbol() + " connected with " +
	 * relationType + " to " + node2.getSymbol());
	 * //nel.addListEntry(node1.getSymbol(), node1.getSimpleModelEntityUUID(),
	 * node2.getSymbol(), node2.getSimpleModelEntityUUID(),
	 * this.utilityService.translateSBOString(relationType));
	 * nel.addListEntry(node1.getSymbol(), node1.getSimpleModelEntityUUID(),
	 * node1.getAnnotation(), node2Edge.getOutputFlatSpecies().getSymbol(),
	 * node2Edge.getOutputFlatSpecies().getSimpleModelEntityUUID(),
	 * node2Edge.getOutputFlatSpecies().getAnnotation(),
	 * this.utilityService.translateSBOString(relationType)); } } }); }
	 * nel.setAnnotationType(this.mappingNodeRepository.findByEntityUUID(
	 * networkEntityUUID).getAnnotationType()); nel.setName("Context transient");
	 * nel.setListId(-1L); return nel;
	 * 
	 * }
	 */

	/*
	 * @Override public NodeEdgeList getNetwork(String mappingNodeEntityUUID, String
	 * method) { //MappingNode mapping =
	 * this.mappingNodeRepository.findByEntityUUID(mappingNodeEntityUUID); Instant
	 * start = Instant.now(); NodeEdgeList mappingNEL = new NodeEdgeList();
	 * mappingNEL.setAnnotationType(this.mappingNodeRepository.findByEntityUUID(
	 * mappingNodeEntityUUID).getAnnotationType());
	 * 
	 * Instant mappingNelTime = Instant.now(); Instant insideLoggedTime; Instant
	 * insideRetrievedTime; Instant insideretrievedAndSetTime; if
	 * (method.equals("directed")) {
	 * logger.info("Using directed method to get MappingContent for mapping " +
	 * mappingNodeEntityUUID); insideLoggedTime = Instant.now(); List<NodeNodeEdge>
	 * mappingList =
	 * this.mappingNodeRepository.getMappingContentDirected(mappingNodeEntityUUID);
	 * insideRetrievedTime = Instant.now();
	 * mappingNEL.setNodeNodeEdgeList(mappingList); insideretrievedAndSetTime =
	 * Instant.now(); mappingNEL.setName(mappingNodeEntityUUID);
	 * mappingNEL.setListId(-1L);
	 * 
	 * } else if (method.equals("undirected")) {
	 * logger.info("Using undirected method to get MappingContent for mapping " +
	 * mappingNodeEntityUUID); insideLoggedTime = Instant.now(); List<NodeNodeEdge>
	 * mappingList =
	 * this.mappingNodeRepository.getMappingContentUnDirected(mappingNodeEntityUUID)
	 * ; insideRetrievedTime = Instant.now();
	 * mappingNEL.setNodeNodeEdgeList(mappingList); insideretrievedAndSetTime =
	 * Instant.now(); mappingNEL.setName(mappingNodeEntityUUID);
	 * mappingNEL.setListId(-1L);
	 * 
	 * } else if (method.equals("properties")) {
	 * logger.info("Using properties method to get MappingContent for mapping " +
	 * mappingNodeEntityUUID); insideLoggedTime = Instant.now();
	 * List<MappingReturnType> mappingReturnList =
	 * this.mappingNodeRepository.getMappingContentAsProperties(
	 * mappingNodeEntityUUID); insideRetrievedTime = Instant.now(); for
	 * (MappingReturnType mrt : mappingReturnList) { Map<String, Object>
	 * node1Annotation = new HashMap<>(); for (String key :
	 * mrt.getNode1Properties().keySet()) { if(key.startsWith("annotation.")) {
	 * node1Annotation.put(key.split("\\.")[1], mrt.getNode1Properties().get(key));
	 * } } Map<String, Object> node2Annotation = new HashMap<>(); for (String key :
	 * mrt.getNode2Properties().keySet()) { if(key.startsWith("annotation.")) {
	 * node2Annotation.put(key.split("\\.")[1], mrt.getNode2Properties().get(key));
	 * } } mappingNEL.addListEntry((String) mrt.getNode1Properties().get("symbol"),
	 * (String) mrt.getNode1Properties().get("entityUUID"), node1Annotation,
	 * (String) mrt.getNode2Properties().get("symbol"), (String)
	 * mrt.getNode2Properties().get("entityUUID"), node2Annotation, mrt.getEdge());
	 * } insideretrievedAndSetTime = Instant.now();
	 * mappingNEL.setName(mappingNodeEntityUUID); mappingNEL.setListId(-1L); } else
	 * { logger.info("Using standard method to get MappingContent for mapping " +
	 * mappingNodeEntityUUID); insideLoggedTime = Instant.now(); List<FlatSpecies>
	 * mappingFlatSpecies =
	 * this.mappingNodeRepository.getMappingFlatSpecies(mappingNodeEntityUUID);
	 * insideRetrievedTime = Instant.now(); logger.info("Gathered " +
	 * mappingFlatSpecies.size() + " Flat Species");
	 * 
	 * for (FlatSpecies species : mappingFlatSpecies) { FlatSpecies node1 =
	 * this.flatSpeciesRepository.findByEntityUUID(species.getEntityUUID());
	 * node1.getAllRelatedSpecies().forEach((relationType, node2List) -> { for
	 * (FlatEdge node2Edge : node2List) { //logger.info("Working on Node " +
	 * species.getSymbol() + " connected with " + relationType + " to " +
	 * node2.getSymbol()); mappingNEL.addListEntry(node1.getSymbol(),
	 * node1.getSimpleModelEntityUUID(),
	 * node2Edge.getOutputFlatSpecies().getSymbol(),
	 * node2Edge.getOutputFlatSpecies().getSimpleModelEntityUUID(),
	 * this.utilityService.translateSBOString(relationType)); } }); }
	 * insideretrievedAndSetTime = Instant.now(); } Instant end = Instant.now();
	 * logger.info("Timing: " + Duration.between(start, mappingNelTime).toString() +
	 * " / " + Duration.between(mappingNelTime, insideLoggedTime).toString() + " / "
	 * + Duration.between(insideLoggedTime, insideRetrievedTime).toString() + " / "
	 * + Duration.between(insideRetrievedTime, insideretrievedAndSetTime).toString()
	 * + " / " + Duration.between(insideretrievedAndSetTime, end).toString());
	 * return mappingNEL; }
	 */

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
	public List<String> getNetworkRelationSymbols(String networkEntityUUID) {
		return this.getNetworkRelationSymbols(this.getNetworkRelations(networkEntityUUID));
	}

	@Override
	public List<String> getNetworkRelationSymbols(Iterable<FlatMappingReturnType> networkEdges) {
		List<String> networkRelationSymbols = new ArrayList<>();
		for (FlatMappingReturnType edge : networkEdges) {
			if (edge.getRelationship().getSymbol() != null) {
				networkRelationSymbols.add(edge.getRelationship().getSymbol());
			} else {
				networkRelationSymbols.add(edge.getStartNode().getSymbol() + "-" + edge.getRelationshipType() + "->"
						+ edge.getEndNode().getSymbol());
			}
		}
		return networkRelationSymbols;
	}

	@Override
	public Iterable<FlatMappingReturnType> getNetworkRelations(String entityUUID) {
		return this.flatNetworkMappingRepository.getNodesAndRelationshipsForMapping(entityUUID);
	}

	@Override
	public MappingNode getMappingNode(String entityUUID) {
		return this.mappingNodeRepository.findByEntityUUID(entityUUID);
	}

	/*
	 * @Override
	 * 
	 * @Deprecated public List<FlatSpecies> copyFlatSpeciesList(List<FlatSpecies>
	 * original) { List<FlatSpecies> newSpeciesList = new
	 * ArrayList<>(original.size()); Map<String, FlatSpecies> oldUUIDToNewSpeciesMap
	 * = new HashMap<>(); for(int i = 0; i != original.size(); i++) { //Map<String,
	 * List<String>> relationTypeOldUUIDListMap = new HashMap<>(); FlatSpecies
	 * oldSpecies = original.get(i); FlatSpecies newSpecies = new FlatSpecies();
	 * this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(
	 * newSpecies); newSpecies.setAnnotation(oldSpecies.getAnnotation());
	 * newSpecies.setLabels(oldSpecies.getLabels());
	 * newSpecies.setProvenance(oldSpecies.getProvenance());
	 * newSpecies.setSboTerm(oldSpecies.getSboTerm());
	 * newSpecies.setSimpleModelEntityUUID(oldSpecies.getSimpleModelEntityUUID());
	 * newSpecies.setSymbol(oldSpecies.getSymbol());
	 * //newSpecies.addRelatedSpecies(oldSpecies.getAllRelatedSpecies());
	 * oldUUIDToNewSpeciesMap.put(oldSpecies.getEntityUUID(), newSpecies);
	 * newSpeciesList.add(i, newSpecies); } for(int j = 0; j!= original.size(); j++)
	 * { FlatSpecies oldSpecies = original.get(j); FlatSpecies newSpecies =
	 * newSpeciesList.get(j); oldSpecies.getAllRelatedSpecies().forEach((relation,
	 * speciesList)-> { for (FlatEdge oldRelatedSpeciesEdge : speciesList) {
	 * newSpecies.addRelatedSpecies(oldUUIDToNewSpeciesMap.get(oldRelatedSpeciesEdge
	 * .getOutputFlatSpecies().getEntityUUID()), relation); } }); }
	 * 
	 * return newSpeciesList; }
	 */

	/*
	 * @Override
	 * 
	 * @Deprecated public List<FlatSpecies>
	 * copyAndFilterFlatSpeciesList(List<FlatSpecies> originalFlatSpeciesList,
	 * FilterOptions options) { final List<String> relationTypes =
	 * options.getRelationTypes(); List<String> nodeTypes = new ArrayList<>(); for
	 * (String type : options.getNodeTypes()) {
	 * nodeTypes.add(this.utilityService.translateToSBOString(type)); } List<String>
	 * nodeTypes = options.getNodeTypes(); List<String> nodeSymbols =
	 * options.getNodeSymbols(); List<FlatSpecies> newSpeciesList = new
	 * ArrayList<>(originalFlatSpeciesList.size()); Map<String, FlatSpecies>
	 * oldUUIDToNewSpeciesMap = new HashMap<>(); Set<String> oldSkippedSpecies = new
	 * HashSet<>(); Set<String> newConnectedSpecies = new HashSet<>(); for(int i =
	 * 0; i != originalFlatSpeciesList.size(); i++) { //Map<String, List<String>>
	 * relationTypeOldUUIDListMap = new HashMap<>(); FlatSpecies oldSpecies =
	 * originalFlatSpeciesList.get(i); if(nodeSymbols != null &&
	 * (!nodeSymbols.contains(oldSpecies.getSymbol()) || (nodeTypes != null &&
	 * !nodeTypes.contains(this.utilityService.translateSBOString(oldSpecies.
	 * getSboTerm()))))) { oldSkippedSpecies.add(oldSpecies.getEntityUUID());
	 * newSpeciesList.add(i, null); continue; } FlatSpecies newSpecies = new
	 * FlatSpecies();
	 * this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(
	 * newSpecies); newSpecies.setAnnotation(oldSpecies.getAnnotation());
	 * newSpecies.setLabels(oldSpecies.getLabels());
	 * newSpecies.setProvenance(oldSpecies.getProvenance());
	 * newSpecies.setSboTerm(oldSpecies.getSboTerm());
	 * newSpecies.setSimpleModelEntityUUID(oldSpecies.getSimpleModelEntityUUID());
	 * newSpecies.setSymbol(oldSpecies.getSymbol());
	 * //newSpecies.addRelatedSpecies(oldSpecies.getAllRelatedSpecies());
	 * oldUUIDToNewSpeciesMap.put(oldSpecies.getEntityUUID(), newSpecies);
	 * newSpeciesList.add(i, newSpecies); } for(int j = 0; j!=
	 * originalFlatSpeciesList.size(); j++) { FlatSpecies oldSpecies =
	 * originalFlatSpeciesList.get(j); FlatSpecies newSpecies =
	 * newSpeciesList.get(j); if(!(newSpecies == null)) {
	 * oldSpecies.getAllRelatedSpecies().forEach((relation, speciesList)-> {
	 * if(relationTypes.contains(this.utilityService.translateSBOString(relation)))
	 * { for (FlatEdge oldRelatedSpeciesEdge : speciesList) { if
	 * (!oldSkippedSpecies.contains(oldRelatedSpeciesEdge.getOutputFlatSpecies().
	 * getEntityUUID())) {
	 * newSpecies.addRelatedSpecies(oldUUIDToNewSpeciesMap.get(oldRelatedSpeciesEdge
	 * .getOutputFlatSpecies().getEntityUUID()), relation);
	 * newConnectedSpecies.add(newSpecies.getEntityUUID());
	 * newConnectedSpecies.add(oldUUIDToNewSpeciesMap.get(oldRelatedSpeciesEdge.
	 * getOutputFlatSpecies().getEntityUUID()).getEntityUUID()); } } } });
	 * 
	 * } } // check for dangling species (that did not get connected with a
	 * relation) for (int i = 0; i != newSpeciesList.size(); i++) { FlatSpecies
	 * species = newSpeciesList.get(i); if(species != null &&
	 * !newConnectedSpecies.contains(species.getEntityUUID())) {
	 * logger.debug("Removing Species with parent " +
	 * originalFlatSpeciesList.get(i).getEntityUUID()); // be sure to keep the list
	 * the same length newSpeciesList.set(i, null); } } return newSpeciesList; }
	 */
	@Override
	public FilterOptions getFilterOptions(WarehouseGraphNodeType nodeType, String entityUUID) {
		if (nodeType.equals(WarehouseGraphNodeType.MAPPING)) {
			return this.getFilterOptions(this.mappingNodeRepository.findByEntityUUID(entityUUID));
		} else {
			return null; // TODO
		}

	}

	private FilterOptions getFilterOptions(MappingNode mappingNode) {
		FilterOptions filterOptions = new FilterOptions();
		filterOptions.setMappingUuid(mappingNode.getEntityUUID());
		filterOptions.setNetworkType(mappingNode.getMappingType());
		if (mappingNode.getMappingNodeSymbols() == null) {
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
		for (String type : mappingNode.getMappingRelationTypes()) {
			relationTypes.add(type);// this.utilityService.translateSBOString(sboTerm));
		}
		filterOptions.setRelationTypes(relationTypes);
		if (mappingNode.getMappingRelationSymbols() == null) {
			filterOptions.setRelationSymbols(getNetworkRelationSymbols(mappingNode.getEntityUUID()));
		} else {
			filterOptions.setRelationSymbols(new ArrayList<>(mappingNode.getMappingRelationSymbols()));
		}
		return filterOptions;
	}

	@Override
	public List<FlatSpecies> createNetworkContext(List<FlatSpecies> oldSpeciesList, String parentUUID,
			FilterOptions options) {
		List<String> sourceSpeciesForContext = options.getNodeSymbols();
		List<FlatSpecies> contextSpeciesList;
		if (sourceSpeciesForContext.size() == 1) {
			String startNodeSymbol = sourceSpeciesForContext.get(0);
			String startNodeEntityUUID = "";
			for (FlatSpecies oldSpecies : oldSpeciesList) {
				if (oldSpecies.getSymbol() != null && oldSpecies.getSymbol().equals(startNodeSymbol)) {
					startNodeEntityUUID = oldSpecies.getEntityUUID();
					break;
				}
			}
			contextSpeciesList = findNetworkContext(startNodeEntityUUID, options);
		} else if (sourceSpeciesForContext.size() > 1) {
			Map<String, FlatSpecies> uuidToOldSpeciesMap = new HashMap<>();
			Set<String> excludeDrug = new HashSet<>();
			excludeDrug.add("targets");
			String relationTypesApocString = getRelationShipApocString(options, excludeDrug);
			List<String> startNodeEntityUUIDList = new ArrayList<>();
			for (FlatSpecies oldSpecies : oldSpeciesList) {
				if (options.getNodeSymbols().contains((String) oldSpecies.getSymbol())) {
					startNodeEntityUUIDList.add(oldSpecies.getEntityUUID());
					uuidToOldSpeciesMap.put(oldSpecies.getEntityUUID(), oldSpecies);
				}
			}
			contextSpeciesList = this.findMultiGeneSubnet(startNodeEntityUUIDList, relationTypesApocString, options);
			// contextSpeciesList = new ArrayList<FlatSpecies>();
			for (int i = 0; i != startNodeEntityUUIDList.size(); i++) {
				for (int j = i + 1; j != startNodeEntityUUIDList.size(); j++) {
					List<FlatSpecies> dijkstraFlatSpeciesList = this.flatSpeciesRepository
							.apocDijkstraWithDefaultWeight(startNodeEntityUUIDList.get(i), // uuidToOldSpeciesMap.get(startNodeEntityUUIDList.get(i)),
									startNodeEntityUUIDList.get(j), // uuidToOldSpeciesMap.get(startNodeEntityUUIDList.get(j)),
									relationTypesApocString, "weight", 1.0f);
					for (FlatSpecies fs : dijkstraFlatSpeciesList) {
						if (!contextSpeciesList.contains(fs)) {
							contextSpeciesList.add(fs);
						}
					}
				}
			}
		} else {
			return null; // raise Exception? or otherwise let controller know what happend..
		}

		return contextSpeciesList;
	}

	private List<FlatSpecies> findMultiGeneSubnet(List<String> sourceSpeciesForContext, String relationTypesApocString,
			FilterOptions options) {

		if (options.isTerminateAtDrug()) {
			return this.flatSpeciesRepository.findMultiGeneSubnet(sourceSpeciesForContext, relationTypesApocString,
					"+FlatSpecies/Drug", options.getMinSize(), options.getMaxSize());
		} else {
			return this.flatSpeciesRepository.findMultiGeneSubnet(sourceSpeciesForContext, relationTypesApocString,
					"+FlatSpecies", options.getMinSize(), options.getMaxSize());
		}
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
		String relationTypesApocString = getRelationShipApocString(options);
		List<FlatSpecies> contextSpeciesList;
		if (options.isTerminateAtDrug()) {
			contextSpeciesList = this.flatSpeciesRepository.findNetworkContext(startNodeEntityUUID,
					relationTypesApocString, "/Drug", options.getMinSize(), options.getMaxSize());
		} else {
			contextSpeciesList = this.flatSpeciesRepository.findNetworkContext(startNodeEntityUUID,
					relationTypesApocString, options.getMinSize(), options.getMaxSize());
		}
		return contextSpeciesList;
	}

	/**
	 * @param options
	 * @return
	 */
	@Override
	public String getRelationShipApocString(FilterOptions options) {
		String relationTypesApocString = "";
		for (String relationType : options.getRelationTypes()) {
			relationTypesApocString += relationType;
			relationTypesApocString += "|";
		}
		return relationTypesApocString.substring(0, relationTypesApocString.length() - 1);
	}

	/**
	 * @param options
	 * @return
	 */
	@Override
	public String getRelationShipApocString(FilterOptions options, Set<String> exclude) {
		String relationTypesApocString = "";
		boolean addedAtLeastOne = false;
		for (String relationType : options.getRelationTypes()) {
			if (!exclude.contains(relationType)) {
				addedAtLeastOne = true;
				relationTypesApocString += relationType;
				relationTypesApocString += "|";
			}
		}
		return addedAtLeastOne ? relationTypesApocString.substring(0, relationTypesApocString.length() - 1)
				: relationTypesApocString;
	}

	@Override
	public String getMappingEntityUUID(String baseNetworkEntityUUID, String geneSymbol, int minSize, int maxSize) {
		MappingNode mappingNode = this.mappingNodeRepository
				.getByBaseNetworkEntityUUIDAndGeneSymbolAndMinSizeAndMaxSize(baseNetworkEntityUUID, geneSymbol, minSize,
						maxSize);
		return mappingNode != null ? mappingNode.getEntityUUID() : null;

	}

	@Override
	public boolean mappingForPathwayExists(String entityUUID) {
		List<ProvenanceEntity> possibleMappings = (List<ProvenanceEntity>) this.provenanceGraphService
				.findAllByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType.wasDerivedFrom, entityUUID);
		if (possibleMappings.size() < 1) {
			return false;
		} else {
			for (ProvenanceEntity entity : possibleMappings) {
				if (entity.getClass().equals(MappingNode.class)) {
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

	@Override
	public MappingNode saveMappingNode(MappingNode node, int depth) {
		return this.mappingNodeRepository.save(node, depth);
	}

	@Override
	public NetworkInventoryItem deactivateNetwork(String mappingNodeEntityUUID) {
		MappingNode networkNode = this.mappingNodeRepository.findByEntityUUID(mappingNodeEntityUUID);
		if (networkNode != null) {
			networkNode.setActive(false);
			this.mappingNodeRepository.save(networkNode, 0);

			return this.getNetworkIventoryItem(networkNode);
		}

		return null;
	}

	@Override
	public Iterable<String> getAllDistinctSpeciesSboTermsOfPathway(String pathwayNodeEntityUUID) {
		return this.pathwayNodeRepository.getAllDistinctSpeciesSboTermsOfPathway(pathwayNodeEntityUUID);
	}

	@Override
	public Iterable<String> getAllDistinctTransitionSboTermsOfPathway(String pathwayNodeEntityUUID) {
		return this.pathwayNodeRepository.getAllDistinctTransitionSboTermsOfPathway(pathwayNodeEntityUUID);
	}

	@Override
	@Deprecated
	public MappingNode copyNetwork(MappingNode parentMapping, MappingNode newMapping) {
		Iterable<FlatMappingReturnType> parentMappingReturnType = this.flatNetworkMappingRepository
				.getNodesAndRelationshipsForMapping(parentMapping.getEntityUUID());
		FlatEdge currentEdge;
		List<FlatEdge> newEdges = new ArrayList<>();
		Map<String, String> newUUIDToOldUUIDMap = new HashMap<>();
		Set<String> nodeTypes = new HashSet<>();
		Set<String> relationTypes = new HashSet<>();
		Set<String> nodeSymbols = new HashSet<>();
		/**
		 * Traverse the result set and alter the content to generate new entities thus
		 * copying them without actually needing to copy them save the connection
		 * through the new and old entityUUID to connect the entities with provenance
		 * edges
		 */
		Iterator<FlatMappingReturnType> it = parentMappingReturnType.iterator();
		FlatMappingReturnType current;
		while (it.hasNext()) {

			current = it.next();
			currentEdge = current.getRelationship();
			// 1. safe the old edge uuid
			// String oldEdgeUUID = currentEdge.getEntityUUID();

			relationTypes.add(current.getRelationshipType());

			// reset the edge graph base properties (new entityUUID, id to null and version
			// to null)
			this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge);
			if (!newUUIDToOldUUIDMap.containsKey(currentEdge.getInputFlatSpecies().getEntityUUID())) {
				// we have not seen and modified this species before
				String oldUUID = currentEdge.getInputFlatSpecies().getEntityUUID();
				nodeTypes.add(currentEdge.getInputFlatSpecies().getSboTerm());
				nodeSymbols.add(currentEdge.getInputFlatSpecies().getSymbol());
				this.sbmlSimpleModelUtilityServiceImpl
						.resetGraphBaseEntityProperties(currentEdge.getInputFlatSpecies());
				newUUIDToOldUUIDMap.put(currentEdge.getInputFlatSpecies().getEntityUUID(), oldUUID);
			}
			if (!newUUIDToOldUUIDMap.containsKey(currentEdge.getOutputFlatSpecies().getEntityUUID())) {
				// we have not seen this species already
				String oldUUID = currentEdge.getOutputFlatSpecies().getEntityUUID();
				nodeTypes.add(currentEdge.getOutputFlatSpecies().getSboTerm());
				nodeSymbols.add(currentEdge.getOutputFlatSpecies().getSymbol());
				this.sbmlSimpleModelUtilityServiceImpl
						.resetGraphBaseEntityProperties(currentEdge.getOutputFlatSpecies());
				newUUIDToOldUUIDMap.put(currentEdge.getOutputFlatSpecies().getEntityUUID(), oldUUID);
			}
			newEdges.add(currentEdge);
		}
		int numberOfEdges = newEdges.size();
		// save the new edges in one go, thus also saving the new species linked in them
		// this should not throw an OptimisticLockingException as it is all saved in one
		// go
		Iterable<FlatEdge> persistedFlatEdges = this.flatEdgeRepository.saveAll(newEdges);

		// now iterate over the persisted set to form the provenance edges
		Iterator<FlatEdge> pfeIt = persistedFlatEdges.iterator();
		Set<String> connectedSpeciesSet = new HashSet<>();
		Map<String, String> oldToNewMap = new HashMap<>();

		while (pfeIt.hasNext()) {
			FlatEdge currentPersistedEdge = pfeIt.next();
			if (!connectedSpeciesSet.contains(currentPersistedEdge.getInputFlatSpecies().getEntityUUID())) {
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()),
						currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
			}
			if (!connectedSpeciesSet.contains(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID())) {
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()),
						currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
			}
		}
		int numberOfNodes = connectedSpeciesSet.size();
		// we need to clear the session at this point as we manipulated the entities
		// calling for getByEntityUUID with the old EntityUUID would now retrieve the
		// new FlatSpecies instead of the old one
		// as the old one is still stored in the session but with the new entityUUID and
		// a call to the old one would
		// result in a cache fetch and return the changed species rather than
		// re-fetching the old one from db
		// TODO Does this have any side effect for other threads also using this
		// session?
		this.session.clear();

		// now we can connect the old and new species with provenance edges and connect
		// the new species with the mapping node
		for (String oldUUID : oldToNewMap.keySet()) {
			String newUUID = oldToNewMap.get(oldUUID);
			FlatSpecies newSpecies = this.flatSpeciesRepository.findByEntityUUID(newUUID);
			this.provenanceGraphService.connect(newSpecies, oldUUID, ProvenanceGraphEdgeType.wasDerivedFrom);
			this.connect(newMapping, newSpecies, WarehouseGraphEdgeType.CONTAINS);
		}

		newMapping.addWarehouseAnnotation("numberofnodes", String.valueOf(numberOfNodes));
		newMapping.addWarehouseAnnotation("numberofrelations", String.valueOf(numberOfEdges));
		newMapping.setMappingNodeTypes(nodeTypes);
		newMapping.setMappingRelationTypes(relationTypes);
		newMapping.setMappingNodeSymbols(nodeSymbols);
		return (MappingNode) this.saveWarehouseGraphNodeEntity(newMapping, 0);
	}

	@Override
	public MappingNode createMappingFromMappingWithOptions(MappingNode parent, MappingNode newMapping,
			FilterOptions options, MappingStep step) {
		Iterable<FlatMappingReturnType> parentMappingReturnType = this.flatNetworkMappingRepository
				.getNodesAndRelationshipsForMapping(parent.getEntityUUID());
		FlatEdge currentEdge;
		List<FlatEdge> newEdges = new ArrayList<>();
		Map<String, String> newUUIDToOldUUIDMap = new HashMap<>();
		Set<String> nodeTypes = new HashSet<>();
		Set<String> relationTypes = new HashSet<>();
		Set<String> nodeSymbols = new HashSet<>();
		Set<String> relationSymbols = new HashSet<>();

		// Lists to check for Filtering
		final List<String> relationTypesToBeIncluded = options.getRelationTypes();
		final List<String> nodeTypesToBeIncluded = options.getNodeTypes();
		final List<String> nodeSymbolsToBeIncluded = options.getNodeSymbols();
		final List<String> relationSymbolsToBeIncluded = options.getRelationSymbols();

		// Do we have annotations?
		boolean addNodeAnnotation = false;
		boolean addRelationAnnotation = false;
		if (step.equals(MappingStep.ANNOTATE)) {
			// yes, which one?
			if (options.getNodeAnnotation() != null && options.getNodeAnnotation().size() > 0
					&& options.getNodeAnnotationName() != null && !options.getNodeAnnotationName().equals("")
					&& options.getNodeAnnotationType() != null && !options.getNodeAnnotationType().equals("")) {
				// nodeAnnotation with all info needed
				addNodeAnnotation = true;
				newMapping.addAnnotationType("node." + options.getNodeAnnotationName(),
						options.getNodeAnnotationType());
			}
			if (options.getRelationAnnotation() != null && options.getRelationAnnotation().size() > 0
					&& options.getRelationAnnotationName() != null && !options.getRelationAnnotationName().equals("")
					&& options.getRelationAnnotationType() != null && !options.getRelationAnnotationType().equals("")) {
				// relationAnnotation with all info needed
				addRelationAnnotation = true;
				newMapping.addAnnotationType("relation." + options.getRelationAnnotationName(),
						options.getRelationAnnotationType());
			}
		}

		/**
		 * Traverse the result set and alter the content to generate new entities thus
		 * copying them without actually needing to copy them save the connection
		 * through the new and old entityUUID to connect the entities with provenance
		 * edges
		 */
		Iterator<FlatMappingReturnType> it = parentMappingReturnType.iterator();
		FlatMappingReturnType current;
		while (it.hasNext()) {

			current = it.next();
			currentEdge = current.getRelationship();
			// 1. safe the old edge uuid
			// String oldEdgeUUID = currentEdge.getEntityUUID();
			if (relationTypesToBeIncluded.contains(current.getRelationshipType())
					&& relationSymbolsToBeIncluded.contains(currentEdge.getSymbol())) {
				relationTypes.add(current.getRelationshipType());
				relationSymbols.add(currentEdge.getSymbol());
				// reset the edge graph base properties (new entityUUID, id to null and version
				// to null)
				this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge);
				// do we have a relationshipAnnotation to add?
				if (addRelationAnnotation && options.getRelationAnnotation().containsKey(currentEdge.getSymbol())) {
					currentEdge.addAnnotation(options.getRelationAnnotationName(),
							options.getRelationAnnotation().get(currentEdge.getSymbol()));
					currentEdge.addAnnotationType(options.getRelationAnnotationName(),
							options.getRelationAnnotationType());
				}
				if (!newUUIDToOldUUIDMap.containsKey(currentEdge.getInputFlatSpecies().getEntityUUID())
						&& nodeTypesToBeIncluded.contains(
								this.utilityService.translateSBOString(currentEdge.getInputFlatSpecies().getSboTerm()))
						&& nodeSymbolsToBeIncluded.contains(currentEdge.getInputFlatSpecies().getSymbol())) {
					// we have not seen and modified this species before
					String oldUUID = currentEdge.getInputFlatSpecies().getEntityUUID();
					nodeTypes.add(currentEdge.getInputFlatSpecies().getSboTerm());
					nodeSymbols.add(currentEdge.getInputFlatSpecies().getSymbol());
					this.sbmlSimpleModelUtilityServiceImpl
							.resetGraphBaseEntityProperties(currentEdge.getInputFlatSpecies());
					// add annotation?
					if (addNodeAnnotation
							&& options.getNodeAnnotation().containsKey(currentEdge.getInputFlatSpecies().getSymbol())) {
						currentEdge.getInputFlatSpecies().addAnnotation(options.getNodeAnnotationName(),
								options.getNodeAnnotation().get(currentEdge.getInputFlatSpecies().getSymbol()));
						currentEdge.getInputFlatSpecies().addAnnotationType(options.getNodeAnnotationName(),
								options.getNodeAnnotationType());
					}

					newUUIDToOldUUIDMap.put(currentEdge.getInputFlatSpecies().getEntityUUID(), oldUUID);
				}
				if (!newUUIDToOldUUIDMap.containsKey(currentEdge.getOutputFlatSpecies().getEntityUUID())
						&& nodeTypesToBeIncluded.contains(
								this.utilityService.translateSBOString(currentEdge.getOutputFlatSpecies().getSboTerm()))
						&& nodeSymbolsToBeIncluded.contains(currentEdge.getOutputFlatSpecies().getSymbol())) {
					// we have not seen this species before
					String oldUUID = currentEdge.getOutputFlatSpecies().getEntityUUID();
					nodeTypes.add(currentEdge.getOutputFlatSpecies().getSboTerm());
					nodeSymbols.add(currentEdge.getOutputFlatSpecies().getSymbol());
					this.sbmlSimpleModelUtilityServiceImpl
							.resetGraphBaseEntityProperties(currentEdge.getOutputFlatSpecies());
					// add annotation?
					if (addNodeAnnotation && options.getNodeAnnotation()
							.containsKey(currentEdge.getOutputFlatSpecies().getSymbol())) {
						currentEdge.getOutputFlatSpecies().addAnnotation(options.getNodeAnnotationName(),
								options.getNodeAnnotation().get(currentEdge.getOutputFlatSpecies().getSymbol()));
						currentEdge.getOutputFlatSpecies().addAnnotationType(options.getNodeAnnotationName(),
								options.getNodeAnnotationType());
					}
					newUUIDToOldUUIDMap.put(currentEdge.getOutputFlatSpecies().getEntityUUID(), oldUUID);
				}
				newEdges.add(currentEdge);
			}
		}
		int numberOfEdges = newEdges.size();
		// save the new edges in one go, thus also saving the new species linked in them
		// this should not throw an OptimisticLockingException as it is all saved in one
		// go
		Iterable<FlatEdge> persistedFlatEdges = this.flatEdgeRepository.saveAll(newEdges);

		// now iterate over the persisted set to form the provenance edges
		Iterator<FlatEdge> pfeIt = persistedFlatEdges.iterator();
		Set<String> connectedSpeciesSet = new HashSet<>();
		Map<String, String> oldToNewMap = new HashMap<>();

		while (pfeIt.hasNext()) {
			FlatEdge currentPersistedEdge = pfeIt.next();
			if (!connectedSpeciesSet.contains(currentPersistedEdge.getInputFlatSpecies().getEntityUUID())) {
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()),
						currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
			}
			if (!connectedSpeciesSet.contains(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID())) {
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()),
						currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
			}
		}
		int numberOfNodes = connectedSpeciesSet.size();
		// we need to clear the session at this point as we manipulated the entities
		// calling for getByEntityUUID with the old EntityUUID would now retrieve the
		// new FlatSpecies instead of the old one
		// as the old one is still stored in the session but with the new entityUUID and
		// a call to the old one would
		// result in a cache fetch and return the changed species rather than
		// re-fetching the old one from db
		// TODO Does this have any side effect for other threads also using this
		// session?
		this.session.clear();
		List<FlatSpecies> newSpeciesList = new ArrayList<>();
		List<FlatEdge> pathwayFlatEdges = new ArrayList<>();
		// now we can connect the old and new species with provenance edges and connect
		// the new species with the mapping node
		for (String oldUUID : oldToNewMap.keySet()) {
			String newUUID = oldToNewMap.get(oldUUID);
			FlatSpecies newSpecies = this.flatSpeciesRepository.findByEntityUUID(newUUID);
			if (step.equals(MappingStep.PATHWAYINFO)) {
				newSpeciesList.add(newSpecies);
			}
			this.provenanceGraphService.connect(newSpecies, oldUUID, ProvenanceGraphEdgeType.wasDerivedFrom);
			this.connect(newSpecies, this.provenanceGraphService.getByEntityUUID(newSpecies.getSimpleModelEntityUUID()), WarehouseGraphEdgeType.DERIVEDFROM);
			this.connect(newMapping, newSpecies, WarehouseGraphEdgeType.CONTAINS);
		}
		// do we want to add pathway Information?
		if (step.equals(MappingStep.PATHWAYINFO)) {
			FlatEdge pathwayEdge;
			for (int i = 0; i != newSpeciesList.size(); i++) {
				for (int j = i+1; j != newSpeciesList.size(); j++) {
					List<PathwayNode> sharedPathwayList = this.pathwayNodeRepository.findAllBySharedDerivedFromWarehouseEdge(newSpeciesList.get(i).getEntityUUID(), newSpeciesList.get(j).getEntityUUID());
					if (sharedPathwayList != null && sharedPathwayList.size() > 0) {
						pathwayEdge = this.flatEdgeService.createFlatEdge("SHAREDPATHWAY");
						String sharedPathwayString = "";
						for (PathwayNode pathway : sharedPathwayList) {
							sharedPathwayString += pathway.getPathwayIdString();
							sharedPathwayString += ", ";
						}
						pathwayEdge.addAnnotation("pathwayids", sharedPathwayString.substring(0, sharedPathwayString.length()-2));
						pathwayEdge.addAnnotationType("pathwayids", "string");
						pathwayEdge.setInputFlatSpecies(newSpeciesList.get(i));
						pathwayEdge.setOutputFlatSpecies(newSpeciesList.get(j));
						pathwayEdge.setSymbol(newSpeciesList.get(i).getSymbol() + "-" + pathwayEdge.getTypeString() + "-" + newSpeciesList.get(j).getSymbol());
						pathwayFlatEdges.add(pathwayEdge);
						numberOfEdges += 1;
						relationTypes.add("SHAREDPATHWAY");
						
					}
				}
			}
			if (pathwayFlatEdges.size() > 0) {
				this.flatEdgeRepository.saveAll(pathwayFlatEdges);
			}
		}
		

		newMapping.addWarehouseAnnotation("numberofnodes", String.valueOf(numberOfNodes));
		newMapping.addWarehouseAnnotation("numberofrelations", String.valueOf(numberOfEdges));
		newMapping.setMappingNodeTypes(nodeTypes);
		newMapping.setMappingRelationTypes(relationTypes);
		newMapping.setMappingNodeSymbols(nodeSymbols);
		newMapping.setMappingRelationSymbols(relationSymbols);
		return (MappingNode) this.saveWarehouseGraphNodeEntity(newMapping, 0);
	}

}
