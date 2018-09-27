package org.tts.controller;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
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

@RestController
public class PersistSBMLRestController {

	private final ModelService modelService;
	private final SpeciesService speciesService;
	private final QualitativeSpeciesService qualitativeSpeciesService;
	private final CompartmentService compartmentService;
	private final ReactionService reactionService;
	private final TransitionService transitionService;
	
	
	@Autowired
	public PersistSBMLRestController(ModelService modelService, SpeciesService speciesService, CompartmentService compartmentService, ReactionService reactionService, QualitativeSpeciesService qualitativeSpeciesService, TransitionService transitionService) {
		this.modelService = modelService;
		this.speciesService = speciesService;
		this.qualitativeSpeciesService = qualitativeSpeciesService;
		this.compartmentService = compartmentService;
		this.reactionService = reactionService;
		this.transitionService = transitionService;

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
			graphModel = persistSBMLModel(model);
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
	public GraphModel persistSBMLModel(Model model) { // TODO: Add back in: , SBOLink sboLink) {
		GraphModel graphModel = new GraphModel(model);
		
		// take the model and check if elements from it already exist.
		// If so, update them (yes or no?) and use them in the model (set the id and version of that entity before persisting)
		// Compartments
		for (GraphCompartment comp : graphModel.getListCompartment()) {
			GraphCompartment existingCompartment = compartmentService.getBySbmlIdString(comp.getSbmlIdString());
			if (existingCompartment != null) { // TODO: Do more checks to ensure it is the same compartment
				comp.setId(existingCompartment.getId());
				comp.setVersion(existingCompartment.getVersion());
			}
			//compartmentService.saveOrUpdate(comp);
		}
		
		// Species
		for (GraphSpecies species : graphModel.getListSpecies()) {
			GraphSpecies existingSpecies = speciesService.getBySbmlIdString(species.getSbmlIdString());
			if (existingSpecies != null) { // TODO: Do more checks to ensure it is the same compartment
				species.setId(existingSpecies.getId());
				species.setVersion(existingSpecies.getVersion());
			}
		}
		
		// Reactions
		for (GraphReaction reaction : graphModel.getListReaction()) {
			GraphReaction existingReaction = reactionService.getBySbmlIdString(reaction.getSbmlIdString());
			if (existingReaction != null) {
				reaction.setId(existingReaction.getId());
				reaction.setVersion(existingReaction.getVersion());
			}
		}
		
		// Transitions
		// a. Translate sbo terms and add them to the attributes (?)
		//Map<String, List<String>> cvTermMap = new HashMap<String, List<String>>();
		for (GraphTransition transition : graphModel.getListTransition()) {
			// setName
			// TODO: Use SBOLink again
			//transition.setName(sboLink.getTerm(transition.getSbmlSBOTerm()).getName()); // TODO: Do not just overwrite this here
			transition.setSbmlNameString(transition.getSbmlSBOTerm()); // might work for now, TODO CHANGE TO SOMETHING BETTER
			/*GraphTransition existingTransition = transitionService.getBySbmlIdString(transition.getSbmlIdString());
			if (existingTransition != null) {
				transition.setId(existingTransition.getId());
				transition.setVersion(existingTransition.getVersion());
				transition.setName(sboLink.getTerm(transition.getSbmlSBOTerm()).getName()); // TODO: Do not just overwrite this here
			}*/
		}
		
		
		
		// QualitativeSpecies
		for (GraphQualitativeSpecies qualSpecies : graphModel.getListQualSpecies()) {
			GraphQualitativeSpecies existingQualSpecies = qualitativeSpeciesService.getBySbmlIdString(qualSpecies.getSbmlIdString());
			if (existingQualSpecies != null) {
				qualSpecies.setId(existingQualSpecies.getId());
				qualSpecies.setVersion(existingQualSpecies.getVersion());
			}
		}
		
		
		
		// then persist the model
		try {
			modelService.saveOrUpdate(graphModel);
		} catch (Exception e) {
			return graphModel;
		}
		return graphModel;
		
	}

}
