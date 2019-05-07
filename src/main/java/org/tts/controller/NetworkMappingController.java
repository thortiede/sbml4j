package org.tts.controller;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.SifFile;
import org.tts.service.FileService;
import org.tts.service.FileStorageService;
import org.tts.service.GraphMLService;
import org.tts.service.NetworkMappingService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
@RestController
public class NetworkMappingController {

	NetworkMappingService networkMappingService;
	FileService fileService;
	FileStorageService fileStorageService;
	GraphMLService graphMLService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public NetworkMappingController(NetworkMappingService networkMappingService, 
									FileService fileService,
									FileStorageService fileStorageService,
									GraphMLService graphMLService) {
		super();
		this.networkMappingService = networkMappingService;
		this.fileService = fileService;
		this.fileStorageService = fileStorageService;
		this.graphMLService = graphMLService;
	}

	/**
	 * GET /filterOptions
	 * Uses custom Cypher Query to extract the types of transitions between species are present in the model
	 * These names can be used to filter GET /mapping/ppi
	 * @return the names of the transition types found in the network in this database
	 */
	@RequestMapping(value = "/filterOptions", method=RequestMethod.GET)
	public ResponseEntity<FilterOptions> getFilterOptions() {
		logger.info("Serving GET /filterOptions");
		FilterOptions fullOptions = this.networkMappingService.getFullFilterOptions();
		// return options
		return new ResponseEntity<FilterOptions>(fullOptions, HttpStatus.OK);
	}
 	
	
	/**
	 * GET /mapping/ppi
	 * Queries the database with custom Cypher Queries to get all interactions between QualitativeSpecies
	 * @return SIF formatted List (node1 transition node2) for further processing
	 */
	/*@RequestMapping(value="/mapping/ppi", method=RequestMethod.GET)
	public ResponseEntity<Resource> getMappingPPI() {
		
		/*
		 * The idea is:
		 * Have a caching feature that saves NodeEdgeLists for export as SiF
		 * This cache is fed by full flatModel network mappings that got persisted in the database
		 * if the mapping is not yet created we build it and persist in the database
		 * if possible add labels to the nodes to denote to which network they belong.
		 * Alternatively create a special node which denotes the filter options used for that network
		 * and connect all flatSpecies nodes to it.
		 */
		/*
		NodeEdgeList fullList = networkMappingService.getProteinInteractionNetwork();
		
		
		
		Resource resource = getResourceFromNodeEdgeList(fullList);
	    logger.info("Fetched sifResource");
	    // Try to determine file's content type
	    String contentType = null;
	  
	    // only generic contentType as we are not dealing with an actual file serverside,
	    // but the file shall only be created at the client side.
	    contentType = "application/octet-stream";
	    String filename = "unnamed";
	    return ResponseEntity.ok()
	            .contentType(MediaType.parseMediaType(contentType))
	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
	            .body(resource);
	}
	*/

	/**
	 * Possible approach to realizing filters on network mappings that should be REST compliant (needs to be verified)
	 * example /mapping/ppi
	 * workflow:
	 * 1. GET /filterOptions (or a better endpoint name for transitionTypes) to get list of available filters
	 * 2. POST /mapping with JSON Payload to set filtering options, generate id for those filter combinations and send to user (with link to endpoint HATEOAS)
	 * 3. GET /mappings show available filter combinations with description and id (always have standard mapping with all and maybe some most used cases?)
	 * 4. GET /mapping/{id} to get the ppi network with those filters applied 
	 */
	
