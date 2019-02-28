package org.tts.service;

import java.util.List;

import org.tts.model.GraphBaseEntity;

public interface GraphBaseEntityService {

	GraphBaseEntity persistEntity(GraphBaseEntity newEntity);

	GraphBaseEntity findByEntityUUID(String entityUuid);

	Iterable<GraphBaseEntity> findAll();

}
