package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.File;

import java.time.Duration;
import java.time.Instant;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.GraphCompartment;
import org.tts.model.GraphModel;
import org.tts.model.GraphQualitativeSpecies;
import org.tts.model.GraphReaction;
import org.tts.model.GraphSpecies;
import org.tts.model.GraphTransition;
import org.tts.model.ReturnModelOverviewEntry;
import org.tts.service.CompartmentService;
import org.tts.service.ModelService;
import org.tts.service.QualitativeSpeciesService;
import org.tts.service.ReactionService;
import org.tts.service.SpeciesService;
import org.tts.service.TransitionService;

import org.neo4j.ogm.exception.OptimisticLockingException;

@RestController
public class PersistSBMLRestController {

	private final ModelService modelService;
	private final SpeciesService speciesService;
	private final QualitativeSpeciesService qualitativeSpeciesService;
	private final CompartmentService compartmentService;
	private final ReactionService reactionService;
	private final TransitionService transitionService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public PersistSBMLRestController(ModelService modelService, SpeciesService speciesService, CompartmentService compartmentService, ReactionService reactionService, QualitativeSpeciesService qualitativeSpeciesService, TransitionService transitionService) {
		this.modelService = modelService;
		this.speciesService = speciesService;
		this.qualitativeSpeciesService = qualitativeSpeciesService;
		this.compartmentService = compartmentService;
		this.reactionService = reactionService;
		this.transitionService = transitionService;

	}
	
	
	@RequestMapping(value = "/sbmlfolder", method = RequestMethod.GET)
	public int persistSBMLFolder(@RequestParam(value="name") String name, @RequestParam(value="organism") String organism) {
		boolean createQual = false;
		logger.debug("reading dir ");
		String directory = name;
		logger.debug(directory);
		if(directory.contains("non-metabolic")) { // TODO: this should extracted from the model itself, see FileController on how to.
			createQual = true;
		} else {
			createQual = false;
		}

		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();
		int cnt = 0;
		// TODO: This should be outside the for loop, or not?
		// I moved them here, lets what happens #reminder
		SBMLReader reader = new SBMLReader();
		SBMLDocument doc = null;
		int numFiles = listOfFiles.length;
		int curFileNum = 0;
		logger.info("Begin Processing " + numFiles + " files/models");
		for (File file : listOfFiles) {
		    if (file.isFile() && !file.getName().equals(".DS_Store")) {
		    	curFileNum += 1;
		    	logger.info("Processing File: " + file.getAbsolutePath() + "(" + curFileNum + "/" + numFiles + ")");	    	
		 
		    	try {
		    		
		    		
		    		
		    		//String localDir = "/Users/ttiede/Documents/workspace_aug2018/testdata/sbml4j/sbml/non-metabolic/hsa/";
		    		//String filepath = directory + name;
		    		//logger.info("Filepath is :" filepath);
		    		
	    			GraphModel graphModel;
	    			doc = reader.readSBML(file.getAbsolutePath());// this can be changed to the static call to SBMLReader
	    			Model model = doc.getModel();
	    			graphModel = persistSBMLModel(model, file.getName(), organism, createQual);
	    			if (graphModel != null) {
	    				cnt = cnt + 1;
	    			}
		    	} catch (XMLStreamException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (Exception e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} finally {
					logger.info("finished persisting model " + doc.getModel().getName());
				}
	    		
		    	
		    }
		}
		logger.info("Finished Processing " + cnt + " models.");
		return cnt;
	}
	
	@RequestMapping(value = "/sbmlfile", method = RequestMethod.GET)
	public GraphModel persistSBML(@RequestParam(value="name") String name, @RequestParam(value="organism") String organism) {
		SBMLReader reader = new SBMLReader(); // this can be changed to the static call to SBMLReader
		SBMLDocument doc;
		
		//String localDir = "/Users/ttiede/Documents/workspace_aug2018/testdata/sbml4j/sbml/metabolic/testing/hsa/";
		String filepath = name;
		logger.info("Filepath is" + filepath);
		File f = new File(filepath);
		String fileName = f.getName();
		try {
			GraphModel graphModel;
			doc = reader.readSBML(filepath);
			Model model = doc.getModel();
			if(filepath.contains("non-metabolic")) {
				graphModel = persistSBMLModel(model, fileName, organism, true);
				logger.debug("Creating non-metabolic model");
			}
			else {
				graphModel = persistSBMLModel(model, fileName, organism, false);
				logger.debug("Creating metabolic model");
			}
			if (graphModel != null) {
				return graphModel;
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new GraphModel();
		

	}
	public GraphModel persistSBMLModel(Model model, String fileName, String organism, boolean createQual) { // TODO: Add back in: , SBOLink sboLink) {
		
		boolean modelExists = false;
		
		
		// Time the individual steps
		List<Long> durationList = new ArrayList<Long>();
		Instant start = Instant.now();
		String newModelNameString = model.getName();
		GraphModel existingModel = modelService.getByModelName(newModelNameString);
		if (existingModel != null) {
			modelExists = true;
			logger.info("Model with Name " + newModelNameString + " already exists");
		}
		
		// TODO: Evaluate how we can compare the two models without creating the new one.
		// 		 and then possibly only create the new / changed entities.
		
		// for now create the model whether it already exists or not.
		GraphModel graphModel = new GraphModel(model, fileName, organism, createQual);
				
		if (modelExists) {
			// set id and version of the model-entity to ensure it will be updated and not duplicated
			/*already doing this in the ModelService.
			We need to do this here, to ensure from the beginning that we work on the same model, as it needs to be unique..
			Do we need to reset it then in the modelService (which we do not call at the moment, thats why we had that collision after
			introducing the constraint on modelName)*/
			graphModel.setId(existingModel.getId());
			graphModel.setVersion(existingModel.getVersion());
		}
		Instant end = Instant.now(); // creation of the model
		durationList.add(Duration.between(start, end).toMillis()); //[0]
		logger.debug("Created GraphModel");
		/**
		 * Look at the compartments of the new model
		 * Are they already existing by sbmlIdString?
		 * if so, update them with any new information you might have
		 * otherwise, create it
		 */
		start = Instant.now();
		for (GraphCompartment comp : graphModel.getListCompartment()) {
			GraphCompartment existingCompartment = compartmentService.getBySbmlIdString(comp.getSbmlIdString());
			if (existingCompartment != null) { // TODO: Do more checks to ensure it is the same compartment
				comp.setId(existingCompartment.getId());
				comp.setVersion(existingCompartment.getVersion());
				if(!modelExists)
				{
					for (GraphModel modelConnectedToCompartment : existingCompartment.getModels())
					{
						if(!modelConnectedToCompartment.getModelName().equals(graphModel.getModelName()))
						{
							comp.setModel(modelConnectedToCompartment);
						}
					}
				}
					
			}
			logger.debug("Persisting compartment " + comp.getSbmlIdString());
			compartmentService.saveOrUpdate(comp, 0);
		}
		end = Instant.now(); // compartment
		durationList.add(Duration.between(start, end).toMillis()); //[1]
		logger.debug("Created compartments");
		/**
		 * Look at the species of the new model
		 * Are they already existing by sbmlIdString?
		 * if so, update them with any new information you might have
		 * otherwise, create them
		 */
		start = Instant.now();
		// Species
		for (GraphSpecies species : graphModel.getListSpecies()) {
			/*GraphSpecies existingSpecies = speciesService.getBySbmlIdString(species.getSbmlIdString());
			if (existingSpecies != null) { // TODO: Do more checks to ensure it is the same species
				species.setId(existingSpecies.getId());
				species.setVersion(existingSpecies.getVersion());
				if(!modelExists)
				{
					logger.debug("Checking models of Species: " + existingSpecies.getName() + " with sbmlId " + existingSpecies.getSbmlIdString() + " and used " + species.getSbmlIdString() + " as inital match");
					for (GraphModel modelConnectedToSpecies : existingSpecies.getModels()) // TODO: Check if it has models first (it might not if previous load was interrupted)
					{
						if(!modelConnectedToSpecies.getModelName().equals(graphModel.getModelName()))
						{
							species.setModel(modelConnectedToSpecies);
						}
						
					}
				}
			}*/
			logger.debug("Persisting species " + species.getSbmlIdString() + " ...");
			speciesService.saveOrUpdate(species);
			logger.debug("Persisting species " + species.getSbmlIdString() + " DONE");
			
		}
		end = Instant.now(); // species
		durationList.add(Duration.between(start, end).toMillis()); //[2]
		logger.debug("Created species");
		/**
		 * Look at the QualitativeSpecies of the new model
		 * only if @param createQual is true (that is if we have a non metabolic model which uses these
		 * Is it already existing by sbmlIdString?
		 * if so, update this one with any information you might have
		 * otherwise, create it
		 */
		start = Instant.now();
		
		if(createQual) {
			// QualitativeSpecies
			for (GraphQualitativeSpecies qualSpecies : graphModel.getListQualSpecies()) {
				GraphQualitativeSpecies existingQualSpecies = qualitativeSpeciesService.getBySbmlIdString(qualSpecies.getSbmlIdString());
				if (existingQualSpecies != null) {
					qualSpecies.setId(existingQualSpecies.getId());
					qualSpecies.setVersion(existingQualSpecies.getVersion());
					if(!modelExists)
					{
						for (GraphModel modelConnectedToQualSpecies : existingQualSpecies.getModels())
						{
							if(!modelConnectedToQualSpecies.getModelName().equals(graphModel.getModelName()))
							{
								qualSpecies.setModel(modelConnectedToQualSpecies);
							}
							
						}
					}
				}
				qualitativeSpeciesService.saveOrUpdate(qualSpecies);
			}
		}
		end = Instant.now(); // Qualitative Species
		durationList.add(Duration.between(start, end).toMillis()); //[3]
		logger.debug("Created qualtiative species");
		/**
		 * Look at the Transitions of the new model
		 * only if @param createQual is true (that is if we have a non metabolic model which uses these
		 * Is it already existing by sbmlIdString?
		 * if so, update this one with any information you might have
		 * otherwise, create it
		 */
		start = Instant.now();
		// Transitions
		// a. Translate sbo terms and add them to the attributes (?)
		//Map<String, List<String>> cvTermMap = new HashMap<String, List<String>>();
		if(createQual) {
			for (GraphTransition transition : graphModel.getListTransition()) {
				// setName
				// TODO: Use SBOLink again
				//transition.setName(sboLink.getTerm(transition.getSbmlSBOTerm()).getName()); // TODO: Do not just overwrite this here
				// Next line will not work when using GraphSBase as type in the loop..
				//transition.setSbmlNameString(transition.getSbmlSBOTerm()); // might work for now, TODO CHANGE TO SOMETHING BETTER
				//GraphTransition existingTransition = transitionService.getByMetaid(transition.getMetaid());
				GraphTransition existingTransition = transitionService.getByTransitionIdString(transition.getTransitionIdString());
				if (existingTransition != null) {
					transition.setId(existingTransition.getId());
					transition.setVersion(existingTransition.getVersion());
					//transition.setName(sboLink.getTerm(transition.getSbmlSBOTerm()).getName()); // TODO: Do not just overwrite this here
					if(!modelExists)
					{
						for (GraphModel modelConnectedToTransition : existingTransition.getModels())
						{
							if(modelConnectedToTransition.getModelName().equals(graphModel.getModelName()))
							{
								transition.setModel(modelConnectedToTransition);
							}
						}
					}
				}
				transitionService.saveOrUpdate(transition);
			}
		}
		end = Instant.now(); // Transitions 
		durationList.add(Duration.between(start, end).toMillis()); //[4]
		logger.debug("Created Transitions");
		/**
		 * Look at the QualitativeSpecies of the new model
		 * Is it already existing by sbmlIdString?
		 * if so, update this one with any information you might have
		 * otherwise, create it
		 */
		start = Instant.now();		
		
		// Reactions
		for (GraphReaction reaction : graphModel.getListReaction()) {
			GraphReaction existingReaction = reactionService.getBySbmlIdString(reaction.getSbmlIdString());
			if (existingReaction != null) {
				reaction.setId(existingReaction.getId());
				reaction.setVersion(existingReaction.getVersion());
				if(!modelExists)
				{
					for (GraphModel modelConnectedToReaction : existingReaction.getModels())
					{
						if(!modelConnectedToReaction.getModelName().equals(graphModel.getModelName()))
						{
							reaction.setModel(modelConnectedToReaction);
						}
					}
				}
			}
			reactionService.saveOrUpdate(reaction);
		}
		logger.debug("Created reactions");
		end = Instant.now(); // reactions
		durationList.add(Duration.between(start, end).toMillis()); // [5]
		start = Instant.now();		
		logger.debug(graphModel.toString());
		//graphModel.toString()
		/*// then persist the model
		try {
			modelService.saveOrUpdate(graphModel);
		} catch (OptimisticLockingException e) {
			logger.warn("Exception persisting Model " + graphModel.getModelName());
			e.printStackTrace();
			// TODO: Still need to rollback the db transaction here
			// Figure out which type of Transaction the api uses to know if rollback occours
			// Might think about reloding the db-state and retry persisting the model.
			//return graphModel;
		}
		*/
		
		// lets try to get all the ids and versions and update them in the model.
		// then persist the model
		List<GraphCompartment> listBeforeUpdate = graphModel.getListCompartment();
		if(listBeforeUpdate.size() > 0) {
			logger.debug("Compartment " + listBeforeUpdate.get(0).getSbmlIdString() + " has id " + listBeforeUpdate.get(0).getId() + " and version " + listBeforeUpdate.get(0).getVersion());
		}
		graphModel.setListCompartment(compartmentService.updateCompartmentList(graphModel.getListCompartment()));
		List<GraphCompartment> listAfterUpdate = graphModel.getListCompartment();
			
		if(listAfterUpdate.size() > 0) {
			logger.debug("Compartment " + listAfterUpdate.get(0).getSbmlIdString() + " has id " + listAfterUpdate.get(0).getId() + " and version " + listAfterUpdate.get(0).getVersion());
		}
		
		graphModel.setListSpecies(speciesService.updateSpeciesList(graphModel.getListSpecies()));
		
		graphModel.setListReaction(reactionService.updateReactionList(graphModel.getListReaction()));
		
		if(createQual) {
			graphModel.setListQualSpecies(qualitativeSpeciesService.updateQualitativeSpeciesList(graphModel.getListQualSpecies()));
			
			List<GraphTransition> transitionListBeforeUpdate = graphModel.getListTransition();
			if(transitionListBeforeUpdate.size() > 0) {
				for (GraphTransition transition : transitionListBeforeUpdate) {
					logger.debug("Transition " + transition.getMetaid() + " has id " + transition.getId() + " and version " + transition.getVersion() + " before");
				}
			}
			graphModel.setListTransition(transitionService.updateTransitionList(graphModel.getListTransition()));
			List<GraphTransition> transitionListAfterUpdate = graphModel.getListTransition();
			if(transitionListAfterUpdate.size() > 0) {
				for (GraphTransition transition : transitionListAfterUpdate) {
					logger.debug("Transition " + transition.getMetaid() + " has id " + transition.getId() + " and version " + transition.getVersion() + " after");
				}
			}
		}
		
		/*
		 * This still results in OptimisticLockingException
		 * Check GraphQualitativeSpecies which has reference to species.. might also need new id
		 * Did not change a thing...
		 */
		//modelService.saveOrUpdate(graphModel);
		for (GraphCompartment comp : graphModel.getListCompartment()) {
			GraphCompartment existingCompartment = compartmentService.getBySbmlIdString(comp.getSbmlIdString());
			if (existingCompartment != null) { // TODO: Do more checks to ensure it is the same compartment
				comp.setId(existingCompartment.getId());
				comp.setVersion(existingCompartment.getVersion());
				if(!modelExists)
				{
					for (GraphModel modelConnectedToCompartment : existingCompartment.getModels())
					{
						if(!modelConnectedToCompartment.getModelName().equals(graphModel.getModelName()))
						{
							comp.setModel(modelConnectedToCompartment);
						}
					}
				}
					
			}
			logger.debug("Persisting compartment (2nd run)" + comp.getSbmlIdString());
			compartmentService.saveOrUpdate(comp, 1);
		}
		end = Instant.now(); // graphModel
		durationList.add(Duration.between(start, end).toMillis()); //[6]
	
		// Now output the times
		// Order of durations:
		// Model, compartment, species, reaction, transition qualSpec, persisting
		logger.debug("Model creation took " + durationList.get(0));
		logger.debug("compartment persistence took " + durationList.get(1));
		logger.debug("species persistence took " + durationList.get(2));
		logger.debug("qualSpecies persistence took " + durationList.get(3));
		logger.debug("transition persistence took " + durationList.get(4));
		logger.debug("reaction persistence took " + durationList.get(5));
		logger.debug("GraphModel persistence took " + durationList.get(6));
		
		
		return graphModel;
		
	}
	
	@RequestMapping(value="/updateModels")
	public ResponseEntity<List<ReturnModelOverviewEntry>> updateModels()  {
		
		String contentType = "application/json";
		
		List<ReturnModelOverviewEntry> returnListRmoe = new ArrayList<ReturnModelOverviewEntry>();
		
		List<GraphModel> allModels = modelService.findAll();
		allModels.forEach(model -> { 
			logger.info("Updating Model " + model.getModelName());
			if(model.getListQualSpecies() != null && model.getListQualSpecies().size() > 0) {
				model.setNumNodes(model.getListQualSpecies().size());
				logger.info("Setting Number of Nodes (QualSpecies) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListQualSpecies().size());
			} else if(model.getListSpecies() != null && model.getListSpecies().size() > 0){
				model.setNumNodes(model.getListSpecies().size());
				logger.info("Setting Number of Nodes (Species) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListSpecies().size());
			} else {
				logger.warn("Model " + model.getModelName() + " (id=" + model.getId() + ") has no nodes!");
			}
			
			if(model.getListTransition() != null && model.getListTransition().size() > 0) {
				model.setNumEdges(model.getListTransition().size());
				model.setQualitativeModel(true);
				logger.info("Setting Number of Edges (transition) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListTransition().size());
			} else if(model.getListReaction() != null && model.getListReaction().size() > 0){
				model.setNumEdges(model.getListReaction().size());
				model.setQualitativeModel(false);
				logger.info("Setting Number of Edges (reaction) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListReaction().size());
			} else {
				logger.warn("Model " + model.getModelName() + " (id=" + model.getId() + ") has no Edges!");
			}
			// save
			modelService.saveOrUpdate(model);
			logger.info("Finished updating Model " + model.getModelName());
			ReturnModelOverviewEntry rmoe = new ReturnModelOverviewEntry(modelService.getById(model.getId()));
			rmoe.add(linkTo(methodOn(BasicRestController.class)
					.getModelById(rmoe.getModelId().toString())
				)
				.withSelfRel()
			);
			returnListRmoe.add(rmoe);
		});
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.body(returnListRmoe);
		
	}
	@RequestMapping(value="/testUpdateModels")
	public ResponseEntity<List<ReturnModelOverviewEntry>> testUpdateModels() {
		
		String contentType = "application/json";
		
		List<ReturnModelOverviewEntry> returnListRmoe = new ArrayList<ReturnModelOverviewEntry>();
		logger.info("Updating Models (test only, db not affected)");
		List<GraphModel> allModels = modelService.findAll();
		allModels.forEach(model -> { 
			logger.info("Updating (test only, db not affeced) Model " + model.getModelName());
			if(model.getListQualSpecies() != null && model.getListQualSpecies().size() > 0) {
				//model.setNumNodes(model.getListQualSpecies().size());
				logger.info("Setting Number of Nodes (QualSpecies) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListQualSpecies().size());
			} else if(model.getListSpecies() != null && model.getListSpecies().size() > 0){
				//model.setNumNodes(model.getListSpecies().size());
				logger.info("Setting Number of Nodes (Species) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListSpecies().size());
			} else {
				logger.warn("Model " + model.getModelName() + " (id=" + model.getId() + ") has no nodes!");
			}
			
			if(model.getListTransition() != null && model.getListTransition().size() > 0) {
				//model.setNumEdges(model.getListTransition().size());
				//model.setQualitativeModel(true);
				logger.info("Setting Number of Edges (transition) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListTransition().size());
			} else if(model.getListReaction() != null && model.getListReaction().size() > 0){
				//model.setNumEdges(model.getListReaction().size());
				//model.setQualitativeModel(false);
				logger.info("Setting Number of Edges (reaction) for model " + model.getModelName() + " (id=" + model.getId() + ") to " + model.getListReaction().size());
			} else {
				logger.warn("Model " + model.getModelName() + " (id=" + model.getId() + ") has no Edges!");
			}
			// save
			//modelService.saveOrUpdate(model);
			logger.info("Finished updating (test only, db not affeced) Model " + model.getModelName());
			ReturnModelOverviewEntry rmoe = new ReturnModelOverviewEntry(modelService.getById(model.getId()));
			rmoe.add(linkTo(methodOn(BasicRestController.class)
					.getModelById(rmoe.getModelId().toString())
				)
				.withSelfRel()
			);
			returnListRmoe.add(rmoe);
		});
		
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.body(returnListRmoe);
		
	}
	
	
}
