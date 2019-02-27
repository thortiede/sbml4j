package org.tts.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.NodeEdgeList;
import org.tts.model.SifFile;
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
	@RequestMapping(value = "/transitionTypes", method=RequestMethod.GET)
	public ResponseEntity<List<String>> getTransitionTypes() {
		List<String> transitionTypes = networkMappingService.getTransitionTypes();
		return new ResponseEntity<List<String>>(transitionTypes, HttpStatus.OK);
	}
 	
	/**
	 * GET /mapping/ppi
	 * Queries the database with custom Cypher Queries to get all interactions between QualitativeSpecies
	 * @return SIF formatted List (node1 transition node2) for further processing
	 */
	@RequestMapping(value="/mapping/ppi", method=RequestMethod.GET)
	public ResponseEntity<Resource> getMappingPPI() {
		
		NodeEdgeList fullList = networkMappingService.getProteinInteractionNetwork();
		SifFile sifFile = fileService.getSifFromNodeEdgeList(fullList);
	
	    //Resource resource = fileStorageService.loadFileAsResource(fileName);
	    Resource resource = fileStorageService.getSifAsResource(sifFile);
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
	
}
