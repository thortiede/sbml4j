package org.tts.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.warehouse.MappingNode;
import org.tts.repository.warehouse.MappingNodeRepository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HttpServiceImpl implements HttpService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private MappingNodeRepository mappingNodeRepository;
	@Autowired
	private SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	
	@Override
	public List<String> getGeneNamesFromKeggURL(String resource) {
		List<String> geneNames = new ArrayList<>();
		String[] resourceParts =  resource.split("/");
		String identifier = resourceParts[resourceParts.length - 1];
		try {
			URL url = new URL("http://rest.kegg.jp/get/" + identifier);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			
			BufferedReader streamReader = null;
			 
			if (status > 299) {
			    logger.error("Cannot get kegg.genes information for " + identifier + ": " + con.getResponseMessage());
			    return geneNames;
			} else {
				streamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			
			String inputLine;
			String[] splitted;
			String lastLine = "";
			while ((inputLine = streamReader.readLine()) != null) {
				logger.debug(inputLine);
				if(inputLine.startsWith("NAME")) {
					lastLine = "NAME";
					splitted = inputLine.split("\\s+");
					if(splitted.length > 1) {
						for (int i = 1; i!= splitted.length; i++) {
							if (splitted[i].endsWith(",")) {
								geneNames.add(splitted[i].substring(0, splitted[i].length() - 1));
							} else {
								geneNames.add(splitted[i]);
							}
						}
					}
				} else if(lastLine.equals("NAME") && inputLine.startsWith(" ")) {
					splitted = inputLine.split("\\s+");
					for (int i = 0; i!= splitted.length; i++) {
						if (splitted[i].endsWith(",")) {
							geneNames.add(splitted[i].substring(0, splitted[i].length() - 1));
						} else {
							geneNames.add(splitted[i]);
						}
					}
				} else if(lastLine.equals("NAME") && !inputLine.startsWith(" ")) {
					lastLine = "";
					break;
				}
			}
			return geneNames;
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ExternalResourceEntity setCompoundAnnotationFromResource(String resource, ExternalResourceEntity entity) {
		String[] resourceParts =  resource.split("/");
		String identifier = resourceParts[resourceParts.length - 1];
		try {
			URL url = new URL("http://rest.kegg.jp/get/" + identifier);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int status = con.getResponseCode();

			BufferedReader streamReader = null;

			if (status > 299) {
				logger.error("Cannot get kegg.compound information for " + identifier + ": " + con.getResponseMessage());
				return entity;
			} else {
				streamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}

			String inputLine;
			String[] splitted;
			String secNameLine;
			String lastLine = "";
			List<String> names = new ArrayList<>();
			String formula;
			String exactMass;
			String molWeight;
			while ((inputLine = streamReader.readLine()) != null) {
				logger.debug(inputLine);
		
				
				if(inputLine.startsWith("NAME")) {
					lastLine = "NAME";
					splitted = inputLine.split("\\s+");
					if(splitted.length > 1) {
						for (int i = 1; i!= splitted.length; i++) {
							if (splitted[i].endsWith(",") || splitted[i].endsWith(";")) {
								names.add(splitted[i].substring(0, splitted[i].length() - 1));
							} else {
								names.add(splitted[i]);
							}
						}
					}
				} else if(lastLine.equals("NAME") && inputLine.startsWith(" ")) {
					//splitted = inputLine.split("\\s+");
					secNameLine = inputLine.trim();
					if (secNameLine.endsWith(",") || secNameLine.endsWith(";")) {
						names.add(secNameLine.substring(0, secNameLine.length() - 1));
					} else {
						names.add(secNameLine);
					}
				} else if(inputLine.startsWith("FORMULA")) {
					lastLine = "FORMULA";
					splitted = inputLine.split("\\s+");
					if(splitted.length == 2) {
						if (splitted[1].endsWith(",") || splitted[1].endsWith(";")) {
							formula = splitted[1].substring(0, splitted[1].length() - 1);
						} else {
							formula = splitted[1];
						}
						entity.addAnnotation(lastLine, formula);
						entity.addAnnotationType(lastLine, "String");
					} else {
						// Formula should have exactly two elements
					}
				} else if(inputLine.startsWith("EXACT_MASS")) {
					lastLine = "EXACT_MASS";
					splitted = inputLine.split("\\s+");
					if(splitted.length == 2) {
						if (splitted[1].endsWith(",") || splitted[1].endsWith(";")) {
							exactMass = splitted[1].substring(0, splitted[1].length() - 1);
						} else {
							exactMass = splitted[1];
						}
						entity.addAnnotation(lastLine, exactMass);
						entity.addAnnotationType(lastLine, "String");
					} else {
						// exactMass should have exactly two elements
					}
				} else if(inputLine.startsWith("MOL_WEIGHT")) {
					lastLine = "MOL_WEIGHT";
					splitted = inputLine.split("\\s+");
					if(splitted.length == 2) {
						if (splitted[1].endsWith(",") || splitted[1].endsWith(";")) {
							molWeight = splitted[1].substring(0, splitted[1].length() - 1);
						} else {
							molWeight = splitted[1];
						}
						entity.addAnnotation(lastLine, molWeight);
						entity.addAnnotationType(lastLine, "String");
					} else {
						// molWeight should have exactly two elements
					}
				}
			}
			entity.setName(names.remove(0));
			
			if (names.size() > 0) {
				String[] secNames = new String[names.size()];
				for (int i = 0; i != names.size(); i++) {
					secNames[i] = names.get(i);
				}
				entity.setSecondaryNames(secNames);
			}
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return entity;
	}

	@Override
	public List<FlatSpecies> getMyDrugCompoundsForNetwork(String mydrugURL, MappingNode mappingNode) {
		Set<String> mappingNodeSymbols = mappingNode.getMappingNodeSymbols();
		String logString = "";
		for(String symbol : mappingNodeSymbols) {
			if (symbol == null)
				logger.info("Symbol is null!!");
			else {
				logString = logString.concat(symbol + ", ");
			}
		}
		logger.info("NodeSymbols are: " + logString);
		//List<FlatSpecies> mappingSpecies = this.mappingNodeRepository.getMappingFlatSpecies(mappingNode.getBaseNetworkEntityUUID());
		Map<String, FlatSpecies> symbolToFlatSpceciesMap = new HashMap<>();
		for(FlatSpecies fs : this.mappingNodeRepository.getMappingFlatSpecies(mappingNode.getEntityUUID())) {
			symbolToFlatSpceciesMap.put(fs.getSymbol(), fs);
		}
		List<FlatSpecies> myDrugFlatSpeciesList = new ArrayList<>();
	/*	try {
			String rootCall = "db/data/";
			URL url = new URL(mydrugURL + rootCall);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.addRequestProperty("Accept", "application/json; charset=UTF-8");
			int status = con.getResponseCode();
			
			BufferedReader streamReader = null;
			 
			if (status > 299) {
			    logger.error("Cannot get mydrug database information for " + mydrugURL + ": " + con.getResponseMessage());
			    return new ArrayList<FlatSpecies>();
			} else {
				streamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				
			}
			String inputLine;
			while ((inputLine = streamReader.readLine()) != null) {
				logger.debug(inputLine);
			}			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		try {
			String rootCall = "db/data/labels";
			URL url = new URL(mydrugURL + rootCall);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.addRequestProperty("Accept", "application/json; charset=UTF-8");
			int status = con.getResponseCode();
			
			BufferedReader streamReader = null;
			 
			if (status > 299) {
			    logger.debug("Cannot get mydrug database information for " + mydrugURL + ": " + con.getResponseMessage());
			    return new ArrayList<FlatSpecies>();
			} else {
				logger.debug("Status: " + status + ", responseMessage: " + con.getResponseMessage());
				streamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				
			}
			String inputLine;
			while ((inputLine = streamReader.readLine()) != null) {
				logger.debug(inputLine);
			}			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		*/
		
		try {
			String rootCall = "db/data/transaction/";
			URL url = new URL(mydrugURL + rootCall);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.addRequestProperty("X-Stream", "true");
			con.setRequestProperty("Content-Type", "application/json");//; utf-8");
			con.setRequestProperty("Accept", "application/json");//; charset=UTF-8");
			con.setDoOutput(true);
			String jsonInputString = "{\"statements\" : [ { \"statement\": \"match (a)-[t:targets]->(b) where b.symbol in [";
			
			for (String nodeSymbol : mappingNodeSymbols) {
				jsonInputString += String.format("\\\"%s\\\", ", nodeSymbol);	
			}
			jsonInputString = jsonInputString.substring(0, jsonInputString.length() - 2);
			jsonInputString += "] return a.name, a.midrug_id, a.drugbank_id, a.drugbank_version, a.drug_class, a.mechanism_of_action, a.indication, a.categories, b.symbol, b.gene_names, b.name;\" } ]}";
			logger.debug("Using body: " + jsonInputString);
			try(OutputStream os = con.getOutputStream()) {
			    byte[] input = jsonInputString.getBytes("utf-8");
			    os.write(input, 0, input.length);           
			}
			
			int status = con.getResponseCode();
			logger.debug("ResponseCode: " + status + " with body: " + con.getResponseMessage());
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			
			if (status > 299) {
			    logger.debug("Cannot get mydrug database information for " + mydrugURL + ": " + con.getResponseCode() + ": " + con.getResponseMessage());
			    return new ArrayList<FlatSpecies>();
			} else {
				// symbolToFlatSpceciesMap
				Map<String, FlatSpecies> drugNameSpeciesMap = new HashMap<>();
				JsonNode jsnNde = objectMapper.readValue(con.getInputStream(), JsonNode.class);//.get("results").get("data");
				JsonNode resultsNode = jsnNde.get("results");
				if(resultsNode.isArray()) {
					JsonNode columnsArrayNode = resultsNode.get(0).get("columns");
					JsonNode dataArrayNode = resultsNode.get(0).get("data");
					//logger.debug(dataNode.toString());
					if (dataArrayNode.isArray()) {
						for (int i = 0; i!= dataArrayNode.size(); i++) {
							FlatSpecies myDrugSpecies = new FlatSpecies();
							boolean foundTarget = false;
							boolean reusingMyDrugSpecies = false;
							this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(myDrugSpecies);
							JsonNode dataNode = dataArrayNode.get(i);
							JsonNode rowNode = dataNode.get("row");
							if(rowNode.isArray() && rowNode.size() == columnsArrayNode.size()) {
								for (int j = 0; j!= rowNode.size(); j++) {
									logger.debug(columnsArrayNode.get(j).asText() + ": " + rowNode.get(j).asText());
									if(!reusingMyDrugSpecies && columnsArrayNode.get(j).asText().equals("a.name")) {
										if(drugNameSpeciesMap.containsKey(rowNode.get(j).asText())) {
											logger.debug("Already created this FlatSpecies for myDrug: " + rowNode.get(j).asText() );
											myDrugSpecies = drugNameSpeciesMap.get(rowNode.get(j).asText());
											reusingMyDrugSpecies = true;
										}
										myDrugSpecies.setSymbol(rowNode.get(j).asText());
									} else if(!reusingMyDrugSpecies && columnsArrayNode.get(j).asText().startsWith("a.") && !rowNode.get(j).isNull()) {
										myDrugSpecies.addAnnotation(columnsArrayNode.get(j).asText().substring(2), rowNode.get(j).asText());
										myDrugSpecies.addAnnotationType(columnsArrayNode.get(j).asText().substring(2), "String");
									} else if (!foundTarget && columnsArrayNode.get(j).asText().startsWith("b.") && !rowNode.get(j).isNull()) {
										if(symbolToFlatSpceciesMap.keySet().contains(rowNode.get(j).asText())) {
											myDrugSpecies.addRelatedSpecies(symbolToFlatSpceciesMap.get(rowNode.get(j).asText()), "targets");
											foundTarget = true;
										}
									}
								}
								if (foundTarget) {
									if (!reusingMyDrugSpecies) {
										myDrugSpecies.setSboTerm("Drug");
										myDrugFlatSpeciesList.add(myDrugSpecies);
										drugNameSpeciesMap.put(myDrugSpecies.getSymbol(), myDrugSpecies);
									}
									
								} else {
									logger.debug("Could not find target for: " + myDrugSpecies.getSymbol());
								}
							}
						}
					}
				}
				return myDrugFlatSpeciesList;
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
		
		
	}
	
}
