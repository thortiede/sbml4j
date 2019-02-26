package org.tts.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HttpServiceImpl implements HttpService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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

}
