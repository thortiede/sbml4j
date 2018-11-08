package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	public int persistSBMLFolder(@RequestParam(value="name") String name) {
		boolean createQual = false;
		logger.debug("reading dir ");
		String directory = name;
		System.out.println(directory);
		if(directory.contains("non-metabolic")) {
			createQual = false;
		} else {
			createQual = true;
		}

		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();
		int cnt = 0;
		// TODO: This should be outside the for loop, or not?
		// I moved them here, lets what happens #reminder
		SBMLReader reader = new SBMLReader();
		SBMLDocument doc = null;
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	logger.info("Processing File: " + file.getAbsolutePath());	    	
		 
		    	try {
		    		
		    		
		    		
		    		//String localDir = "/Users/ttiede/Documents/workspace_aug2018/testdata/sbml4j/sbml/non-metabolic/hsa/";
		    		//String filepath = directory + name;
		    		//System.out.println(filepath);
		    		
	    			GraphModel graphModel;
	    			doc = reader.readSBML(file.getAbsolutePath());
	    			Model model = doc.getModel();
	    			graphModel = persistSBMLModel(model, createQual);
	    			if (graphModel != null) {
	    				cnt = cnt + 1;
	    			}
		    	} catch (XMLStreamException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (IOException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} finally {
					logger.info("finished persisting model " + doc.getModel().getName());
				}
	    		
		    	
		    }
		}
		return cnt;
	}
	
	@RequestMapping(value = "/sbmlfile", method = RequestMethod.GET)
	public GraphModel persistSBML(@RequestParam(value="name") String name) {
		SBMLReader reader = new SBMLReader();
		SBMLDocument doc;
		
		String localDir = "/Users/ttiede/Documents/workspace_aug2018/testdata/sbml4j/sbml/non-metabolic/hsa/";
		String filepath = localDir + name;
		System.out.println(filepath);
		try {
			GraphModel graphModel;
			doc = reader.readSBML(filepath);
			Model model = doc.getModel();
			graphModel = persistSBMLModel(model, true);
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
	public GraphModel persistSBMLModel(Model model, boolean createQual) { // TODO: Add back in: , SBOLink sboLink) {
		
		// Need somthing to hold the Timings each run
		List<Long> durationList = new ArrayList<Long>();
		
		/**
		 * The Model itself as new model.
		 * This creates all the java instances for the entities in the model
		 * 
		 */
		
		Instant start = Instant.now();
		GraphModel graphModel = new GraphModel(model, createQual);
		GraphModel existingModel = modelService.getByModelName(graphModel.getModelName());
		boolean modelExists = false;
		if (existingModel != null) {
			modelExists = true;
			System.out.println("Model exists");
			// already doing this in the ModelService.
			// TODO: Which Location makes more sense? #reminder
			//graphModel.setId(existingModel.getId());
			//graphModel.setVersion(existingModel.getVersion());
		}
		Instant end = Instant.now();
		durationList.add(Duration.between(start, end).toMillis());
		
		/**
		 * Look at the compartment of the new model
		 * Is it already existing by sbmlIdString?
		 * if so, update this one with any information you might have
		 * otherwise, create it
		 */
		start = Instant.now();
		// take the model and check if elements from it already exist.
		// If so, update them (yes or no?) and use them in the model (set the id and version of that entity before persisting)
		// Compartments
		for (GraphCompartment comp : graphModel.getListCompartment()) {
			GraphCompartment existingCompartment = compartmentService.getBySbmlIdString(comp.getSbmlIdString());
			if (existingCompartment != null) { // TODO: Do more checks to ensure it is the same compartment
				comp.setId(existingCompartment.getId());
				comp.setVersion(existingCompartment.getVersion());
				if(!modelExists)
				{
					for (GraphModel modelConnectedToCompartment : existingCompartment.getModels())
					{
						comp.setModel(modelConnectedToCompartment);
					}
				}
					
			}
			//compartmentService.saveOrUpdate(comp);
		}
		 end = Instant.now();
		durationList.add(Duration.between(start, end).toMillis());
		
		/**
		 * Look at the species of the new model
		 * Is it already existing by sbmlIdString?
		 * if so, update this one with any information you might have
		 * otherwise, create it
		 */
		start = Instant.now();
		// Species
		for (GraphSpecies species : graphModel.getListSpecies()) {
			GraphSpecies existingSpecies = speciesService.getBySbmlIdString(species.getSbmlIdString());
			if (existingSpecies != null) { // TODO: Do more checks to ensure it is the same species
				species.setId(existingSpecies.getId());
				species.setVersion(existingSpecies.getVersion());
				if(!modelExists)
				{
					logger.debug("Checking models of Species: " + existingSpecies.getName() + " with sbmlId " + existingSpecies.getSbmlIdString() + " and used " + species.getSbmlIdString() + " as inital match");
					for (GraphModel modelConnectedToSpecies : existingSpecies.getModels()) // TODO: Check if it has models first (it might not if previous load was interrupted)
					{
						species.setModel(modelConnectedToSpecies);
						
					}
				}
			}
		}
		end = Instant.now();
		durationList.add(Duration.between(start, end).toMillis());
		
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
							qualSpecies.setModel(modelConnectedToQualSpecies);
							
						}
					}
				}
			}
		}
		end = Instant.now();
		durationList.add(Duration.between(start, end).toMillis());
		
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
				GraphTransition existingTransition = transitionService.getBySbmlIdString(transition.getSbmlIdString());
				if (existingTransition != null) {
					transition.setId(existingTransition.getId());
					transition.setVersion(existingTransition.getVersion());
					//transition.setName(sboLink.getTerm(transition.getSbmlSBOTerm()).getName()); // TODO: Do not just overwrite this here
					if(!modelExists)
					{
						for (GraphModel modelConnectedToTransition : existingTransition.getModels())
						{
							transition.setModel(modelConnectedToTransition);
							
						}
					}
				}	
			}
		}
		end = Instant.now();
		durationList.add(Duration.between(start, end).toMillis());
		
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
						reaction.setModel(modelConnectedToReaction);
						
					}
				}
			}
		}
	
		end = Instant.now();
		
		durationList.add(Duration.between(start, end).toMillis());
		start = Instant.now();		
		logger.debug(graphModel.toString());
		//graphModel.toString()
		// then persist the model
		try {
			for (GraphCompartment compartment : graphModel.getListCompartment()) {
				GraphCompartment tmp = compartmentService.saveOrUpdate(compartment);
				if (tmp.getVersion() != compartment.getVersion()) {
					logger.warn("Compartment " + compartment.getSbmlNameString() + ", " + compartment.getId() + " has different Version");
				}
			}
			/*for (GraphSpecies species : graphModel.getListSpecies()) {
				GraphSpecies tmp = speciesService.saveOrUpdate(species);
				if (tmp.getVersion() != species.getVersion()) {
					logger.warn("species " + species.getSbmlNameString() + ", " + species.getId() + " has different Version");
				}
			}*/
			for (GraphReaction reaction : graphModel.getListReaction()) {
				GraphReaction tmp = reactionService.saveOrUpdate(reaction);
				if (tmp.getVersion() != reaction.getVersion()) {
					logger.warn("reaction " + reaction.getSbmlNameString() + ", " + reaction.getId() + " has different Version");
				}
			}
			if(createQual) {
			/*	for (GraphQualitativeSpecies qualitativeSpecies : graphModel.getListQualSpecies()) { // TODO: Method should be: getListQualitativeSpecies for naming standards
					GraphQualitativeSpecies tmp = qualitativeSpeciesService.saveOrUpdate(qualitativeSpecies);
					if (tmp.getVersion() != qualitativeSpecies.getVersion()) {
						logger.warn("qualitativeSpecies " + qualitativeSpecies.getSbmlNameString() + ", " + qualitativeSpecies.getId() + " has different Version");
					}
				}*/
				for (GraphTransition transition : graphModel.getListTransition()) {
					GraphTransition tmp = transitionService.saveOrUpdate(transition);
					if (tmp.getVersion() != transition.getVersion()) {
						logger.warn("transition " + transition.getSbmlNameString() + ", " + transition.getId() + " has different Version");
					}
				}
			}
			
			//modelService.saveOrUpdate(graphModel);
		} catch (OptimisticLockingException e) {
			logger.warn("Exception persisting Model " + graphModel.getModelName());
			e.printStackTrace();
			// TODO: Still need to rollback the db transaction here
			// Figure out which type of Transaction the api uses to know if rollback occours
			// Might think about reloding the db-state and retry persisting the model.
			//return graphModel;
		}
		
		end = Instant.now();
		
		durationList.add(Duration.between(start, end).toMillis());
	
		// Now output the times
		// Order of durations:
		// Model, compartment, species, reaction, transition qualSpec, persisting
		logger.debug("Model took " + durationList.get(0));
		logger.debug("compartment took " + durationList.get(1));
		logger.debug("species took " + durationList.get(2));
		logger.debug("reaction took " + durationList.get(3));
		logger.debug("transition took " + durationList.get(4));
		logger.debug("qualSpec took " + durationList.get(5));
		logger.info("Persisting took " + durationList.get(6));
		
		
		return graphModel;
		
	}

}
