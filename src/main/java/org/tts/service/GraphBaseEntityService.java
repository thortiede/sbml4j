package org.tts.service;

import org.springframework.core.io.Resource;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.AnnotationName;

public interface GraphBaseEntityService {

	GraphBaseEntity persistEntity(GraphBaseEntity newEntity);

	GraphBaseEntity findByEntityUUID(String entityUuid);

	Iterable<GraphBaseEntity> findAll();

	GraphBaseEntity addAnnotation(GraphBaseEntity entity, String annotationName, String annotationType, Object annotationValue, boolean appendExisting);
	
	String testFlatMappingExtract(String entityUUID);

	Resource testMyDrug(String networkEntityUUID, String myDrugURL);

	boolean deleteEntity(GraphBaseEntity entity);

}
