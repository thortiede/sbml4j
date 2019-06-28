package org.tts.service;

import java.util.Map;

import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.Organism;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
import org.tts.model.warehouse.PathwayNode;


public interface WarehouseGraphService {
	/*public WarehouseGraphNode getWarehouseGraphNode(WarehouseGraphNodeType warehouseGraphNodeType, String source, String sourceVersion, Organism org, Map<String, String> entityKeyMap);
	
	public WarehouseGraphNode createWarehouseGraphNode(WarehouseGraphNodeType warehouseGraphNodeType, String source, String sourceVersion, Organism org, Map<String, String> entityKeyMap);

	public boolean warehouseGraphNodeExists(WarehouseGraphNodeType warehouseGraphNodeType, String source, String sourceVersion, Organism org, Map<String, String> entityKeyMap);
*/
	public DatabaseNode getDatabaseNode(String source, String sourceVersion, Organism org,
			Map<String, String> matchingAttributes);

	public DatabaseNode createDatabaseNode(String source, String sourceVersion, Organism org,
			Map<String, String> matchingAttributes);

	public boolean fileNodeExists(FileNodeType fileNodeType, Organism org, String filename);

	public FileNode createFileNode(FileNodeType fileNodeType, Organism org, String filename, byte[] filecontent);

	public FileNode getFileNode(FileNodeType fileNodeType, Organism org, String filename);

	public void connect(ProvenanceEntity source, ProvenanceEntity target, WarehouseGraphEdgeType edgetype);

	public PathwayNode createPathwayNode(String idString, String nameString, Organism org);
	
}
