package org.tts.service;


import java.util.ArrayList;
import java.util.List;

import org.sbml.jsbml.SBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphReaction;
import org.tts.model.GraphTransition;
import org.tts.model.NodeEdgeList;
import org.tts.repository.ModelRepository;
import org.tts.repository.ReactionRepository;
import org.tts.repository.TransitionRepository;



@Service
public class NodeEdgeListServiceImpl implements NodeEdgeListService {

	private ModelRepository modelRepository;
	private TransitionRepository transitionRepository;
	private ReactionRepository reactionRepository;
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public NodeEdgeListServiceImpl(ModelRepository modelRepository, TransitionRepository transistionRepository, ReactionRepository reactionRepository) {
		this.modelRepository = modelRepository;
		this.transitionRepository = transistionRepository;
		this.reactionRepository = reactionRepository;
	}
	
	
	@Override
	public NodeEdgeList getFullNet() {
		List<GraphTransition> graphTransitionList = new ArrayList<GraphTransition>();
		NodeEdgeList nodeEdgeList = new NodeEdgeList();
		
		transitionRepository.findAll(0).forEach(graphTransitionList::add);
		logger.debug("Found all transitions");
		for (GraphTransition transition : graphTransitionList) {
			logger.debug("Processing transition: " + transition.getMetaid());
			boolean allThere = true;
			
			
			if(transition.getSbmlSBOTerm() == null) {
				logger.debug("SBOTerm is null");
				allThere = false;
			} else {
				//logger.debug("Transition Name: " + transition.getSbmlNameString());
				// do a reload to get the qualSpecies in the transistion
				//transition = transistionRepository.getBySbmlIdString(transition.getSbmlNameString());
			}
			if(transition.getQualSpeciesOneSbmlNameString() == null) {
				
				logger.debug("Qual 1 Name is null");
				allThere = false;
			}
			if(transition.getQualSpeciesTwoSbmlNameString() == null) {
				logger.debug("Qual 2 Name is null");
				allThere = false;
			}
			
			if(allThere) {
				/*logger.debug("Adding Entry: " + transition.getQualSpeciesOneSbmlNameString() + ", " +  
											transition.getQualSpeciesTwoSbmlNameString()+ ", " + 
											transition.getSbmlNameString() );*/
				
				nodeEdgeList.addListEntry(	transition.getQualSpeciesOneSbmlNameString(),
											"entitiyUUIDNotDefined",
											transition.getQualSpeciesTwoSbmlNameString(),
											"entitiyUUIDNotDefined",
											translateSBOTerm(transition.getSbmlSBOTerm()));
			}
		}		
		
		return nodeEdgeList;
	}


	private String translateSBOTerm(String sbmlSBOTerm) {
		if (sbmlSBOTerm.equals("")) return "not determined, no SBO Link";
		// Do translation using SBOLink here
		else return SBO.getTerm(sbmlSBOTerm).getName();
	}


	@Override
	public NodeEdgeList getMetabolicNet() {
		List<GraphReaction> reactionList = new ArrayList<>();
		NodeEdgeList nodeEdgeList = new NodeEdgeList();
		reactionRepository.findAll().forEach(reactionList::add);
		
		for (GraphReaction reaction : reactionList) {
			reaction.getSbmlNameString();
		}
		

		return null;
	}

}
