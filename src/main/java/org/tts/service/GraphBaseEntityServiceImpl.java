package org.tts.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.tts.model.common.GraphBaseEntity;
import org.tts.repository.common.GraphBaseEntityRepository;

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

	@Override
	public GraphBaseEntity findByEntityUUID(String entityUuid) {
		return graphBaseEntityRepository.findByEntityUUID(entityUuid);
	}

	@Override
	public Iterable<GraphBaseEntity> findAll() {
		// TODO Auto-generated method stub
		return graphBaseEntityRepository.findAll();
	}

}
