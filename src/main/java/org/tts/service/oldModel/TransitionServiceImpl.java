package org.tts.service.oldModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.old.GraphQualitativeSpecies;
import org.tts.model.old.GraphReaction;
import org.tts.model.old.GraphTransition;
import org.tts.repository.oldModel.QualitativeSpeciesRepository;
import org.tts.repository.oldModel.TransitionRepository;

@Service
public class TransitionServiceImpl implements TransitionService {

	TransitionRepository transitionRepository;
	QualitativeSpeciesRepository qualitativeSpeciesRepository;
	
	@Autowired
	public TransitionServiceImpl(TransitionRepository transitionRepository, QualitativeSpeciesRepository qualitativeSpeciesRepository) {
		this.transitionRepository = transitionRepository;
		this.qualitativeSpeciesRepository = qualitativeSpeciesRepository;
	}

	@Transactional
	public GraphTransition saveOrUpdate(GraphTransition newTransition) {
		return transitionRepository.save(newTransition, 1); // TODO: Which depth here? Does this cause optimistic locking execption?
	}

	@Override
	public GraphTransition getByMetaid(String metaid) {
		return transitionRepository.getByMetaid(metaid);
	}

	@Override
	public List<GraphTransition> updateTransitionList(List<GraphTransition> listTransition) {
		List<GraphTransition> newTransitionList = new ArrayList<GraphTransition>();
		for (GraphTransition newTransition : listTransition) {
			GraphTransition existingTransition = getByMetaid(newTransition.getMetaid());
			if(existingTransition != null) {
				newTransition.setId(existingTransition.getId());
				newTransition.setVersion(existingTransition.getVersion());	
			}
			GraphQualitativeSpecies newQualSpeciesOne = newTransition.getQualSpeciesOne();
			GraphQualitativeSpecies existingQualSpecies = qualitativeSpeciesRepository.getBySbmlIdString(newQualSpeciesOne.getSbmlIdString());
			if(existingQualSpecies != null) {
				newQualSpeciesOne.setId(existingQualSpecies.getId());
				newQualSpeciesOne.setVersion(existingQualSpecies.getVersion());
			}
			newTransition.setQualSpeciesOne(newQualSpeciesOne);
			
			GraphQualitativeSpecies newQualSpeciesTwo = newTransition.getQualSpeciesTwo();
			existingQualSpecies = qualitativeSpeciesRepository.getBySbmlIdString(newQualSpeciesTwo.getSbmlIdString());
			if(existingQualSpecies != null) {
				newQualSpeciesTwo.setId(existingQualSpecies.getId());
				newQualSpeciesTwo.setVersion(existingQualSpecies.getVersion());
			}
			newTransition.setQualSpeciesTwo(newQualSpeciesTwo);
			
			newTransitionList.add(newTransition);
		}
		return newTransitionList;
	}

	@Override
	public List<GraphTransition> findAll() {
		List<GraphTransition> allTransitions = new ArrayList<GraphTransition>();
		transitionRepository.findAll().forEach(allTransitions::add);
		return allTransitions;
	}

	@Override
	public GraphTransition getByTransitionIdString(String transitionIdString) {
		return transitionRepository.getByTransitionIdString(transitionIdString);
	}

}
