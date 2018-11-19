package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.tts.model.GraphModel;
import org.tts.model.GraphReaction;
import org.tts.model.GraphSpecies;
import org.tts.model.NodeEdgeList;
import org.tts.model.ReturnModelEntry;
import org.tts.model.ReturnModelOverviewEntry;
import org.tts.service.FileService;
import org.tts.service.FileStorageService;
import org.tts.service.ModelService;
import org.tts.service.NodeEdgeListService;
import org.tts.service.ReactionService;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

@RestController
public class BasicRestController {

	private NodeEdgeListService nodeEdgeListService;
	
	private FileStorageService fileStorageService;
	private ReactionService reactionService;
	private ModelService modelService;
	
	
	private FileService fileService;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Autowired
	public BasicRestController(
			NodeEdgeListService nodeEdgeListService, 
			FileStorageService fileStorageService,
			FileService fileService,
			ReactionService reactionService,
			ModelService modelService) 
	{
		super();
		this.nodeEdgeListService = nodeEdgeListService;
		this.fileStorageService = fileStorageService;
		this.fileService = fileService;
		this.reactionService = reactionService;
		this.modelService = modelService;
	}

	@RequestMapping(value = "/fullnet", method=RequestMethod.GET)
	public ResponseEntity<NodeEdgeList> fullnet() {
		logger.info("Requesting Full Network of Transitions");
		NodeEdgeList fullnet = nodeEdgeListService.getFullNet();
		logger.info("Fetched NodeEdgeList");
		// create a link to self and add to payload
		Link selfLink = linkTo(methodOn(BasicRestController.class).fullnet()).withSelfRel();
		fullnet.add(selfLink);
		
		return new ResponseEntity<NodeEdgeList>(fullnet, HttpStatus.OK);
		
			
	}
	
	@RequestMapping(value = "/sifNet/{fileName}")
	public ResponseEntity<Resource> sifNet(@PathVariable String fileName, HttpServletRequest request) {
		  // Load file as Resource
		
		// for testing, generate fullnet as sif and output
		
		logger.info("Requesting sifNet");
        //Resource resource = fileStorageService.loadFileAsResource(fileName);
        Resource resource = fileStorageService.getSifAsResource(fileService.getFullNet());
        logger.info("Fetched sifResource");
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

	@RequestMapping(value = "/reaction")
	public ResponseEntity<List<List<String>>> retrieveReaction(@RequestParam(value="name") String reactionname, @RequestParam(value="depth") String depth){
		GraphReaction entryReaction = reactionService.getBySbmlNameString(reactionname);
		//Long entryId = entryReaction.getId();
		if(entryReaction==null) return new ResponseEntity<List<List<String>>>(new ArrayList<List<String>>(), HttpStatus.NOT_FOUND);
		List<String> reaction = new ArrayList<String>();
		reaction.add(entryReaction.getSbmlNameString());
		List<GraphSpecies> entryProducts = entryReaction.getProducts();
		List<GraphSpecies> entryReactants = entryReaction.getReactants();
		List<String> reactants = new ArrayList<String>();
		List<String> products = new ArrayList<String>();
		
		for (GraphSpecies prod : entryProducts) {
			products.add(prod.getSbmlNameString());
		}
		for (GraphSpecies react : entryReactants) {
			reactants.add(react.getSbmlNameString());
		}
		List<List<String>> reactionList = new ArrayList<List<String>>();
		reactionList.add(reactants);
		reactionList.add(reaction);
		reactionList.add(products);
		return new ResponseEntity<List<List<String>>>(reactionList, HttpStatus.OK);
		
		
		
	}
	
	@RequestMapping(value="/models")
	public ResponseEntity<List<ReturnModelOverviewEntry>> getAllModels(@RequestParam(value="search", defaultValue="") String searchString) {
	
		String contentType = "application/json";
		
		List<GraphModel> searchResult = modelService.search(searchString);
		List<ReturnModelOverviewEntry> returnList = new ArrayList<ReturnModelOverviewEntry>();
		
		if(searchResult != null) {
			searchResult.forEach(model -> {
				ReturnModelOverviewEntry rmoe = new ReturnModelOverviewEntry(model);
				rmoe.add(linkTo(methodOn(BasicRestController.class).getModelById(rmoe.getModelId().toString())).withSelfRel());
				returnList.add(rmoe);
			});
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.body(returnList);
		} else {
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.body(new ArrayList<ReturnModelOverviewEntry>());
		}
	}
	
	@RequestMapping(value="/model/{modelId}")
	public ResponseEntity<GraphModel> getModelById(@PathVariable String modelId) {
		String contentType = "application/json";
		
		ReturnModelEntry returnModelEntry = new ReturnModelEntry(modelService.getById(Long.valueOf(modelId)));
		if (returnModelEntry.getModel() != null) {
			// add selfLink
			Link selfLink = linkTo(methodOn(BasicRestController.class).getModelById(returnModelEntry.getModelId().toString())).withSelfRel();
			returnModelEntry.add(selfLink);
		}
		GraphModel model = modelService.getById(Long.valueOf(modelId));
		if(model == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.body(model);
		}
	}
	
	
	
}
