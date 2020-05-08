package org.tts.service;

import org.springframework.core.io.Resource;
import org.tts.model.common.GraphBaseEntity;

public interface GraphBaseEntityService {

	GraphBaseEntity persistEntity(GraphBaseEntity newEntity);

	GraphBaseEntity findByEntityUUID(String entityUuid);

	Iterable<GraphBaseEntity> findAll();

	String testFlatMappingExtract(String entityUUID);

	Resource testMyDrug(String networkEntityUUID, String myDrugURL);

}
