package org.tts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphBaseEntity;
import org.tts.model.SBMLQualModelExtension;
import org.tts.model.SBMLQualTransition;
import org.tts.model.SBMLSBaseEntity;
import org.tts.model.SBMLSBaseExtension;
import org.tts.repository.GraphBaseEntityRepository;

@Service
public class SBMLPersistenceServiceImpl implements SBMLPersistenceService {

	GraphBaseEntityRepository graphBaseRepository;
	
	
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
		return graphBaseRepository.save(o);
	}
}
