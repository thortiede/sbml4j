package org.tts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.SBMLSBaseEntity;
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

}
