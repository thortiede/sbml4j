/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.provenance.ProvenanceGraphEdge;
import org.tts.repository.provenance.ProvenanceEntityRepository;
import org.tts.repository.provenance.ProvenanceGraphActivityNodeRepository;
import org.tts.repository.provenance.ProvenanceGraphAgentNodeRepository;
import org.tts.repository.provenance.ProvenanceGraphEdgeRepository;

@Service
public class ProvenanceGraphService {

	@Autowired
	private ProvenanceEntityRepository provenanceEntityRepository;
	@Autowired
	private ProvenanceGraphAgentNodeRepository provenanceGraphAgentNodeRepository;
	@Autowired
	private GraphBaseEntityService graphBaseEntityService;
	@Autowired
	private ProvenanceGraphActivityNodeRepository provenanceGraphActivityNodeRepository;
	@Autowired
	private ProvenanceGraphEdgeRepository provenanceGraphEdgeRepository;
	
	/**
	 * Creates a ProvenanceGraphAgentNode if it not already exists for that type and the given name
	 * If it exists, it adds missing Properties from agentNodeProperties to the node
	 * 
	 * @param agentNodeProperties
	 * @return
	 */
	public ProvenanceGraphAgentNode createProvenanceGraphAgentNode(
			Map<String, Object> agentNodeProperties) {
		
		switch ((ProvenanceGraphAgentType)agentNodeProperties.get("graphagenttype")) {
		case User:
			//String graphagenttype = ((ProvenanceGraphAgentType)agentNodeProperties.get("graphagenttype")).name();
			ProvenanceGraphAgentNode provenanceGraphAgentNode = 
			this.provenanceGraphAgentNodeRepository.findByGraphAgentTypeAndGraphAgentName(
					(ProvenanceGraphAgentType)agentNodeProperties.get("graphagenttype"), 
					agentNodeProperties.get("graphagentname").toString()
					);

			if (provenanceGraphAgentNode == null) {
				provenanceGraphAgentNode = new ProvenanceGraphAgentNode();
				this.graphBaseEntityService.setGraphBaseEntityProperties(provenanceGraphAgentNode);
				provenanceGraphAgentNode.setGraphAgentType((ProvenanceGraphAgentType) agentNodeProperties.get("graphagenttype"));
				provenanceGraphAgentNode.setGraphAgentName((String) agentNodeProperties.get("graphagentname"));
				return this.provenanceGraphAgentNodeRepository.save(provenanceGraphAgentNode);
			} else {
				return provenanceGraphAgentNode;
			}
			
		case Organisation:
			break;
		case SoftwareAgent:
			break;
		default:
			break;
		}
		return null;
	}

	/**
	 * Creates a ProvenanceGraphActivityNode if not already present for Key: graphactivitytype and 
	 * @param activityNodeProperties Properties map to be put in the provenance properties filed of the node
	 * @param activityType the type of the activity from GraphEnum.ProvenanceGraphActivityType
	 * @return the persisted node
	 */
	public ProvenanceGraphActivityNode createProvenanceGraphActivityNode(
			Map<String, Object> activityNodeProperties) {
	
		ProvenanceGraphActivityNode provenanceGraphActivityNode = 
		this.provenanceGraphActivityNodeRepository.findByGraphActivityTypeAndGraphActivityName(
				(ProvenanceGraphActivityType) activityNodeProperties.get("graphactivitytype"),
				activityNodeProperties.get("graphactivityname").toString()
				);

		if(provenanceGraphActivityNode == null) {
			provenanceGraphActivityNode = new ProvenanceGraphActivityNode();
			this.graphBaseEntityService.setGraphBaseEntityProperties(provenanceGraphActivityNode);
		}
		provenanceGraphActivityNode.setGraphActivityType((ProvenanceGraphActivityType) activityNodeProperties.get("graphactivitytype"));
		provenanceGraphActivityNode.setGraphActivityName((String) activityNodeProperties.get("graphactivityname"));
		
		return this.provenanceGraphActivityNodeRepository.save(provenanceGraphActivityNode);
	}

