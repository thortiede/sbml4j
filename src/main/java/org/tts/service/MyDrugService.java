package org.tts.service;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.GraphEnum.AnnotationName;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.warehouse.MappingNode;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MyDrugService {

	@Autowired
	WarehouseGraphService warehouseGraphService;

	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	
	@Autowired
	FlatEdgeService flatEdgeService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public MappingNode addMyDrugToNetwork(String networkEntityUUID, String myDrugURL) {
		MappingNode mappingNode = this.warehouseGraphService.getMappingNode(networkEntityUUID);
		
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
			FlatSpecies flatSpeciesToNodeSymbol = (FlatSpecies) this.graphBaseEntityService.findByEntityUUID(
					this.warehouseGraphService.getEntityUUIDForSymbolInNetwork(networkEntityUUID, nodeSymbol));
			addSymbolToFlatSpeciesMapping(symbolToFlatSpeciesMap, nodeSymbol, flatSpeciesToNodeSymbol);
			String secondaryNames = (String) flatSpeciesToNodeSymbol.getAnnotation().get(AnnotationName.SECONDARYNAMES.getAnnotationName());
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
								this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(myDrugSpecies);
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
										this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(targetsEdge);
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
		for (String newNodeSymbol : newNodeSymbols) {
			mappingNode.addMappingNodeSymbol(newNodeSymbol);
		}
		for (String newRelationSymbol : newRelationSymbols) {
			mappingNode.addMappingRelatonSymbol(newRelationSymbol);
		}
		this.flatEdgeService.persistAll(myDrugFlatEdgeList, 1);
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
		
		return (MappingNode) this.graphBaseEntityService.persistEntity(mappingNode, 0);
	}


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
	
}
