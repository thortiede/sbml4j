package org.tts.service;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphModel;
import org.tts.model.GraphTransition;
import org.tts.model.NodeEdgeList;
import org.tts.repository.ModelRepository;
import org.tts.repository.TransitionRepository;
import uk.ac.ebi.sbo.ws.client.SBOLink;


@Service
public class NodeEdgeListServiceImpl implements NodeEdgeListService {

	private ModelRepository modelRepository;
	private TransitionRepository transitionRepository;
	private SBOLink sbolink;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public NodeEdgeListServiceImpl(ModelRepository modelRepository, TransitionRepository transistionRepository) {
		this.modelRepository = modelRepository;
		this.transitionRepository = transistionRepository;
		this.sbolink = new SBOLink();
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
											transition.getQualSpeciesTwoSbmlNameString(),
											translateSBOTerm(transition.getSbmlSBOTerm()));
			}
		}		
		
		return nodeEdgeList;
	}


	private String translateSBOTerm(String sbmlSBOTerm) {
		if (sbmlSBOTerm.equals("")) return "not determined, no SBO Link";
		// Do translation using SBOLink here
		else return sbolink.getTerm(sbmlSBOTerm).getName();
	}

}
