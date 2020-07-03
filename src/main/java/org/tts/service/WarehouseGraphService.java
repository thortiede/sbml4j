package org.tts.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Input.PathwayCollectionCreationItem;
import org.tts.model.api.Output.FlatMappingReturnType;
import org.tts.model.api.Output.NetworkInventoryItem;
import org.tts.model.api.Output.PathwayInventoryItem;
import org.tts.model.api.Output.WarehouseInventoryItem;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.common.GraphEnum.MappingStep;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphNodeType;
import org.tts.model.common.Organism;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayCollectionNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.model.warehouse.WarehouseGraphNode;


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

	public boolean connect(ProvenanceEntity source, ProvenanceEntity target, WarehouseGraphEdgeType edgetype);

	public boolean connect(ProvenanceEntity source, ProvenanceEntity target, WarehouseGraphEdgeType edgetype, boolean doCheck);

	public void connect(Iterable<ProvenanceEntity> sourceEntities, ProvenanceEntity target, WarehouseGraphEdgeType edgetype);

	public void connect(ProvenanceEntity source, Iterable<ProvenanceEntity> targetEntities, WarehouseGraphEdgeType edgetype);

	public PathwayNode createPathwayNode(String idString, String nameString, Organism org);

	public MappingNode createMappingNode(WarehouseGraphNode pathway, NetworkMappingType type, String mappingName);

	public List<PathwayInventoryItem> getListofPathwayInventory(String username);

	public List<ProvenanceEntity> getPathwayContents(String username, String entityUUID);

	public PathwayNode getPathwayNode(String username, String entityUUID);

	public WarehouseInventoryItem getWarehouseInventoryItem(WarehouseGraphNode warehouseGraphNode);

	public DatabaseNode getDatabaseNode(String databaseEntityUUID);

	public PathwayCollectionNode createPathwayCollection(PathwayCollectionCreationItem pathwayCollectionCreationItem, DatabaseNode databaseNode,
			ProvenanceGraphActivityNode createPathwayCollectionGraphActivityNode, ProvenanceGraphAgentNode agentNode);

	public PathwayCollectionNode createPathwayCollectionNode(String pathwayCollectionName, String pathwayCollectionDescription,
			Organism organism);

	public Map<String, Integer> buildPathwayFromCollection(PathwayNode pathwayNode, PathwayCollectionNode pathwayCollectionNode,
			ProvenanceGraphActivityNode buildPathwayFromCollectionActivityNode, ProvenanceGraphAgentNode agentNode);

	public List<String> getListofPathwayUUIDs();

	public WarehouseGraphNode saveWarehouseGraphNodeEntity(WarehouseGraphNode node, int depth);

	public List<NetworkInventoryItem> getListOfNetworkInventoryItems(String username, boolean isActiveOnly);

	//public NodeEdgeList getNetwork(String mappingNodeEntityUUID, String method);

	public NetworkInventoryItem getNetworkInventoryItem(String entityUUID);

	public Iterable<FlatSpecies> getNetworkNodes(String entityUUID);

	public Set<String> getNetworkNodeSymbols(Iterable<FlatSpecies> networkNodes);

	public Set<String> getNetworkNodeSymbols(String entityUUID);

	public MappingNode getMappingNode(String entityUUID);

	//public List<FlatSpecies> copyFlatSpeciesList(List<FlatSpecies> original);

	public FilterOptions getFilterOptions(WarehouseGraphNodeType nodeType, String entityUUID);

	//public List<FlatSpecies> copyAndFilterFlatSpeciesList(List<FlatSpecies> originalFlatSpeciesList, FilterOptions options);

	//public List<FlatSpecies> createNetworkContext(List<FlatSpecies> oldSpeciesList, String parentUUID, FilterOptions options);
	
	public MappingNode saveMappingNode(MappingNode node, int depth);

	public String getMappingEntityUUID(String baseNetworkEntityUUID, String geneSymbol, int minSize, int maxSize);

	public boolean mappingForPathwayExists(String entityUUID);

	public String findStartNode(String baseNetworkUUID, String geneSymbol);

	//List<FlatSpecies> findNetworkContext(String startNodeEntityUUID, FilterOptions options);

	//NodeEdgeList flatSpeciesListToNEL(List<FlatSpecies> flatSpeciesList, String networkEntityUUID);

	public NetworkInventoryItem deactivateNetwork(String mappingNodeEntityUUID);
	
	public Iterable<String> getAllDistinctSpeciesSboTermsOfPathway(String pathwayNodeEntityUUID);

	public Iterable<String> getAllDistinctTransitionSboTermsOfPathway(String pathwayNodeEntityUUID);

	public MappingNode copyNetwork(MappingNode parentMapping, MappingNode newMapping);

	public MappingNode createMappingFromMappingWithOptions(MappingNode parent, MappingNode newMapping,
			FilterOptions options, MappingStep step);

	Set<String> getNetworkRelationSymbols(String networkEntityUUID);

	Set<String> getNetworkRelationSymbols(Iterable<FlatMappingReturnType> networkEdges);

	Iterable<FlatMappingReturnType> getNetworkRelations(String networkEntityUUID);
	
	//public String getRelationShipApocString(FilterOptions options);

	//public String getRelationShipApocString(FilterOptions options, Set<String> exclude);

	public Resource getNetwork(String networkEntityUUID, boolean directed);

	public Resource getNetwork(String networkEntityUUID, List<String> geneset, boolean directed);

	public Resource getNetworkContext(String networkEntityUUID, List<String> genes, int minSize, int maxSize,
			boolean terminateAtDrug, String direction, boolean directed);

	public String postNetworkContext(MappingNode mappingNode, List<String> genes, int minSize, int maxSize,
			boolean terminateAtDrug, String direction);

	/**
	 * @param username
	 * @param parent
	 * @param newMappingName
	 * @param activityName
	 * @param activityType
	 * @param mappingType
	 * @return
	 */
	MappingNode createMappingPre(String username, MappingNode parent, String newMappingName, String activityName,
			ProvenanceGraphActivityType activityType, NetworkMappingType mappingType);

	public int getNumberOfNetworkNodes(String networkEntityUUID);

	public Set<String> getNetworkNodeTypes(String networkEntityUUID);

	public Set<String> getNetworkRelationTypes(String networkEntityUUID);

	public Set<String> getNetworkRelationTypes(Iterable<FlatMappingReturnType> networkEdges);

	public int getNumberOfNetworkRelations(String networkEntityUUID);

	public Set<String> getNetworkNodeTypes(Iterable<FlatSpecies> networkNodes);



}
