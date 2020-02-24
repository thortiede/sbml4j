package org.tts.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.ApocPathReturnType;
import org.tts.model.api.Output.FlatMappingReturnType;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.repository.common.ExternalResourceEntityRepository;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLSpeciesRepository;
import org.tts.repository.flat.FlatEdgeRepository;
import org.tts.repository.flat.FlatNetworkMappingRepository;
import org.tts.repository.flat.FlatSpeciesRepository;
import org.neo4j.ogm.session.Session;

@Service
public class GraphBaseEntityServiceImpl implements GraphBaseEntityService {

	@Autowired
	GraphBaseEntityRepository graphBaseEntityRepository;

	@Autowired
	FlatNetworkMappingRepository flatNetworkMappingRepository;

	@Autowired
	FlatEdgeRepository flatEdgeRepository;

	@Autowired
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;

	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	GraphMLService graphMLService;

	@Autowired
	SBMLSpeciesRepository sbmlSpeciesRepository;

	@Autowired
	ExternalResourceEntityRepository externalResourceEntityRepository;

	@Autowired
	FlatSpeciesRepository flatSpeciesRepository;
	
	@Autowired
	Session session;
	
	/*public GraphBaseEntityServiceImpl(GraphBaseEntityRepository graphBaseEntityRepository,
			FlatNetworkMappingRepository flatNetworkMappingRepository,
			FlatEdgeRepository flatEdgeRepository,
			SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl,
			ProvenanceGraphService provenanceGraphService,
			SBMLSpeciesRepository sbmlSpeciesRepository,
			Session session
			) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.flatNetworkMappingRepository = flatNetworkMappingRepository;
		this.flatEdgeRepository = flatEdgeRepository;
		this.sbmlSimpleModelUtilityServiceImpl = sbmlSimpleModelUtilityServiceImpl;
		this.provenanceGraphService = provenanceGraphService;
		this.sbmlSpeciesRepository = sbmlSpeciesRepository;
		this.session = session;
	}*/

	@Override
	public GraphBaseEntity persistEntity(GraphBaseEntity newEntity) {
		return this.graphBaseEntityRepository.save(newEntity);
	}

	@Override
	public GraphBaseEntity findByEntityUUID(String entityUuid) {
		return graphBaseEntityRepository.findByEntityUUID(entityUuid);
	}

	@Override
	public Iterable<GraphBaseEntity> findAll() {
		// TODO Auto-generated method stub
		return graphBaseEntityRepository.findAll();
	}

