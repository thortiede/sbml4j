package org.tts.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.ApocPathReturnType;
import org.tts.model.api.Output.FlatMappingReturnType;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.warehouse.MappingNode;
import org.tts.repository.common.ExternalResourceEntityRepository;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLSpeciesRepository;
import org.tts.repository.flat.FlatEdgeRepository;
import org.tts.repository.flat.FlatNetworkMappingRepository;
import org.tts.repository.flat.FlatSpeciesRepository;
import org.tts.repository.warehouse.MappingNodeRepository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GraphBaseEntityServiceImpl implements GraphBaseEntityService {

	@Autowired
	GraphBaseEntityRepository graphBaseEntityRepository;

	@Autowired
	FlatNetworkMappingRepository flatNetworkMappingRepository;

	@Autowired
	MappingNodeRepository mappingNodeRepository;
	
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
	FlatEdgeService flatEdgeService;
	
	@Autowired
	SBMLSpeciesRepository sbmlSpeciesRepository;

	@Autowired
	ExternalResourceEntityRepository externalResourceEntityRepository;

	@Autowired
	FlatSpeciesRepository flatSpeciesRepository;
	
	@Autowired
	Session session;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

	private JsonNode runHttpRESTQuery(String baseUrl, String endpointUrl, String requestType, String matchString, String returnString) {
		
		try {
			URL url = new URL(baseUrl + endpointUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod(requestType);
			con.addRequestProperty("X-Stream", "true");
			con.setRequestProperty("Content-Type", "application/json");//; utf-8");
			con.setRequestProperty("Accept", "application/json");//; charset=UTF-8");
			con.setDoOutput(true);
			String jsonInputString = "{\"statements\" : [ { \"statement\":\"match " + matchString;
			jsonInputString += " return ";
			jsonInputString += returnString;
			jsonInputString += ";\" } ]}";
			System.out.println(jsonInputString);
			OutputStream os = con.getOutputStream();
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
			
			if (con.getResponseCode() > 299) {
				// looks like an error
				logger.error("Could not fetch " + jsonInputString + " for " + baseUrl + endpointUrl);
				logger.error(con.getResponseCode() + ": " + con.getResponseMessage());
				return null;
			} else {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
				return objectMapper.readValue(con.getInputStream(), JsonNode.class);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return null;
		} 
	}
	
	@Override
	public Resource testMyDrug(String networkEntityUUID, String myDrugURL) {
		MappingNode mappingNode = this.mappingNodeRepository.findByEntityUUID(networkEntityUUID);
		
		String matchString = "(a)-[t:targets]->(b) where b.symbol in [";
		String returnString = " a.name, a.midrug_id, a.drugbank_id, a.drugbank_version, a.drug_class, a.mechanism_of_action, a.indication, a.categories, b.symbol, b.gene_names, b.name";
		for (String nodeSymbol : mappingNode.getMappingNodeSymbols()) {
			matchString += String.format("\\\"%s\\\", ", nodeSymbol);
		}
		matchString = matchString.substring(0, matchString.length() - 2);
		matchString += "]";
		
		JsonNode drugNodes = this.runHttpRESTQuery(myDrugURL, "/db/data/transaction/", "POST", matchString, returnString);
		if (drugNodes == null) {
			return null;
		}
		JsonNode resultsNode = drugNodes.get("results");
		if (!resultsNode.isArray()) {
			logger.error("Unexpected result type from mydrug query: " + resultsNode.toString());
		}
		Map<String, FlatSpecies> drugNameSpeciesMap = new HashMap<>();
		
		JsonNode columnsArrayNode = resultsNode.get(0).get("columns");
		JsonNode dataArrayNode = resultsNode.get(0).get("data");
		if (!dataArrayNode.isArray()) {
			return null;
		}
		//List<FlatSpecies> mappingSpecies = this.mappingNodeRepository.getMappingFlatSpecies(mappingNode.getBaseNetworkEntityUUID());
		Map<String, FlatSpecies> symbolToFlatSpceciesMap = new HashMap<>();
		for(FlatSpecies fs : this.mappingNodeRepository.getMappingFlatSpecies(mappingNode.getEntityUUID())) {
			symbolToFlatSpceciesMap.put(fs.getSymbol(), fs);
		}
		List<ProvenanceEntity> myDrugFlatSpeciesList = new ArrayList<>();
		List<FlatEdge> myDrugFlatEdgeList = new ArrayList<>();
		
		
		for (int i = 0; i!= dataArrayNode.size(); i++) {
			FlatSpecies myDrugSpecies = new FlatSpecies();
			FlatEdge targetsEdge = null;
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(myDrugSpecies, Arrays.asList(new String[]{"Drug"}));
			boolean foundTarget = false;
			boolean reusingMyDrugSpecies = false;
		
			JsonNode rowNode = dataArrayNode.get(i).get("row");
			for (int j = 0; j!= rowNode.size(); j++) {
				logger.debug(columnsArrayNode.get(j).asText() + ": " + rowNode.get(j).asText());
				if (!reusingMyDrugSpecies && columnsArrayNode.get(j).asText().equals("a.name")) {
					if (drugNameSpeciesMap.containsKey(rowNode.get(j).asText())) {
						logger.debug("Already created this FlatSpecies for myDrug: " + rowNode.get(j).asText() );
						myDrugSpecies = drugNameSpeciesMap.get(rowNode.get(j).asText());
						reusingMyDrugSpecies = true;
					} else {
						logger.debug("New FlatSpecies for myDrug: " + rowNode.get(j).asText() );
						myDrugSpecies.setSymbol(rowNode.get(j).asText());
						myDrugSpecies.setSboTerm("Drug");
						mappingNode.addMappingNodeSymbol(rowNode.get(j).asText());
					}
				} else if (!reusingMyDrugSpecies && columnsArrayNode.get(j).asText().startsWith("a.") && !rowNode.get(j).isNull()) {
					myDrugSpecies.addAnnotation(columnsArrayNode.get(j).asText().substring(2), rowNode.get(j).asText());
					myDrugSpecies.addAnnotationType(columnsArrayNode.get(j).asText().substring(2), "String");
				} else if (!foundTarget && columnsArrayNode.get(j).asText().startsWith("b.") && !rowNode.get(j).isNull()) {
					if(symbolToFlatSpceciesMap.keySet().contains(rowNode.get(j).asText())) {
						targetsEdge = this.flatEdgeService.createFlatEdge("targets");
						this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(targetsEdge);
						targetsEdge.setInputFlatSpecies(myDrugSpecies);
						targetsEdge.setOutputFlatSpecies(symbolToFlatSpceciesMap.get(rowNode.get(j).asText()));
						targetsEdge.setSymbol(myDrugSpecies.getSymbol() + "-TARGETS->"
								+ symbolToFlatSpceciesMap.get(rowNode.get(j).asText()).getSymbol());
						//myDrugSpecies.addRelatedSpecies(symbolToFlatSpceciesMap.get(rowNode.get(j).asText()), "targets");
						mappingNode.addMappingRelatonSymbol(targetsEdge.getSymbol());
						foundTarget = true;
					}
					
				}
					
			}
			if (foundTarget) {
				if (!reusingMyDrugSpecies) {
					//myDrugSpecies.setSboTerm("Drug");
					myDrugFlatSpeciesList.add(myDrugSpecies);
					drugNameSpeciesMap.put(myDrugSpecies.getSymbol(), myDrugSpecies);
				}
				if (targetsEdge != null) {
					myDrugFlatEdgeList.add(targetsEdge);
				}
			} else {
				logger.debug("Could not find target for: " + myDrugSpecies.getSymbol());
			}
		}
		
		Iterable<FlatEdge> persistedMyDrugEdges = this.flatEdgeRepository.save(myDrugFlatEdgeList, 1);
		this.warehouseGraphService.connect((ProvenanceEntity) mappingNode, myDrugFlatSpeciesList, WarehouseGraphEdgeType.CONTAINS);
		Map<String, Object> mappingWarehouseAnnotation = mappingNode.getWarehouse();
		if(mappingWarehouseAnnotation.containsKey("numberofnodes")) {
			logger.debug("Updating Mapping numberOfNodes");
			mappingWarehouseAnnotation.put("numberofnodes", String.valueOf((Integer.parseInt((String) mappingWarehouseAnnotation.get("numberofnodes")) + myDrugFlatSpeciesList.size())));
		}
		if(mappingWarehouseAnnotation.containsKey("numberofrelations")) {
			logger.debug("Updating Mapping numberOfRelations");
			mappingWarehouseAnnotation.put("numberofrelations", String.valueOf((Integer.parseInt((String) mappingWarehouseAnnotation.get("numberofrelations")) + myDrugFlatEdgeList.size())));
		}
		mappingNode.addMappingNodeType("drug");
		mappingNode.addMappingRelationType("TARGETS");
		mappingNode = this.mappingNodeRepository.save(mappingNode, 0);
		
		ByteArrayOutputStream graphMLStream = this.graphMLService.getGraphMLForFlatEdges(persistedMyDrugEdges, true);
		String fileName = "mydrug_";
		fileName += mappingNode.getEntityUUID();
		fileName += ".graphml";
		return new ByteArrayResource(graphMLStream.toByteArray(), fileName);
	}


	
}
