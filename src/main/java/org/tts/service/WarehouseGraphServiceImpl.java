package org.tts.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphEdge;
import org.tts.model.provenance.ProvenanceGraphEntityNode;
import org.tts.model.common.Organism;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
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

}
