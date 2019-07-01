package org.tts.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.controller.WarehouseController;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.full.SBMLReaction;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphEdge;
import org.tts.model.provenance.ProvenanceGraphEntityNode;
import org.tts.model.simple.SBMLSimpleTransition;
import org.tts.model.common.Organism;
import org.tts.model.common.SBMLCompartment;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLQualSpeciesGroup;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.common.SBMLSpeciesGroup;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.model.warehouse.WarehouseGraphEdge;
import org.tts.model.warehouse.WarehouseGraphNode;
import org.tts.repository.warehouse.DatabaseNodeRepository;
import org.tts.repository.warehouse.FileNodeRepository;
import org.tts.repository.warehouse.MappingNodeRepository;
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
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;

	@Autowired
	WarehouseGraphEdgeRepository warehouseGraphEdgeRepository;

	@Autowired
	UtilityService utilityService;
	
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
	 * connect two warehouse entities. The entity is not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param source the startNode of the edge
	 * @param target the endNode of the edge
	 * @param edgetype the WarehouseGraphEdgeType
	 */
	@Override
	public void connect(ProvenanceEntity source, ProvenanceEntity target,
			WarehouseGraphEdgeType edgetype) {

		WarehouseGraphEdge newEdge = new WarehouseGraphEdge();
		this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newEdge);
		newEdge.setWarehouseGraphEdgeType(edgetype);
		newEdge.setStartNode(source);
		newEdge.setEndNode(target);
		
		this.warehouseGraphEdgeRepository.save(newEdge, 0);
		
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
	public List<PathwayInventoryItem> getListofPathwayInventory(String username) {
		List<PathwayInventoryItem> pathwayInventoryItemList = new ArrayList<>();
		for (PathwayNode pathwayNode : this.pathwayNodeRepository.findAllPathwaysAttributedToUser(username)) {
			PathwayInventoryItem item = new PathwayInventoryItem();
			item.setEntityUUID(pathwayNode.getEntityUUID());
			item.setPathwayId(pathwayNode.getPathwayIdString());
			item.setPathwayName(pathwayNode.getPathwayNameString());
			item.setPathwayOrganismCode(((Organism)this.warehouseGraphNodeRepository.findOrganismForWarehouseGraphNode(pathwayNode.getEntityUUID())).getOrgCode());
			DatabaseNode sourceEntity = this.findSource(pathwayNode);
			item.setPathwaySource(sourceEntity.getSource());
			item.setPathwaySourceVersion(sourceEntity.getSourceVersion());
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

	private DatabaseNode findSource(ProvenanceEntity node) {
		if(node.getClass() == DatabaseNode.class) {
			return (DatabaseNode) node;
		} else {
			return findSource(this.warehouseGraphNodeRepository.findByWarehouseGraphEdgeTypeAndEndNode(WarehouseGraphEdgeType.CONTAINS, node.getEntityUUID()));
		}
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

}