	public Map<String, String> cloneAndPersistNetworkAndReturnOldToNewMap(String entityUUID) {
		Iterable<FlatMappingReturnType> ret = this.flatNetworkMappingRepository.getNodesAndRelationshipsForMapping(entityUUID);		
		FlatEdge currentEdge;
		List<FlatEdge> newEdges = new ArrayList<>();
		Map<String, String> newUUIDToOldUUIDMap = new HashMap<>();
		
		/**
		 * Traverse the result set and alter the content to generate new entities
		 * thus copying them without actually needing to copy them
		 * save the connection through the new and old entityUUID to connect the entities with provenance edges
		 */
		Iterator<FlatMappingReturnType> it = ret.iterator();
		FlatMappingReturnType current;
		while (it.hasNext()) {
		
			current = it.next();
			currentEdge = current.getRelationship();
			// 1. safe the old edge uuid
			//String oldEdgeUUID = currentEdge.getEntityUUID();
			// reset the edge graph base properties (new entityUUID, id to null and version to null)
			this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge);			
			if(!newUUIDToOldUUIDMap.containsKey(currentEdge.getInputFlatSpecies().getEntityUUID())) { 
				// we have not seen and modified this species before
				String oldUUID = currentEdge.getInputFlatSpecies().getEntityUUID();
				this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge.getInputFlatSpecies());
				newUUIDToOldUUIDMap.put(currentEdge.getInputFlatSpecies().getEntityUUID(), oldUUID);
			}
			if(!newUUIDToOldUUIDMap.containsKey(currentEdge.getOutputFlatSpecies().getEntityUUID())) {
				// we have not seen this species already
				String oldUUID = currentEdge.getOutputFlatSpecies().getEntityUUID();
				this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge.getOutputFlatSpecies());
				newUUIDToOldUUIDMap.put(currentEdge.getOutputFlatSpecies().getEntityUUID(), oldUUID);
			}
			newEdges.add(currentEdge);
			
			
		}
		// save the new edges in one go, thus also saving the new species linked in them
		// this should not throw an OptimisticLockingException as it is all saved in one go
		Iterable<FlatEdge> persistedFlatEdges = this.flatEdgeRepository.saveAll(newEdges);
		
		// now iterate over the persisted set to form the provenance edges
		Iterator<FlatEdge> pfeIt = persistedFlatEdges.iterator();
		Set<String> connectedSpeciesSet = new HashSet<>();
		Map<String, String> oldToNewMap = new HashMap<>();
		
		while (pfeIt.hasNext()) {
			FlatEdge currentPersistedEdge = pfeIt.next();
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getInputFlatSpecies().getEntityUUID())) {
				String newUUID = currentPersistedEdge.getInputFlatSpecies().getEntityUUID();
				String oldUUID = newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				//this.provenanceGraphService.connect(currentPersistedEdge.getInputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()), currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
			}
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID())) {
				//this.provenanceGraphService.connect(currentPersistedEdge.getOutputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()), currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
			}
				
			System.out.println(currentPersistedEdge.getEntityUUID());
		}
		return oldToNewMap;
	}
	
	@Override
	public String testFlatMappingExtract(String entityUUID) {
		
		Map<String, String> oldToNewMap = this.cloneAndPersistNetworkAndReturnOldToNewMap(entityUUID);
		session.clear();
		connectProvenance(oldToNewMap);
		
		/*while (pfeIt.hasNext()) {
			FlatEdge currentPersistedEdge = pfeIt.next();
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getInputFlatSpecies().getEntityUUID())) {
				String newUUID = currentPersistedEdge.getInputFlatSpecies().getEntityUUID();
				String oldUUID = newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				this.provenanceGraphService.connect(currentPersistedEdge.getInputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				connectedSpeciesSet.add(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
			}
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID())) {
				this.provenanceGraphService.connect(currentPersistedEdge.getOutputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				connectedSpeciesSet.add(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
			}
				
			System.out.println(currentPersistedEdge.getEntityUUID());
		}*/
		return "yes";
	}

	/**
	 * @param oldToNewMap
	 */
	private void connectProvenance(Map<String, String> oldToNewMap) {
		for (String oldUUID : oldToNewMap.keySet()) {
			String newUUID = oldToNewMap.get(oldUUID);
			this.provenanceGraphService.connect(newUUID, oldUUID, ProvenanceGraphEdgeType.wasDerivedFrom);
		}
	}

	@Override
	public Resource testContextMethod(String networkEntityUUID, String geneSymbol) {
		String flatSpeciesEntityUUID = this.flatSpeciesRepository.findStartNodeEntityUUID(networkEntityUUID, geneSymbol);
		FlatSpecies geneSymbolSpecies;
		// 0. Check whether we have that geneSymbol directly in the network
		if (flatSpeciesEntityUUID == null) {
			
			// 1. Find SBMLSpecies with geneSymbol (or SBMLSpecies connected to externalResource with geneSymbol)
			SBMLSpecies geneSpecies = this.sbmlSpeciesRepository.findBySBaseName(geneSymbol);
			String simpleModelGeneEntityUUID = null;
			if(geneSpecies != null) {
				simpleModelGeneEntityUUID = geneSpecies.getEntityUUID();
			} else {
				Iterable<SBMLSpecies> bqSpecies = this.sbmlSpeciesRepository.findByBQConnectionTo(geneSymbol, "KEGG");
				if (bqSpecies == null) {
					// we could not find that gene
					return null;
				} else {
					Iterator<SBMLSpecies> bqSpeciesIterator = bqSpecies.iterator();
					while (bqSpeciesIterator.hasNext()) {
						SBMLSpecies current = bqSpeciesIterator.next();
						System.out.println("Found Species " + current.getsBaseName() + " with uuid: " + current.getEntityUUID());
						if(geneSpecies == null) {
							System.out.println("Using " + current.getsBaseName());
							geneSpecies = current;
						}
					}
					simpleModelGeneEntityUUID = geneSpecies.getEntityUUID();
				}
			}
			if (simpleModelGeneEntityUUID == null) {
				return null;
			}
			// 2. Find FlatSpecies in network with networkEntityUUID to use as startPoint for context search
			geneSymbolSpecies = this.flatSpeciesRepository.findBySimpleModelEntityUUIDInNetwork(simpleModelGeneEntityUUID, networkEntityUUID);
			if (geneSymbolSpecies == null) {
				//return "Could not find derived FlatSpecies to SBMLSpecies (uuid:" + simpleModelGeneEntityUUID + ") in network with uuid: " + networkEntityUUID;
				return null;
			}
			flatSpeciesEntityUUID = geneSymbolSpecies.getEntityUUID();;
		}
		// 3. getContext (TODO What is the return type of this employing FlatEdges?)
		Iterable<ApocPathReturnType> contextNet = this.flatNetworkMappingRepository.runApocPathExpandFor(flatSpeciesEntityUUID, "STIMULATION|INHIBITION", "FlatSpecies", 1, 3);
		ByteArrayOutputStream graphMLStream = this.graphMLService.getGraphMLForApocPathReturn(contextNet, false);
		
		
		return new ByteArrayResource(graphMLStream.toByteArray(), "context_" + geneSymbol + ".graphml");
	}

	@Override
	public Resource testGetNet(String networkEntityUUID) {
		Iterable<FlatEdge> getNet = this.flatEdgeRepository.getNetworkContentsFromUUID(networkEntityUUID);
		ByteArrayOutputStream graphMLStream = this.graphMLService.getGraphMLForFlatEdges(getNet, false);
		return new ByteArrayResource(graphMLStream.toByteArray(), "network.graphml");
	}
	
	@Override
	public Resource testGetNet(String networkEntityUUID, List<String> genes) {
		Iterable<FlatSpecies> geneSetFlatSpecies = this.flatSpeciesRepository.getNetworkNodes(networkEntityUUID, genes);
		Iterable<FlatEdge> geneSetFlatEdges = this.flatEdgeRepository.getGeneSet(networkEntityUUID, genes);
		Set<String> seenSymbols = new HashSet<>();
		Iterator<FlatEdge> edgeIter = geneSetFlatEdges.iterator();
		List<FlatSpecies> unconnectedSpecies = new ArrayList<>();
		while(edgeIter.hasNext()) {
			FlatEdge current = edgeIter.next();
			seenSymbols.add(current.getInputFlatSpecies().getSymbol());
			seenSymbols.add(current.getOutputFlatSpecies().getSymbol());
		}
		Iterator<FlatSpecies> speciesIter = geneSetFlatSpecies.iterator();
		while (speciesIter.hasNext()) {
			FlatSpecies current = speciesIter.next();
			if(!seenSymbols.contains(current.getSymbol())) {
				unconnectedSpecies.add(current);
			}
		}
		ByteArrayOutputStream graphMLStream;
		if(unconnectedSpecies.size() == 0) {
			graphMLStream = this.graphMLService.getGraphMLForFlatEdges(geneSetFlatEdges, true);
			
		} else {
			for (FlatSpecies species : unconnectedSpecies) {
				System.out.println("Found unconnected species in geneset: " + species.getSymbol() + ": " + species.getEntityUUID());
			}
			graphMLStream = this.graphMLService.getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(geneSetFlatEdges, unconnectedSpecies, true);
			
		}
		return new ByteArrayResource(graphMLStream.toByteArray(), "geneset.graphml");
	}

	@Override
	public Resource testMultiGeneSubNet(String networkEntityUUID, List<String> geneList, FilterOptions options) {
		List<FlatEdge> allEdges = new ArrayList<>();
		Set<String> seenEdges = new HashSet<>();
		List<FlatSpecies> genes = new ArrayList<>();
		String relationTypesApocString = this.warehouseGraphService.getRelationShipApocString(options, new HashSet<String>());
		String nodeFilterString = "+FlatSpecies";
		if(options.isTerminateAtDrug()) {
			nodeFilterString += "|/Drug";
		}
		Iterable<ApocPathReturnType> multiNodeApocPath = this.flatNetworkMappingRepository.findMultiNodeApocPathInNetworkFromNodeSymbols(networkEntityUUID, geneList, relationTypesApocString, nodeFilterString, options.getMinSize(), options.getMaxSize());
		extractFlatEdgesFromApocPathReturnType(allEdges, seenEdges, multiNodeApocPath);
		for (int i = 0; i != geneList.size(); i++) {
			String gene1 = geneList.get(i);
			String startNodeEntityUUID = this.flatSpeciesRepository.findStartNodeEntityUUID(networkEntityUUID, gene1);
			for (int j = i; j != geneList.size(); j++) {
				String gene2 = geneList.get(j);
				String endNodeEntityUUID = this.flatSpeciesRepository.findStartNodeEntityUUID(networkEntityUUID, gene2);
				Iterable<ApocPathReturnType> dijkstra = this.flatNetworkMappingRepository.apocDijkstraWithDefaultWeight(startNodeEntityUUID, endNodeEntityUUID, relationTypesApocString,  "weight", 1.0f);
				extractFlatEdgesFromApocPathReturnType(allEdges, seenEdges, dijkstra);
			}
		}
		ByteArrayOutputStream graphMLStream = this.graphMLService.getGraphMLForFlatEdges(allEdges, false);
		String fileName = "context_";
		for(String geneSymbol : geneList) {
			fileName += geneSymbol;
			fileName += "-";
		}
		fileName = fileName.substring(0, fileName.length() - 1);
		fileName += ".graphml";
		return new ByteArrayResource(graphMLStream.toByteArray(), fileName);
		
	}

	/**
	 * @param allEdges
	 * @param seenEdges
	 * @param multiNodeApocPath
	 */
	private void extractFlatEdgesFromApocPathReturnType(List<FlatEdge> allEdges, Set<String> seenEdges,
			Iterable<ApocPathReturnType> multiNodeApocPath) {
		Iterator<ApocPathReturnType> iter = multiNodeApocPath.iterator();
		while (iter.hasNext()) {
			ApocPathReturnType current = iter.next();
			for(FlatEdge edge : current.getPathEdges()) {
				if(!seenEdges.contains(edge.getSymbol())) {
					allEdges.add(edge);
					seenEdges.add(edge.getSymbol());
				}
			}
		}
	}


	
}
