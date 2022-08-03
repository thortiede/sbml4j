/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml4j.Exception.NetworkAlreadyExistsException;
import org.sbml4j.Exception.NetworkDeletionException;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.base.GraphEnum.WarehouseGraphEdgeType;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.sbml4j.service.networks.NetworkService;
import org.sbml4j.service.warehouse.MappingNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for querying a MyDrug Neo4j Instance and adding Drugs to a network
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class MyDrugService {
	
	@Autowired
	FlatEdgeService flatEdgeService;
	
	@Autowired
	FlatSpeciesService flatSpeciesService;

	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Add MyDrug Nodes and targets-edges to the network contained in the <a href="#{@link}">{@link MappingNode}</a> with entityUUID networkEntityUUID
	 * @param user The user associated with the new <a href="#{@link}">{@link MappingNode}</a>
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to be copied or altered
	 * @param myDrugURL The URL of a MyDrug Neo4j REST server
	 * @param networkname An optional name for the resulting network, or prefix to the name (if prefixName is true)
	 * @param prefixName Whether to prefix the existing network name (true) with the given prefixString or replace it with networkname (false)
	 * @param derive Whether to create a copy of the existing network (true), or add data to the network without copying it (false).
	 * @return The <a href="#{@link}">{@link MappingNode}</a> which has the Drug-Node-<a href="#{@link}">{@link FlatSpecies}</a> added.
	 * @throws NetworkAlreadyExistsException If a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists for the given user
	 * @throws NetworkDeletionException If a <a href="#{@link}">{@link MappingNode}</a> with the given name ()or prefixed name) already exists but deletion failed
	 */
	public MappingNode addMyDrugToNetwork(String user, String networkEntityUUID, String myDrugURL, String networkname, boolean prefixName, boolean derive) throws NetworkAlreadyExistsException, NetworkDeletionException {
		
		MappingNode mappingNode = this.networkService.getCopiedOrNamedMappingNode(user, networkEntityUUID, networkname, prefixName, derive, "MyDrugNodesOn_", ProvenanceGraphActivityType.addMyDrugNodes);
		
		StringBuilder matchStringPre = new StringBuilder();
		matchStringPre.append("(a)-[t:targets]->(b) where b.symbol in [");

		StringBuilder matchStringPost = new StringBuilder();
		matchStringPost.append("]");
		
		String returnString = " a.name, a.midrug_id, a.drugbank_id, a.drugbank_version, a.drug_class, a.mechanism_of_action, a.indication, a.categories, b.symbol, b.gene_names, b.name";
		
		//boolean first = true;
		Map<String, Set<FlatSpecies> > symbolToFlatSpeciesMap = new HashMap<>();
		Map<String, FlatSpecies> drugNameSpeciesMap = new HashMap<>();
		List<ProvenanceEntity> myDrugFlatSpeciesList = new ArrayList<>();
		List<FlatEdge> myDrugFlatEdgeList = new ArrayList<>();
		Set<String> newNodeSymbols = new HashSet<>();
		Set<String> newRelationSymbols = new HashSet<>();
		Set<String> mappingNodeSymbols = mappingNode.getMappingNodeSymbols();
		for (String nodeSymbol : mappingNodeSymbols) {
			StringBuilder matchString = new StringBuilder();
			matchString.append("\\\"");
			matchString.append(nodeSymbol);
			matchString.append("\\\"");
			for (FlatSpecies flatSpeciesToNodeSymbol : 
					this.networkService.findAllEntityForSymbolInNetwork(networkEntityUUID, nodeSymbol)) {
				addSymbolToFlatSpeciesMapping(symbolToFlatSpeciesMap, nodeSymbol, flatSpeciesToNodeSymbol);
				String secondaryNames = (String) flatSpeciesToNodeSymbol.getSecondaryNames();
				if (secondaryNames != null) {
					for (String secondaryName : secondaryNames.split(", ")) {
						matchString.append(", ");
						matchString.append("\\\"");
						matchString.append(secondaryName);
						matchString.append("\\\"");
						addSymbolToFlatSpeciesMapping(symbolToFlatSpeciesMap, secondaryName, flatSpeciesToNodeSymbol);
					}
				}
				// do one query here for this one symbol/FlatSpecies
				JsonNode drugNodes = this.runHttpRESTQuery(myDrugURL, "/db/data/transaction/", "POST", matchStringPre.toString() + matchString.toString() + matchStringPost.toString(), returnString);
				if (drugNodes == null) {
					// something went wrong with this query?
					logger.info("NULL return of myDrug query for FlatSpecies with uuid: " + flatSpeciesToNodeSymbol.getEntityUUID());
					continue;
				} else {
					JsonNode resultsNode = drugNodes.get("results");
					if (!resultsNode.isArray()) {
						logger.info("Unexpected result type for FlatSpecies (" + flatSpeciesToNodeSymbol.getEntityUUID() + ") from mydrug query: " + resultsNode.toString());
						continue;
					}
					// check for error here, and whether "columns" is actually present
					JsonNode columnsArrayNode = resultsNode.get(0).get("columns");
					JsonNode dataArrayNode = resultsNode.get(0).get("data");
					if (!dataArrayNode.isArray()) {
						logger.info("dataArrayNode is not an Array for FlatSpecies (" + flatSpeciesToNodeSymbol.getEntityUUID()+ ") of resultsNode: " + resultsNode.toString());
						continue;
					}
					for (int i = 0; i!= dataArrayNode.size(); i++) {
						FlatSpecies myDrugSpecies = null;
						FlatEdge targetsEdge = null;
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
									myDrugSpecies = new FlatSpecies();
									this.graphBaseEntityService.setGraphBaseEntityProperties(myDrugSpecies);
									myDrugSpecies.addLabel("Drug");
									myDrugSpecies.setSymbol(rowNode.get(j).asText());
									myDrugSpecies.setSboTerm("Drug");
									newNodeSymbols.add(rowNode.get(j).asText());
									
									drugNameSpeciesMap.put(rowNode.get(j).asText(), myDrugSpecies);
								}
							} else if (!reusingMyDrugSpecies && columnsArrayNode.get(j).asText().startsWith("a.") && !rowNode.get(j).isNull()) {
								if(myDrugSpecies != null) {
									this.graphBaseEntityService.addAnnotation(myDrugSpecies, columnsArrayNode.get(j).asText().substring(2), "string", rowNode.get(j).asText(), true);
								} else {
									logger.error("Adding annotation to not initialized FlatSpecies " + columnsArrayNode.get(j).asText().substring(2) + " with " + rowNode.get(j).asText());
									continue;
								}
							} else if (!foundTarget && columnsArrayNode.get(j).asText().startsWith("b.") && !rowNode.get(j).isNull()) {
								if (myDrugSpecies != null) {
									if(symbolToFlatSpeciesMap.keySet().contains(rowNode.get(j).asText())) {
										for(FlatSpecies sp : symbolToFlatSpeciesMap.get(rowNode.get(j).asText())) {
											targetsEdge = this.flatEdgeService.createFlatEdge("targets");
											this.graphBaseEntityService.setGraphBaseEntityProperties(targetsEdge);
											targetsEdge.setInputFlatSpecies(myDrugSpecies);
											targetsEdge.setOutputFlatSpecies(sp);
											targetsEdge.setSymbol(myDrugSpecies.getSymbol() + "-TARGETS->"
													+ sp.getSymbol());
											newRelationSymbols.add(targetsEdge.getSymbol());
											//mappingNode.addMappingRelatonSymbol(targetsEdge.getSymbol());
											foundTarget = true;
											myDrugFlatSpeciesList.add(myDrugSpecies);
											drugNameSpeciesMap.put(myDrugSpecies.getSymbol(), myDrugSpecies);
											myDrugFlatEdgeList.add(targetsEdge);
										}
									}
								} else {
									logger.error("Trying to use not initialized FlatSpecies " + columnsArrayNode.get(j).asText().substring(2) + " with " + rowNode.get(j).asText());
									continue;
								}
							}		
						}
						if (!foundTarget) {
							logger.debug("Could not find target for: " + myDrugSpecies.getSymbol());
						}
					}
				}
			}
		}
		for (String newNodeSymbol : newNodeSymbols) {
			mappingNode.addMappingNodeSymbol(newNodeSymbol);
		}
		for (String newRelationSymbol : newRelationSymbols) {
			mappingNode.addMappingRelationSymbol(newRelationSymbol);
		}
		this.flatEdgeService.save(myDrugFlatEdgeList, 1);
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
		
		return this.mappingNodeService.save(mappingNode, 0);
	}

	/**
	 * Adds a Symbol, <a href="#{@link}">{@link FlatSpecies}</a> mapping to the symbolToFlatSpeciesMap Map containing a Set as value
	 * @param symbolToFlatSpeciesMap The Map to add the mapping to
	 * @param symbol The symbol to act as key in the map
	 * @param flatSpecies The <a href="#{@link}">{@link FlatSpecies}</a> to add to the Set
	 */
	private void addSymbolToFlatSpeciesMapping(Map<String, Set<FlatSpecies>> symbolToFlatSpeciesMap, String symbol,
			FlatSpecies flatSpecies) {
		if(symbolToFlatSpeciesMap.containsKey(symbol)) {
			symbolToFlatSpeciesMap.get(symbol).add(flatSpecies);
		} else {
			Set<FlatSpecies> nodeSymbolSet = new HashSet<>();
			nodeSymbolSet.add(flatSpecies);
			symbolToFlatSpeciesMap.put(symbol, nodeSymbolSet);
		}
	}
	
	/**
	 * Run a Query to a REST service
	 * @param baseUrl The basic URL of the MyDrug Neo4j REST service
	 * @param endpointUrl The enpoint to call at the url
	 * @param requestType The Type of Request to run
	 * @param matchString The MATCH string in the Cypher query
	 * @param returnString The RETURN string in the Cypher query
	 * @return The JsonNode that holds the results
	 */
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
			logger.debug(jsonInputString);
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
}
