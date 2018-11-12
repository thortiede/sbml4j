package org.tts.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.GraphReaction;
import org.tts.model.GraphSpecies;
import org.tts.repository.ReactionRepository;
import org.tts.repository.SpeciesRepository;

@Service
public class ReactionServiceImpl implements ReactionService {

	ReactionRepository reactionRepository;
	SpeciesRepository speciesRepository;
	
	@Autowired
	public ReactionServiceImpl(ReactionRepository reactionRepository, SpeciesRepository speciesRepository) {
		this.reactionRepository = reactionRepository;
		this.speciesRepository = speciesRepository;
	}

	@Transactional
	public GraphReaction saveOrUpdate(GraphReaction newReaction) {
		return reactionRepository.save(newReaction, 1); // TODO: Which depth here? Does this cause optimistic locking execption?
	}

	@Override
	public GraphReaction getBySbmlIdString(String sbmlIdString) {
		return reactionRepository.getBySbmlIdString(sbmlIdString);
	}

	@Override
	public GraphReaction getBySbmlNameString(String sbmlNameString) {
		return reactionRepository.getBySbmlNameString(sbmlNameString);
	}

	@Override
	public List<GraphReaction> updateReactionList(List<GraphReaction> listReaction) {
		List<GraphReaction> newReactionList = new ArrayList<GraphReaction>();
		for (GraphReaction newReaction : listReaction) {
			GraphReaction existingReaction = getBySbmlIdString(newReaction.getSbmlIdString());
			if(existingReaction != null) {
				newReaction.setId(existingReaction.getId());
				newReaction.setVersion(existingReaction.getVersion());
				List<GraphSpecies> newReactantList = new ArrayList<GraphSpecies>();
				List<GraphSpecies> newProductList = new ArrayList<GraphSpecies>();
				for(GraphSpecies newReactant : newReaction.getReactants()) {
					for(GraphSpecies existingReactant : existingReaction.getReactants()) {
						if (newReactant.getSbmlIdString().equals(existingReactant.getSbmlIdString())) {
							newReactant.setId(existingReactant.getId());
							newReactant.setVersion(existingReactant.getVersion());
						}
					}
					newReactantList.add(newReactant);
				}
				newReaction.setReactants(newReactantList);
				for(GraphSpecies newProduct : newReaction.getProducts()) {
					for(GraphSpecies existingProduct : existingReaction.getProducts()) {
						if (newProduct.getSbmlIdString().equals(existingProduct.getSbmlIdString())) {
							newProduct.setId(existingProduct.getId());
							newProduct.setVersion(existingProduct.getVersion());
						}
					}
					newProductList.add(newProduct);
				}
				newReaction.setProducts(newProductList);
			} else {
				// reaction does not exist, but the references species might exist nonetheless...
				List<GraphSpecies> newReactantList = new ArrayList<GraphSpecies>();
				List<GraphSpecies> newProductList = new ArrayList<GraphSpecies>();
				for(GraphSpecies newReactant : newReaction.getReactants()) {
					GraphSpecies existingSpecies = speciesRepository.getBySbmlIdString(newReactant.getSbmlIdString());
					if(existingSpecies != null) {
						newReactant.setId(existingSpecies.getId());
						newReactant.setVersion(existingSpecies.getVersion());
					}
					newReactantList.add(newReactant);
				}
				newReaction.setReactants(newReactantList);
				for(GraphSpecies newProduct : newReaction.getProducts()) {
					GraphSpecies existingSpecies = speciesRepository.getBySbmlIdString(newProduct.getSbmlIdString());
					if(existingSpecies != null) {
						newProduct.setId(existingSpecies.getId());
						newProduct.setVersion(existingSpecies.getVersion());
					}
					newProductList.add(newProduct);
				}
				newReaction.setProducts(newProductList);
			}
			newReactionList.add(newReaction);
		}
		return newReactionList;
	}

}
