package org.tts.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.NodeEdgeList;
import org.tts.service.NodeEdgeListService;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class BasicRestController {

	@Autowired
	NodeEdgeListService nodeEdgeListService;
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@RequestMapping(value = "/fullnet", method=RequestMethod.GET)
	public ResponseEntity<Resource<NodeEdgeList>> fullnet() {
		
		NodeEdgeList fullnet = nodeEdgeListService.getFullNet();
		
		return new ResponseEntity<Resource<NodeEdgeList>>(new Resource<NodeEdgeList>(fullnet), HttpStatus.OK);
			//	linkTo()
			//	linkTo(methodOn(BasicRestController.class).fullnet().withSelfRel()));
	}
	
	@RequestMapping(value = "/sifNet/{fileName}")
	public ResponseEntity<Resource> sifNet(@PathVariable String fileName, HttpServletRequest request) {
		  // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            //logger.info("Could not determine file type.");
        	ex.printStackTrace();
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
	}
}
