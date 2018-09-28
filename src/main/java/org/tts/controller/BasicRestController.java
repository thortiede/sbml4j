package org.tts.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.NodeEdgeList;
import org.tts.service.FileService;
import org.tts.service.FileStorageService;
import org.tts.service.NodeEdgeListService;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

@RestController
public class BasicRestController {

	private NodeEdgeListService nodeEdgeListService;
	
	private FileStorageService fileStorageService;
	
	
	private FileService fileService;
	
	
	@Autowired
	public BasicRestController(NodeEdgeListService nodeEdgeListService, FileStorageService fileStorageService,
			FileService fileService) {
		super();
		this.nodeEdgeListService = nodeEdgeListService;
		this.fileStorageService = fileStorageService;
		this.fileService = fileService;
	}

	@RequestMapping(value = "/fullnet", method=RequestMethod.GET)
	public ResponseEntity<NodeEdgeList> fullnet() {
		
		NodeEdgeList fullnet = nodeEdgeListService.getFullNet();
		// create a link to self and add to payload
		Link selfLink = linkTo(methodOn(BasicRestController.class).fullnet()).withSelfRel();
		fullnet.add(selfLink);
		
		return new ResponseEntity<NodeEdgeList>(fullnet, HttpStatus.OK);
		
			
	}
	
	@RequestMapping(value = "/sifNet/{fileName}")
	public ResponseEntity<Resource> sifNet(@PathVariable String fileName, HttpServletRequest request) {
		  // Load file as Resource
		
		// for testing, generate fullnet as sif and output
		
		
        //Resource resource = fileStorageService.loadFileAsResource(fileName);
        Resource resource = fileStorageService.getSifAsResource(fileService.getFullNet());
        // Try to determine file's content type
        String contentType = null;
      
        // only generic contentType as we are not dealing with an actual file serverside,
        // but the file shall only be created at the client side.
        contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
	
}
