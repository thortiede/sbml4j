package org.tts.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphCompartment;
import org.tts.model.GraphModel;
import org.tts.model.GraphSBase;
import org.tts.repository.CompartmentRepository;
import org.tts.repository.ModelRepository;
import org.tts.repository.QualitativeSpeciesRepository;
import org.tts.repository.ReactionRepository;
import org.tts.repository.SBaseRepository;
import org.tts.repository.SpeciesRepository;
import org.tts.repository.TransitionRepository;

@Service
public class ModelServiceImpl implements ModelService {

	ModelRepository modelRepository;
	CompartmentRepository compartmentRepository;
	SpeciesRepository speciesRepository;
	QualitativeSpeciesRepository qualitativeSpeciesRepository;
	TransitionRepository transitionRepository;
	ReactionRepository reactionRepository;
	SBaseRepository sBaseRepository;
	
	@Autowired
	public ModelServiceImpl(
			ModelRepository modelRepository,
			CompartmentRepository compartmentRepository,
			SpeciesRepository speciesRepository,
			QualitativeSpeciesRepository qualitativeSpeciesRepository,
			TransitionRepository transitionRepository,
			ReactionRepository reactionRepository,
			SBaseRepository sBaseRepository) 
	{
		super();
		this.modelRepository = modelRepository;
		this.compartmentRepository = compartmentRepository;
		this.speciesRepository = speciesRepository;
		this.qualitativeSpeciesRepository = qualitativeSpeciesRepository;
		this.transitionRepository = transitionRepository;
		this.reactionRepository = reactionRepository;
		this.sBaseRepository = sBaseRepository;
	}

	@Override
	public GraphModel saveOrUpdate(GraphModel newModel) {
		
		GraphModel modelInDb = modelRepository.getByModelName(newModel.getModelName());
		if (modelInDb != null) {
			newModel.setId(modelInDb.getId());
			newModel.setVersion(modelInDb.getVersion());
		}
		for(GraphSBase entity : newModel.getListCompartment())
		{
			matchEntitiesToDb(entity);
		}
		for(GraphSBase entity : newModel.getListSpecies())
		{
			matchEntitiesToDb(entity);
		}
		for(GraphSBase entity : newModel.getListQualSpecies())
		{
			matchEntitiesToDb(entity);
		}
		for(GraphSBase entity : newModel.getListTransition())
		{
			matchEntitiesToDb(entity);
		}
		for(GraphSBase entity : newModel.getListReaction())
		{
			matchEntitiesToDb(entity);
		}
		
		return modelRepository.save(newModel, 10);
	}
	
	private void matchEntitiesToDb(GraphSBase entity) {
		GraphSBase entityInDb = sBaseRepository.getBySbmlIdString(entity.getSbmlIdString());
		if (entityInDb != null) 
		{
			entity.setId(entityInDb.getId());
			entity.setVersion(entityInDb.getVersion());
		}
	}
	

}
