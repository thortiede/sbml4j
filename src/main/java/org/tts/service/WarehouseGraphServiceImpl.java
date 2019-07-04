package org.tts.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.controller.WarehouseController;
import org.tts.model.api.Input.PathwayCollectionCreationItem;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.NodeEdgeList;
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
	public WarehouseGraphNode saveWarehouseGraphNodeEntity(WarehouseGraphNode node) {
		return this.warehouseGraphNodeRepository.save(node, 0);
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
			DatabaseNode sourceEntity = this.findSource(pathwayNode);
			item.setSource(sourceEntity.getSource());
			item.setSourceVersion(sourceEntity.getSourceVersion());
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
		item.setSource(findSource(warehouseGraphNode).getSource());
		item.setSourceVersion(findSource(warehouseGraphNode).getSourceVersion());
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
			if(pathway != null && findSource(pathway).getEntityUUID() == databaseNode.getEntityUUID()) {
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
	public List<NetworkInventoryItem> getListOfNetworkInventoryItems() {
		List<NetworkInventoryItem> inventory = new ArrayList<>();
		// get all mapping nodes
		for (MappingNode mapping : this.mappingNodeRepository.findAll()) {
			// create an inventory item for each of them and fill it.
			NetworkInventoryItem item = new NetworkInventoryItem();
			item.setWarehouseGraphNodeType(WarehouseGraphNodeType.MAPPING);
			item.setEntityUUID(mapping.getEntityUUID());
			item.setSource(findSource(mapping).getSource());
			item.setSourceVersion(findSource(mapping).getSourceVersion());
			item.setName(mapping.getMappingName());
			item.setOrganismCode(((Organism)this.warehouseGraphNodeRepository.findOrganismForWarehouseGraphNode(mapping.getEntityUUID())).getOrgCode());
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
				
				item.setNumberOfNodes((String)warehouseMap.get("numberofnodes"));
				item.setNumberOfRelations((String)warehouseMap.get("numberofrelations"));
			} catch (Exception e) {
				logger.info("Mapping " + mapping.getMappingName() + " (" + mapping.getEntityUUID() + ") does not have the warehouse-Properties set");
			}
			for (OutputType type : OutputType.values()) {
				item.add(linkTo(methodOn(WarehouseController.class).getNetwork(item.getEntityUUID(), "standard", type.name())).withRel(type.name()));
			
			}	inventory.add(item);
		
		}
		return inventory;
		
	}

	@Override
	public NodeEdgeList getNetwork(String mappingNodeEntityUUID, String method) {
		//MappingNode mapping = this.mappingNodeRepository.findByEntityUUID(mappingNodeEntityUUID);
		NodeEdgeList mappingNEL = new NodeEdgeList();
		if (method == "directed") {
			
			mappingNEL.setNodeNodeEdgeList(this.mappingNodeRepository.getMappingContentDirected(mappingNodeEntityUUID));
			mappingNEL.setName(mappingNodeEntityUUID);
			mappingNEL.setListId(-1L);
			
		} else if (method == "undirected") {
			
			mappingNEL.setNodeNodeEdgeList(this.mappingNodeRepository.getMappingContentUnDirected(mappingNodeEntityUUID));
			mappingNEL.setName(mappingNodeEntityUUID);
			mappingNEL.setListId(-1L);
			
		} else {
			List<FlatSpecies> mappingFlatSpecies = this.mappingNodeRepository.getMappingFlatSpecies(mappingNodeEntityUUID);
			
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
		}
		return mappingNEL;
	}

	
}