	@RequestMapping(value = "/mapping", method=RequestMethod.POST)
	public ResponseEntity<Map<String, FilterOptions>> defineFilterOption(@RequestBody FilterOptions filterOptions){
		if(filterOptions == null) {
			logger.info("Serving POST /mapping, but filterOptions was null");
			return new ResponseEntity<Map<String, FilterOptions>>(HttpStatus.BAD_REQUEST);
		}
		if(filterOptions.getNetworkType() == null) {
			filterOptions.setNetworkType("");
		}
		if(filterOptions.getTransitionTypes() == null) {
			filterOptions.setTransitionTypes(new ArrayList<>());
		}
		if(filterOptions.getNodeTypes() == null) {
			filterOptions.setNodeTypes(new ArrayList<>());
		}
		if (filterOptions.getNetworkType().equals("") && filterOptions.getTransitionTypes().size() == 0 && filterOptions.getNodeTypes().size() == 0) {
			logger.info("Serving POST /mapping, but filterOptions were empty");
			return new ResponseEntity<Map<String, FilterOptions>>(HttpStatus.BAD_REQUEST);
		}
		logger.info("Serving POST /mapping for: " + filterOptions.toString());
		
		// check if that set of options does already exist
		FilterOptions retFilterOptions;
		Map<String, FilterOptions> retFilterOptionsMap  = new HashMap<String, FilterOptions>();
		if(this.networkMappingService.filterOptionsExists(filterOptions)) {
			// if yes, get the existing set
			retFilterOptions = this.networkMappingService.getExistingFilterOptions(filterOptions);
			if(retFilterOptions == null) {
				logger.info("Serving POST /mapping, filterOptions existed, but retrieving them failed");
				return new ResponseEntity<Map<String, FilterOptions>>(HttpStatus.BAD_REQUEST);
			}
		} else {
			// if no, generate new set and generate mapping (TODO: Generate the mapping in a separate thread)
			retFilterOptions = this.networkMappingService.addNetworkMapping(filterOptions);
		}
		// add self link
		retFilterOptions.add(linkTo(methodOn(NetworkMappingController.class).getMappingWithFilter(retFilterOptions.getMappingUuid(), "graphml")).withSelfRel());
		// package in Map to return to requerster
		retFilterOptionsMap.put(retFilterOptions.getMappingUuid(), retFilterOptions);
		return new ResponseEntity<Map<String, FilterOptions>>(retFilterOptionsMap, HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/mappings", method=RequestMethod.GET)
	public ResponseEntity<Map<String, FilterOptions>> showAvailableFilterOptions(){
		
		// return list of filterOptions, with ids and Links to endpoint generating network using that filterOption
		Map<String, FilterOptions> allFilterOptions = this.networkMappingService.getFilterOptions();
		
		for (String uuid : allFilterOptions.keySet()) {
			FilterOptions currentOptions = allFilterOptions.get(uuid);
			currentOptions.add(linkTo(methodOn(NetworkMappingController.class).getMappingWithFilter(uuid, "graphml")).withSelfRel());
		}
		
		return new ResponseEntity<Map<String, FilterOptions>>(allFilterOptions, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/mapping/{uuid}", method=RequestMethod.GET)
	public ResponseEntity<Resource> getMappingWithFilter(@PathVariable String uuid,
															@RequestParam(value = "format", defaultValue = "graphml") String format ){
		// TODO: Check here whether the mapping has been created, or just the filterOptions posted.
		
		// Get FilterOption with id @id from @FILTER_OPTION_STORE
		FilterOptions filterOptionsFromId = this.networkMappingService.getFilterOptions(uuid);
		if(filterOptionsFromId == null) {
			logger.info("Serving GET /mapping/{uuid} with uuid: " + uuid + ", but no filterOptions-Entry was found under that uuid");
			return new ResponseEntity<Resource>(HttpStatus.BAD_REQUEST);
		}
		logger.info("Serving GET /mapping/{uuid} with uuid: " + uuid);
		NodeEdgeList flatNetwork = this.networkMappingService.getMappingFromFilterOptions(filterOptionsFromId);
		logger.info("Retrieved flat Network representation");
		// generating dummy data
		/*List<String> dummyTransitionTypeFilterOption = new ArrayList<>();
		dummyTransitionTypeFilterOption.add("stimulation");
		dummyTransitionTypeFilterOption.add("inhibition");
		filterOptionsFromId.setTransitionTypes(dummyTransitionTypeFilterOption);
		
		// generate ppi with filters applied
		NodeEdgeList flatNetwork = networkMappingService.getProteinInteractionNetwork(filterOptionsFromId.getTransitionTypes());*/
		//Resource resource = getResourceFromNodeEdgeList(flatNetwork, "sif");
		Resource resource = getResourceFromNodeEdgeList(flatNetwork, format);
		if (resource != null) {
			logger.info("Converted flatNetwork to Resource");
			// Try to determine file's content type
		    String contentType =  "application/octet-stream";
		  
		    // only generic contentType as we are not dealing with an actual file serverside,
			// but the file shall only be created at the client side.
			String filename = "unnamed";
			return ResponseEntity.ok()
			        .contentType(MediaType.parseMediaType(contentType))
			        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
			        .body(resource);	
		} else {
			return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT);
		}
		
	}
	
	
	
	/**
	 * Convert a NodeEdgeList to a returnable Resource Object
	 * @param nodeEdgeList The nodeEdgeList to convert
	 * @return a sifResource as ByteArrayResource
	 */
	private Resource getResourceFromNodeEdgeList(NodeEdgeList nodeEdgeList, String type) {
		switch (type) {
		case "sif":
		
			SifFile sifFile = fileService.getSifFromNodeEdgeList(nodeEdgeList);
		
		    //Resource resource = fileStorageService.loadFileAsResource(fileName);
			if(sifFile != null) {
			    Resource resource = fileStorageService.getSifAsResource(sifFile);
			    return resource;
			} else {
				return null;
			}
		case "graphml":
			String graphMLString = graphMLService.getGraphMLString(nodeEdgeList);
			
			return new ByteArrayResource(graphMLString.getBytes(), "network.graphml");
			
		default:
			return null;		
		}
	}

	
	

	
}
