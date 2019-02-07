package org.tts.service;

import org.springframework.stereotype.Service;
import org.tts.model.GraphBaseEntity;
import org.tts.repository.GraphBaseEntityRepository;

@Service
public class GraphBaseEntityServiceImpl implements GraphBaseEntityService {

	GraphBaseEntityRepository graphBaseEntityRepository;
	
	public GraphBaseEntityServiceImpl(GraphBaseEntityRepository graphBaseEntityRepository) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
	}

	@Override
	public GraphBaseEntity persistEntity(GraphBaseEntity newEntity) {
		return this.graphBaseEntityRepository.save(newEntity);
	}

}
