package org.tts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.BiomodelsQualifier;
import org.tts.model.GraphBaseEntity;
import org.tts.model.SBMLQualModelExtension;
import org.tts.model.SBMLQualSpecies;
import org.tts.model.SBMLQualTransition;
import org.tts.model.SBMLSBaseEntity;
import org.tts.model.SBMLSBaseExtension;
import org.tts.repository.GraphBaseEntityRepository;

@Service
public class SBMLPersistenceServiceImpl implements SBMLPersistenceService {

	GraphBaseEntityRepository graphBaseRepository;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public SBMLPersistenceServiceImpl(GraphBaseEntityRepository graphBaseRepository) {
		super();
		this.graphBaseRepository = graphBaseRepository;
	}

	@Override
	public Iterable<SBMLSBaseEntity> saveAll(Map<String, Iterable<SBMLSBaseEntity>> entityMap) {
		List<SBMLSBaseEntity> returnIterable = new ArrayList<>();
		for (String key : entityMap.keySet()) {
			Iterable<SBMLSBaseEntity> currentList = entityMap.get(key);
			for (SBMLSBaseEntity currentEntity : currentList) {
				returnIterable.add(graphBaseRepository.save(currentEntity));
			}
		}
		return returnIterable;
	}
/*
	@Override
	public Map<String, Iterable<SBMLSBaseEntity>> findExisting(Map<String, Iterable<SBMLSBaseEntity>> entityMap) {
		for (String key : entityMap.keySet()) {
			switch (key) {
			case "SBMLSBaseExtension":
				for (SBMLSBaseEntity extension : entityMap.get(key)) {
					SBMLSBaseExtension sBaseExtension = (SBMLSBaseExtension) extension;
					switch (sBaseExtension.getShortLabel()) {
					case "qual":
						SBMLQualModelExtension sbmlQualModelExtension = (SBMLQualModelExtension) sBaseExtension;
						List<SBMLQualTransition> newTransitions = sbmlQualModelExtension.getSbmlQualTransitions();
						
						break;

					default:
						break;
					}
				}
				break;

			default:
				break;
			}
		}
		
	}*/

	@Override
	public GraphBaseEntity save(GraphBaseEntity o) {
		
		GraphBaseEntity existingEntity = graphBaseRepository.findByEntityUUID(o.getEntityUUID());
		if (existingEntity != null && existingEntity.getId().equals(o.getId())) {
			o.setVersion(existingEntity.getVersion());
		}
		
		return graphBaseRepository.save(o);
	}

	@Override
	public boolean checkIfExists(GraphBaseEntity entity) {
		try {
			if ((SBMLQualSpecies) entity instanceof SBMLQualSpecies) {
				// a qual species has annotations to external resources from which we can 
				// decide whether this is the entity we are looking for
				//for (BiomodelsQualifier qualifier : ((SBMLQualSpecies) entity).getExternalResources()
				logger.info("Entity is of type qualspecies");
				// this is not reached as probably qualSpecies are persisted not directly
				// but through the modelExtension -> Transition -> Input/Output
				// TODO: Find a way to match new entities with existing ones
			}
		} catch (ClassCastException e) {
			logger.info("Not qualSpecies");
		}
		return false; //graphBaseRepository.existsByEntityUUID(entity.getEntityUUID());
	}

	@Override
	public GraphBaseEntity getByEntityUUID(String entityUUID) {
		return graphBaseRepository.findByEntityUUID(entityUUID);
	}
}
