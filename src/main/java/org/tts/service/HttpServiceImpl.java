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

	
	
}
