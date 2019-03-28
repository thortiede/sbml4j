package org.tts.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.NodeEdgeList;
import org.tts.model.SifFile;
import org.tts.model.api.Input.FilterOptions;
import org.tts.service.FileService;
import org.tts.service.FileStorageService;
import org.tts.service.SimpleModel.NetworkMappingService;

@RestController
public class NetworkMappingController {

	NetworkMappingService networkMappingService;
	FileService fileService;
	FileStorageService fileStorageService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public NetworkMappingController(NetworkMappingService networkMappingService, FileService fileService,
			FileStorageService fileStorageService) {
		super();
		this.networkMappingService = networkMappingService;
		this.fileService = fileService;
		this.fileStorageService = fileStorageService;
	}

	/**
	 * GET /transitionTypes
	 * Uses custom Cypher Query to extract the types of transitions between species are present in the model
	 * These names can be used to filter GET /mapping/ppi
	 * @return the names of the transition types found in the network in this database
	 */
	@RequestMapping(value = "/mapping/ppi/filterOptions", method=RequestMethod.GET)
	public ResponseEntity<Map<String, List<String>>> getFilterOptions() {
		logger.info("Serving GET /mapping/ppi/filterOptions");
		Map<String, List<String>> filterOptions = new HashMap<>();
		// Types of Transitions
		List<String> transitionTypes = networkMappingService.getTransitionTypes();
		filterOptions.put("transitionTypes", transitionTypes);
		
		// Types of Nodes
		// TODO: get types of Nodes to allow filtering
		
		// TODO: more filter options?
		
		// return options
		return new ResponseEntity<Map<String, List<String>>>(filterOptions, HttpStatus.OK);
	}
 	
	/**
	 * GET /mapping/ppi
	 * Queries the database with custom Cypher Queries to get all interactions between QualitativeSpecies
	 * @return SIF formatted List (node1 transition node2) for further processing
	 */
	@RequestMapping(value="/mapping/ppi", method=RequestMethod.GET)
	public ResponseEntity<Resource> getMappingPPI() {
		
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


	/**
	 * Possible approach to realizing filters on network mappings that should be REST compliant (needs to be verified)
	 * example /mapping/ppi
	 * workflow:
	 * 1. GET /mapping/ppi/transitionTypes (or a better endpoint name for transitionTypes) to get list of available filters
	 * 2. POST /mapping/ppi/filter with JSON Payload to set filtering options, generate id for those filter combinations and send to user (with link to endpoint HATEOAS)
	 * 3. GET /mapping/ppi/filters show available filter combinations with description and id (always have standard mapping with all and maybe some most used cases?)
	 * 4. GET /mapping/ppi/filter/{id} or /mapping/ppi/filter?id={id} to get the ppi network with those filters applied 
	 */
	
	@RequestMapping(value = "/mapping/ppi/filter", method=RequestMethod.POST)
	public ResponseEntity<Map<String, List<String>>> defineFilterOption(@RequestBody FilterOptions filterOptions){
		logger.info(filterOptions.toString());
		// Need some way to store these filter options @FILTER_OPTION_STORE
		// check if that set of options does already exist
		// if yes, get id and return uri to requester
		// if no, generate new set and return uri to requester
		return new ResponseEntity<Map<String, List<String>>>(null, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/mapping/ppi/filters", method=RequestMethod.GET)
	public ResponseEntity<Resource> showAvailableFilterOptions(){
		// Query @FILTER_OPTION_STORE
		// return list of filterOptions, with ids and Links to endpoint generating network using that filterOption
		return new ResponseEntity<Resource>(new ByteArrayResource(null), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/mapping/ppi/filter/{id}", method=RequestMethod.GET)
	public ResponseEntity<Resource> getMappingPPIWithFilter(@PathVariable Integer id){
		// Get FilterOption with id @id from @FILTER_OPTION_STORE
		FilterOptions filterOptionsFromId = new FilterOptions(); // actually query the @FILTER_OPTION_STORE here ofc
		// generating dummy data
		List<String> dummyTransitionTypeFilterOption = new ArrayList<>();
		dummyTransitionTypeFilterOption.add("stimulation");
		dummyTransitionTypeFilterOption.add("inhibition");
		filterOptionsFromId.setTransitionTypes(dummyTransitionTypeFilterOption);
		
		// generate ppi with filters applied
		NodeEdgeList ppi = networkMappingService.getProteinInteractionNetwork(filterOptionsFromId.getTransitionTypes());
		Resource resource = getResourceFromNodeEdgeList(ppi);
		logger.info("Fetched sifResource");
		// Try to determine file's content type
	    String contentType =  "application/octet-stream";
	  
	    // only generic contentType as we are not dealing with an actual file serverside,
		// but the file shall only be created at the client side.
		String filename = "unnamed";
		return ResponseEntity.ok()
		        .contentType(MediaType.parseMediaType(contentType))
		        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
		        .body(resource);	
	}
	
	
	
	/**
	 * Convert a NodeEdgeList to a returnable Resource Object
	 * @param nodeEdgeList The nodeEdgeList to convert
	 * @return a sifResource as ByteArrayResource
	 */
	private Resource getResourceFromNodeEdgeList(NodeEdgeList nodeEdgeList) {
		SifFile sifFile = fileService.getSifFromNodeEdgeList(nodeEdgeList);
	
	    //Resource resource = fileStorageService.loadFileAsResource(fileName);
	    Resource resource = fileStorageService.getSifAsResource(sifFile);
		return resource;
	}
	
	
}