	/**
	 * connect two provenance entities. The entity is not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param source the startNode of the edge
	 * @param target the endNode of the edge
	 * @param edgetype the provenanceGraphEdgeType
	 */
	public void connect(ProvenanceEntity source, ProvenanceEntity target,
			ProvenanceGraphEdgeType edgetype) {
		// TODO: Make sure Edge does not exist yet...
		if (!this.provenanceEntityRepository.areProvenanceEntitiesConnectedWithProvenanceEdgeType(source.getEntityUUID(), target.getEntityUUID(), edgetype)) {
			ProvenanceGraphEdge newEdge = new ProvenanceGraphEdge();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newEdge);
			newEdge.setProvenanceGraphEdgeType(edgetype);
			newEdge.setStartNode(source);
			newEdge.setEndNode(target);
			
			this.provenanceGraphEdgeRepository.save(newEdge, 0);
		}
	}

	/**
	 * connect two provenance entities. The entity is not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param source the startNode of the edge
	 * @param targetEntityUUID the entityUUID of the endNode of the edge, this gets queried in the db and used for connection
	 * @param edgetype the provenanceGraphEdgeType
	 */
	public void connect (ProvenanceEntity source, String targetEntityUUID, 
			ProvenanceGraphEdgeType edgetype) {
		this.connect(source, this.getByEntityUUID(targetEntityUUID), edgetype);
	}
	/**
	 * connect two provenance entities. The entity is not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param sourceEntityUUID the entityUUID of the startNode of the edge, this gets queried in the db and used for connection
	 * @param target the endNode of the edge
	 * @param edgetype the provenanceGraphEdgeType
	 */
	public void connect (String sourceEntityUUID, ProvenanceEntity target,
			ProvenanceGraphEdgeType edgetype) {
		this.connect(getByEntityUUID(sourceEntityUUID), target, edgetype);
	}
	/**
	 * connect two provenance entities. The entity is not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param sourceEntityUUID the entityUUID of the startNode of the edge, this gets queried in the db and used for connection
	 * @param targetEntityUUID the entityUUID of the endNode of the edge, this gets queried in the db and used for connection
	 * @param edgetype the provenanceGraphEdgeType
	 */
	public void connect (String sourceEntityUUID, String targetEntityUUID,
			ProvenanceGraphEdgeType edgetype) {
		this.connect(getByEntityUUID(sourceEntityUUID),
						getByEntityUUID(targetEntityUUID), 
						edgetype);
	}
	
	/**
	 * connect a set of source provenance entities with one target entity. The entities are not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param sourceEntities the startNodes of the edges
	 * @param target the endNode of the edge
	 * @param edgetype the provenanceGraphEdgeType
	 */
	public void connect (Iterable<ProvenanceEntity> sourceEntities, ProvenanceEntity target, ProvenanceGraphEdgeType edgetype) {
		for (ProvenanceEntity source : sourceEntities) {
			this.connect(source, target, edgetype);
		}
	}
	
	/**
	 * connect a source entity with a set of target provenance entities. The entities are not returned. 
	 * Should that be required in the future, it needs to be changed here
	 * @param sourceEntities the startNodes of the edges
	 * @param target the endNode of the edge
	 * @param edgetype the provenanceGraphEdgeType
	 */
	public void connect (ProvenanceEntity source, Iterable<ProvenanceEntity> targetEntities, ProvenanceGraphEdgeType edgetype) {
		for (ProvenanceEntity target : targetEntities) {
			this.connect(source, target, edgetype);
		}
	}
	
	/**
	 * Find a ProvenanceGraphAgentNode for a ProvenanceGraphAgentType and a Name
	 * @param type The ProvenanceGraphAgentType of the ProvenanceGraphAgentNode to find
	 * @param name The name string of the ProvenanceGraphAgentNode to find
	 * @return ProvenanceGraphAgentNode found for the input, or null otherwise
	 */
	public ProvenanceGraphAgentNode findProvenanceGraphAgentNode(ProvenanceGraphAgentType type, String name) {
		return this.provenanceGraphAgentNodeRepository.findByGraphAgentTypeAndGraphAgentName(type, name);
	}
	
	public ProvenanceEntity getByEntityUUID(String entityUUID) {
		return this.provenanceEntityRepository.findByEntityUUID(entityUUID);
	}
	
	public boolean areProvenanceEntitiesConnectedWithProvenanceEdgeType(String sourceEntityUUID, String targetEntityUUID, ProvenanceGraphEdgeType edgetype) {
		return this.provenanceEntityRepository.areProvenanceEntitiesConnectedWithProvenanceEdgeType(sourceEntityUUID, targetEntityUUID, edgetype);
	}
	

	public ProvenanceEntity findByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType edgetype,
			String startNodeEntityUUID) {
		return this.provenanceEntityRepository.findByProvenanceGraphEdgeTypeAndStartNode(edgetype, startNodeEntityUUID);
	}
	
	public Iterable<ProvenanceEntity> findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType edgetype,
			String startNodeEntityUUID) {
		return this.provenanceEntityRepository.findAllByProvenanceGraphEdgeTypeAndStartNode(edgetype, startNodeEntityUUID);
	}
	
	public ProvenanceEntity findByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType edgetype,
			String endNodeEntityUUID) {
		return this.provenanceEntityRepository.findByProvenanceGraphEdgeTypeAndEndNode(edgetype, endNodeEntityUUID);
	}
	
	public Iterable<ProvenanceEntity> findAllByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType edgetype,
			String endNodeEntityUUID) {
		return this.provenanceEntityRepository.findAllByProvenanceGraphEdgeTypeAndEndNode(edgetype, endNodeEntityUUID);
	}

	public void deleteProvenanceEntity(ProvenanceEntity entity) {
		this.provenanceEntityRepository.delete(entity);	
	}
	
	
}
