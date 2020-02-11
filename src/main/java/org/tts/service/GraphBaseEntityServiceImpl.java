package org.tts.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.api.Output.FlatMappingReturnType;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.flat.FlatEdge;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.flat.FlatEdgeRepository;
import org.tts.repository.flat.FlatNetworkMappingRepository;
import org.neo4j.ogm.session.Session;

@Service
public class GraphBaseEntityServiceImpl implements GraphBaseEntityService {

	@Autowired
	GraphBaseEntityRepository graphBaseEntityRepository;
	FlatNetworkMappingRepository flatNetworkMappingRepository;
	FlatEdgeRepository flatEdgeRepository;
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	ProvenanceGraphService provenanceGraphService;
	Session session;
	
	public GraphBaseEntityServiceImpl(GraphBaseEntityRepository graphBaseEntityRepository,
			FlatNetworkMappingRepository flatNetworkMappingRepository,
			FlatEdgeRepository flatEdgeRepository,
			SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl,
			ProvenanceGraphService provenanceGraphService,
			Session session
			) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.flatNetworkMappingRepository = flatNetworkMappingRepository;
		this.flatEdgeRepository = flatEdgeRepository;
		this.sbmlSimpleModelUtilityServiceImpl = sbmlSimpleModelUtilityServiceImpl;
		this.provenanceGraphService = provenanceGraphService;
		this.session = session;
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

	public Map<String, String> cloneAndPersistNetworkAndReturnOldToNewMap(String entityUUID) {
		Iterable<FlatMappingReturnType> ret = this.flatNetworkMappingRepository.getNodesAndRelationshipsForMapping(entityUUID);		
		FlatEdge currentEdge;
		List<FlatEdge> newEdges = new ArrayList<>();
		Map<String, String> newUUIDToOldUUIDMap = new HashMap<>();
		
		/**
		 * Traverse the result set and alter the content to generate new entities
		 * thus copying them without actually needing to copy them
		 * save the connection through the new and old entityUUID to connect the entities with provenance edges
		 */
		Iterator<FlatMappingReturnType> it = ret.iterator();
		FlatMappingReturnType current;
		while (it.hasNext()) {
		
			current = it.next();
			currentEdge = current.getRelationship();
			// 1. safe the old edge uuid
			//String oldEdgeUUID = currentEdge.getEntityUUID();
			// reset the edge graph base properties (new entityUUID, id to null and version to null)
			this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge);			
			if(!newUUIDToOldUUIDMap.containsKey(currentEdge.getInputFlatSpecies().getEntityUUID())) { 
				// we have not seen and modified this species before
				String oldUUID = currentEdge.getInputFlatSpecies().getEntityUUID();
				this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge.getInputFlatSpecies());
				newUUIDToOldUUIDMap.put(currentEdge.getInputFlatSpecies().getEntityUUID(), oldUUID);
			}
			if(!newUUIDToOldUUIDMap.containsKey(currentEdge.getOutputFlatSpecies().getEntityUUID())) {
				// we have not seen this species already
				String oldUUID = currentEdge.getOutputFlatSpecies().getEntityUUID();
				this.sbmlSimpleModelUtilityServiceImpl.resetGraphBaseEntityProperties(currentEdge.getOutputFlatSpecies());
				newUUIDToOldUUIDMap.put(currentEdge.getOutputFlatSpecies().getEntityUUID(), oldUUID);
			}
			newEdges.add(currentEdge);
			
			
		}
		// save the new edges in one go, thus also saving the new species linked in them
		// this should not throw an OptimisticLockingException as it is all saved in one go
		Iterable<FlatEdge> persistedFlatEdges = this.flatEdgeRepository.saveAll(newEdges);
		
		// now iterate over the persisted set to form the provenance edges
		Iterator<FlatEdge> pfeIt = persistedFlatEdges.iterator();
		Set<String> connectedSpeciesSet = new HashSet<>();
		Map<String, String> oldToNewMap = new HashMap<>();
		
		while (pfeIt.hasNext()) {
			FlatEdge currentPersistedEdge = pfeIt.next();
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getInputFlatSpecies().getEntityUUID())) {
				String newUUID = currentPersistedEdge.getInputFlatSpecies().getEntityUUID();
				String oldUUID = newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				//this.provenanceGraphService.connect(currentPersistedEdge.getInputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()), currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
			}
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID())) {
				//this.provenanceGraphService.connect(currentPersistedEdge.getOutputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				oldToNewMap.put(newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()), currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
				connectedSpeciesSet.add(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
			}
				
			System.out.println(currentPersistedEdge.getEntityUUID());
		}
		return oldToNewMap;
	}
	
	@Override
	public String testFlatMappingExtract(String entityUUID) {
		
		Map<String, String> oldToNewMap = this.cloneAndPersistNetworkAndReturnOldToNewMap(entityUUID);
		session.clear();
		connectProvenance(oldToNewMap);
		
		/*while (pfeIt.hasNext()) {
			FlatEdge currentPersistedEdge = pfeIt.next();
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getInputFlatSpecies().getEntityUUID())) {
				String newUUID = currentPersistedEdge.getInputFlatSpecies().getEntityUUID();
				String oldUUID = newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
				this.provenanceGraphService.connect(currentPersistedEdge.getInputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getInputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				connectedSpeciesSet.add(currentPersistedEdge.getInputFlatSpecies().getEntityUUID());
			}
			if(!connectedSpeciesSet.contains(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID())) {
				this.provenanceGraphService.connect(currentPersistedEdge.getOutputFlatSpecies(), newUUIDToOldUUIDMap.get(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				connectedSpeciesSet.add(currentPersistedEdge.getOutputFlatSpecies().getEntityUUID());
			}
				
			System.out.println(currentPersistedEdge.getEntityUUID());
		}*/
		return "yes";
	}

	/**
	 * @param oldToNewMap
	 */
	@Transactional
	private void connectProvenance(Map<String, String> oldToNewMap) {
		for (String oldUUID : oldToNewMap.keySet()) {
			String newUUID = oldToNewMap.get(oldUUID);
			this.provenanceGraphService.connect(newUUID, oldUUID, ProvenanceGraphEdgeType.wasDerivedFrom);
		}
	}

}
