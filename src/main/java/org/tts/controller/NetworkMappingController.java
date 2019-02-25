package org.tts.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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