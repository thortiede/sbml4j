package org.tts.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.flat.FlatEdge;

public interface GraphBaseEntityService {

	GraphBaseEntity persistEntity(GraphBaseEntity newEntity);

	GraphBaseEntity findByEntityUUID(String entityUuid);

	Iterable<GraphBaseEntity> findAll();

	String testFlatMappingExtract(String entityUUID);

	Resource testContextMethod(String networkEntityUUID, String geneSymbol);

	Resource testGetNet(String networkEntityUUID);

	Resource testMultiGeneSubNet(String networkEntityUUID, List<String> geneList, FilterOptions options);

	Resource testGetNet(String networkEntityUUID, List<String> genes);

	Resource testMyDrug(String networkEntityUUID, String myDrugURL);

}
