package org.tts.service;

import org.springframework.core.io.Resource;
import org.tts.model.common.GraphBaseEntity;


public interface GraphBaseEntityService {


	GraphBaseEntity findByEntityUUID(String entityUuid);

	Iterable<GraphBaseEntity> findAll();

	GraphBaseEntity addAnnotation(GraphBaseEntity entity, String annotationName, String annotationType, Object annotationValue, boolean appendExisting);
	
	String testFlatMappingExtract(String entityUUID);

	Resource testMyDrug(String networkEntityUUID, String myDrugURL);

	boolean deleteEntity(GraphBaseEntity entity);

	GraphBaseEntity persistEntity(GraphBaseEntity newEntity, int depth);

}
