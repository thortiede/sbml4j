package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.tts.model.GraphTransition;
import org.tts.model.NodeEdgeList;
import org.tts.model.ReturnModelEntry;
import org.tts.model.ReturnModelOverviewEntry;
import org.tts.service.FileService;
import org.tts.service.FileStorageService;
import org.tts.service.ModelService;
import org.tts.service.NodeEdgeListService;
import org.tts.service.ReactionService;
import org.tts.service.TransitionService;

import uk.ac.ebi.sbo.ws.client.SBOLink;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

@RestController
public class BasicRestController {

	private NodeEdgeListService nodeEdgeListService;
	
	private FileStorageService fileStorageService;
	private ReactionService reactionService;
	private TransitionService transitionService;
	private ModelService modelService;
	
	
	private FileService fileService;
	
	private SBOLink sboLink;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Autowired
	public BasicRestController(
			NodeEdgeListService nodeEdgeListService, 
			FileStorageService fileStorageService,
			FileService fileService,
			ReactionService reactionService,
			TransitionService transitionService,
			ModelService modelService) 
	{
		super();
		this.nodeEdgeListService = nodeEdgeListService;
		this.fileStorageService = fileStorageService;
		this.fileService = fileService;
		this.reactionService = reactionService;
		this.transitionService = transitionService;
		this.modelService = modelService;
		this.sboLink = new SBOLink();
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
	public ResponseEntity<GraphReaction> getReaction(@RequestParam(value="name") String reactionName) {
		return new ResponseEntity<GraphReaction>(reactionService.getBySbmlNameString(reactionName, 2), HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/reactionSimple")
	public ResponseEntity<Map<String, Map<String, List<String>>>> getReactionSimple(@RequestParam(value="name") String reactionname, @RequestParam(value="depth", defaultValue = "1") String depth){
		GraphReaction entryReaction = reactionService.getBySbmlNameString(reactionname);
		logger.info("Retrieved Reaction");
		//Long entryId = entryReaction.getId();
		if(entryReaction==null) return new ResponseEntity<Map<String, Map<String, List<String>>>>(new HashMap<String, Map<String, List<String>>>(), HttpStatus.NOT_FOUND);
		Map<String, List<String>> reaction = new HashMap<String, List<String>>();
		String notesString = entryReaction.getSbmlNotesString();
		List<String> notesList = new ArrayList<String>();
		notesList.add(notesString);
		List<String> reactionNameList = new ArrayList<String>();
		reactionNameList.add(reactionname);
		reactionNameList.add(entryReaction.getSbmlNameString());
		reaction.put("ReactionName", reactionNameList);
		reaction.put("Occurs In", grepOccursIn(notesString));

		List<String> modelList = new ArrayList<String>();
				
		List<GraphModel> models = entryReaction.getModels();
		for (GraphModel model : models) {
			modelList.add(model.getModelName());
		}
		reaction.put("Models it is part of", modelList);
		
		logger.info("Occurs In done.");
		reaction.put("Notes", notesList);
		List<GraphSpecies> entryProducts = entryReaction.getProducts();
		List<GraphSpecies> entryReactants = entryReaction.getReactants();
		Map<String, List<String>> reactants = new HashMap<String, List<String>>();
		Map<String, List<String>> products = new HashMap<String, List<String>>();
		
		List<String> reactantList = new ArrayList<String>();
		List<String> productList =  new ArrayList<String>();
		int i = 1;
		for (GraphSpecies prod : entryProducts) {
			productList.add(prod.getSbmlNameString());
			
			i+=1;
		}
		logger.info("products done.");
		products.put("Products", productList);
		i = 1;
		for (GraphSpecies react : entryReactants) {
			reactantList.add(react.getSbmlNameString());
			
			i+=1;
		}
		logger.info("reactants done.");
		reactants.put("Reactants",reactantList);
		
		Map<String, Map<String, List<String>>> reactionMap = new HashMap<String, Map<String, List<String>>>();
		reactionMap.put("reactants", reactants);
		reactionMap.put("reaction", reaction);
		reactionMap.put("products", products);
		return new ResponseEntity<Map<String, Map<String, List<String>>>>(reactionMap, HttpStatus.OK);
		
		
		
	}
	
	private List<String> grepOccursIn(String notesString) {;
		List<String> occursInStrings = new ArrayList<>();
		int occursInIndex = notesString.indexOf("Occurs in:");
		if (occursInIndex != -1) {
			int liIndex = 0;
			int liCloseIndex = 0;
			int startIndex = occursInIndex;
			while (startIndex != -1) {
				liIndex = notesString.indexOf("<li>", startIndex);
				// if we onlu have this one html list in the notesString this is fine
				// if not, we get the next list here as well.
				// so we should check, whether that occurs List is closed in between with </ul>
				if(liIndex != -1) {
					liCloseIndex = notesString.indexOf("</li>", liIndex);
					// Found one Pathway it occurs in
					if (liCloseIndex != -1) {
						occursInStrings.add(notesString.substring(liIndex + 4, liCloseIndex));
						startIndex = liCloseIndex;
					}
				} else {
					startIndex = -1;
				}
			}
		}
		return occursInStrings;
	}

	@RequestMapping(value="/transition")
	public ResponseEntity<GraphTransition> getTransition(@RequestParam(value="metaId") String transitionMetaId) {
		return new ResponseEntity<GraphTransition>(transitionService.getByMetaid(transitionMetaId), HttpStatus.OK);
	}
	@RequestMapping(value="/transitionSimple")
	public ResponseEntity<Map<String, String>> getTransitionSimple(@RequestParam(value="metaId") String transitionMetaId) {
		
		GraphTransition transition = transitionService.getByMetaid(transitionMetaId);
		
		Map<String, String> transitionSimple = new HashMap<String, String>();
		transitionSimple.put("metaId", transitionMetaId);
		transitionSimple.put("QualSpeciesOne", transition.getQualSpeciesOneSbmlNameString());
		transitionSimple.put("QualSpeciesTwo", transition.getQualSpeciesTwoSbmlNameString());
		transitionSimple.put("SBO-Term", transition.getSbmlSBOTerm());
		transitionSimple.put("Translated", sboLink.getTerm(transition.getSbmlSBOTerm()).getName());
		//List<String> modelNameList = new ArrayList<String>();
		int i = 1;
		for(GraphModel model : transition.getModels()) {
			transitionSimple.put(i + "_In Pathway",model.getModelName());
		}
		
		
		
		
		return new ResponseEntity<Map<String, String>>(transitionSimple, HttpStatus.OK);
	}
	
	
	
	
	
	
	@RequestMapping(value="/models")
	public ResponseEntity<List<ReturnModelOverviewEntry>> getAllModels(@RequestParam(value="search", defaultValue="") String searchString) {
	
		String contentType = "application/json";
		
		List<GraphModel> searchResult = modelService.search(searchString);
		List<ReturnModelOverviewEntry> returnList = new ArrayList<ReturnModelOverviewEntry>();
		
		if(searchResult != null) {
			searchResult.forEach(model -> {
				ReturnModelOverviewEntry rmoe = new ReturnModelOverviewEntry(model);
				rmoe.add(linkTo(methodOn(BasicRestController.class)
									.getModelById(rmoe.getModelId().toString())
								)
								.withSelfRel()
						);
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
	public ResponseEntity<ReturnModelEntry> getModelById(@PathVariable String modelId) {
		String contentType = "application/json";
		
		ReturnModelEntry returnModelEntry = new ReturnModelEntry(modelService.getById(Long.valueOf(modelId)));
		if (returnModelEntry.getModel() != null) {
			// add selfLink
			Link selfLink = linkTo(methodOn(BasicRestController.class).getModelById(returnModelEntry.getModelId().toString())).withSelfRel();
			returnModelEntry.add(selfLink);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(contentType))
					.body(returnModelEntry);
		} else {
				return ResponseEntity.notFound().build();
		}
	}
	
	
	
}
