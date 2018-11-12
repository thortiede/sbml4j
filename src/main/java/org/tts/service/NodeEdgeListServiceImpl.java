package org.tts.service;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphModel;
import org.tts.model.GraphReaction;
import org.tts.model.GraphTransition;
import org.tts.model.NodeEdgeList;
import org.tts.repository.ModelRepository;
import org.tts.repository.TransitionRepository;

@Service
public class NodeEdgeListServiceImpl implements NodeEdgeListService {

	private ModelRepository modelRepository;
	private TransitionRepository transistionRepository;
	
	
	@Autowired
	public NodeEdgeListServiceImpl(ModelRepository modelRepository, TransitionRepository transistionRepository) {
		this.modelRepository = modelRepository;
		this.transistionRepository = transistionRepository;
	}
	
	
	@Override
	public NodeEdgeList getFullNet() {
		List<GraphModel> graphModelList = new ArrayList<GraphModel>();
		NodeEdgeList nodeEdgeList = new NodeEdgeList();
		
		// 1. Get all GraphModels from the database/repository
	
		modelRepository.findAll().forEach(graphModelList::add);
		
		// 2. go through and extract all nodes and edges for transitions
		for (GraphModel model : graphModelList) {
			// 3. assemble and the entries
			if (model.getListTransition() != null && model.getListTransition().size() > 0) {
				for(GraphTransition transition : model.getListTransition()) {
					boolean allThere = true;
					
					
					if(transition.getSbmlNameString() == null) {
						System.out.println("transName is null");
						allThere = false;
					} else {
						//System.out.println("Transition Name: " + transition.getSbmlNameString());
						// do a reload to get the qualSpecies in the transistion
						//transition = transistionRepository.getBySbmlIdString(transition.getSbmlNameString());
					}
					if(transition.getQualSpeciesOneSbmlNameString() == null) {
						
						System.out.println("Qual 1 Name is null");
						allThere = false;
					}
					if(transition.getQualSpeciesTwoSbmlNameString() == null) {
						System.out.println("Qual 2 Name is null");
						allThere = false;
					}
					
					if(allThere) {
						/*System.out.println("Adding Entry: " + transition.getQualSpeciesOneSbmlNameString() + ", " +  
													transition.getQualSpeciesTwoSbmlNameString()+ ", " + 
													transition.getSbmlNameString() );*/
						
						nodeEdgeList.addListEntry(	transition.getQualSpeciesOneSbmlNameString(), 
													transition.getQualSpeciesTwoSbmlNameString(),
													transition.getSbmlSBOTerm());
					}
				}
			}
		}
	
		// 4. check for duplicates
		// TODO
		
		// 5. check for conflicts
		// TODO
		
		return nodeEdgeList;
	}

}
