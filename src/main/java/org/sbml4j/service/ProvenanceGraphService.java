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
package org.sbml4j.service;

import java.util.Map;

import org.sbml4j.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.provenance.ProvenanceGraphEdge;
import org.sbml4j.repository.provenance.ProvenanceEntityRepository;
import org.sbml4j.repository.provenance.ProvenanceGraphActivityNodeRepository;
import org.sbml4j.repository.provenance.ProvenanceGraphAgentNodeRepository;
import org.sbml4j.repository.provenance.ProvenanceGraphEdgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProvenanceGraphService {

	Logger log = LoggerFactory.getLogger(ProvenanceGraphService.class);
	
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
	 * If it exists, it returns the existing node
	 * 
	 * @param agentNodeProperties
	 * @return
	 */
	public ProvenanceGraphAgentNode createProvenanceGraphAgentNode(
			Map<String, Object> agentNodeProperties) {
		return this.createProvenanceGraphAgentNode(agentNodeProperties.get("graphagentname").toString(), (ProvenanceGraphAgentType)agentNodeProperties.get("graphagenttype"));
	}
	
	/**
	 * Creates a {@link ProvenanceGraphAgentNode} if it not already exists for that type and the given name
	 * If it exists, it returns the existing node
	 * Method is synchonized, only one thread at a time is allowed to access or create {@link ProvenanceGraphAgentNode}s
	 * @param name The name of the agent
	 * @param graphAgentType The {@link ProvenanceGraphAgentType} for the node to create
	 * @return The {@link ProvenanceGraphAgentNode} that has been created or found
	 */
	public synchronized ProvenanceGraphAgentNode createProvenanceGraphAgentNode(String name, ProvenanceGraphAgentType graphAgentType) {
					
		log.info("Searching ProvenanceGraphAgentNode for " + name);
		
		switch (graphAgentType) {
		case User:
			ProvenanceGraphAgentNode provenanceGraphAgentNode = 
			this.provenanceGraphAgentNodeRepository.findByGraphAgentTypeAndGraphAgentName(graphAgentType, name);
			if (provenanceGraphAgentNode == null) {
				log.info("No GraphAgentNode found for user " + name + ". Creating new..");
				provenanceGraphAgentNode = new ProvenanceGraphAgentNode();
				this.graphBaseEntityService.setGraphBaseEntityProperties(provenanceGraphAgentNode);
				provenanceGraphAgentNode.setGraphAgentType(graphAgentType);
				provenanceGraphAgentNode.setGraphAgentName(name);
				return this.provenanceGraphAgentNodeRepository.save(provenanceGraphAgentNode);
			} else {
				log.info("Using GraphAgentNode: " + provenanceGraphAgentNode.getGraphAgentName() + " with uuid " + provenanceGraphAgentNode.getEntityUUID());
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
	 * Creates a {@link ProvenanceGraphActivityNode} if not already present for Key: graphactivitytype and graphactivityName
	 * Method is synchonized, only one thread at a time is allowed to access or create {@link ProvenanceGraphActivityNode}s
	 * @param activityNodeProperties Properties map to be put in the provenance properties filed of the node
	 * @return the persisted node
	 */
	public synchronized ProvenanceGraphActivityNode createProvenanceGraphActivityNode(
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
	 * Find a {@link ProvenanceGraphAgentNode} for a {@link ProvenanceGraphAgentType} and a Name
	 * @param type The {@link ProvenanceGraphAgentType} of the {@link ProvenanceGraphAgentNode} to find
	 * @param name The name string of the {@link ProvenanceGraphAgentNode} to find
	 * @return {@link ProvenanceGraphAgentNode} found for the input, or null otherwise
	 */
	public ProvenanceGraphAgentNode findProvenanceGraphAgentNode(ProvenanceGraphAgentType type, String name) {
		return this.provenanceGraphAgentNodeRepository.findByGraphAgentTypeAndGraphAgentName(type, name);
	}
	
	/**
	 * Retrieve {@link ProvenanceEntity} with given UUID
	 * @param entityUUID The {@link String} containing the UUID of the entity to find
	 * @return The found {@link ProvenanceEntity} or null if not found
	 */
	public ProvenanceEntity getByEntityUUID(String entityUUID) {
		return this.provenanceEntityRepository.findByEntityUUID(entityUUID);
	}
	
	/**
	 * Check whether two {@link ProvenanceEntity} are connected with a {@link ProvenanceGraphEdge} of type {@link ProvenanceGraphEdgeType}
	 * @param sourceEntityUUID The {@link String} containing the UUID of the startNode of the {@link ProvenanceGraphEdge}
	 * @param targetEntityUUID he {@link String} containing the UUID of the endNode of the {@link ProvenanceGraphEdge}
	 * @param edgetype The {@link ProvenanceGraphEdgeType} of the edge to look for
	 * @return true if the given Nodes are connected by this type of {@link ProvenanceGraphEdge}, false otherwise
	 */
	public boolean areProvenanceEntitiesConnectedWithProvenanceEdgeType(String sourceEntityUUID, String targetEntityUUID, ProvenanceGraphEdgeType edgetype) {
		return this.provenanceEntityRepository.areProvenanceEntitiesConnectedWithProvenanceEdgeType(sourceEntityUUID, targetEntityUUID, edgetype);
	}
	
	/**
	 * Find a {@link ProvenanceEntity} that is the EndNode of a {@link ProvenanceGraphEdge}
	 * of type {@link ProvenanceGraphEdgeType} where the StartNode is a {@link ProvenanceEntity} with the UUID provided
	 * @param edgetype The {@link ProvenanceGraphEdgeType} of the edge to look for
	 * @param startNodeEntityUUID The {@link String} containing the UUID of the {@link ProvenanceEntity} which is the StartNode of the Edge
	 * @return The {@link ProvenanceEntity} that is the EndNode of the {@link ProvenanceGraphEdge}, or null if none was found
	 */
	public ProvenanceEntity findByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType edgetype,
			String startNodeEntityUUID) {
		return this.provenanceEntityRepository.findByProvenanceGraphEdgeTypeAndStartNode(edgetype, startNodeEntityUUID);
	}
	
	/**
	 * Find all {@link ProvenanceEntity}ies that are EndNodes of {@link ProvenanceGraphEdge}s
	 * of type {@link ProvenanceGraphEdgeType} where the StartNode is a {@link ProvenanceEntity} with the UUID provided
	 * @param edgetype The {@link ProvenanceGraphEdgeType} of the edge to look for
	 * @param startNodeEntityUUID The {@link String} containing the UUID of the {@link ProvenanceEntity} which is the StartNode of the Edge
	 * @return An {@link Iterable} of {@link ProvenanceEntity} that are the EndNodes of {@link ProvenanceGraphEdge}s, or null if none was found
	 */
	public Iterable<ProvenanceEntity> findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType edgetype,
			String startNodeEntityUUID) {
		return this.provenanceEntityRepository.findAllByProvenanceGraphEdgeTypeAndStartNode(edgetype, startNodeEntityUUID);
	}
	
	/**
	 * Find a {@link ProvenanceEntity} that is the StartNode of a {@link ProvenanceGraphEdge}
	 * of type {@link ProvenanceGraphEdgeType} where the EndNode is a {@link ProvenanceEntity} with the UUID provided
	 * @param edgetype The {@link ProvenanceGraphEdgeType} of the edge to look for
	 * @param endNodeEntityUUID The {@link String} containing the UUID of the {@link ProvenanceEntity} which is the EndNode of the Edge
	 * @return The {@link ProvenanceEntity} that is the StartNode of the {@link ProvenanceGraphEdge}, or null if none was found
	 */
	public ProvenanceEntity findByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType edgetype,
			String endNodeEntityUUID) {
		return this.provenanceEntityRepository.findByProvenanceGraphEdgeTypeAndEndNode(edgetype, endNodeEntityUUID);
	}
	
	/**
	 * Find all {@link ProvenanceEntity}ies that are StartNodes of {@link ProvenanceGraphEdge}s
	 * of type {@link ProvenanceGraphEdgeType} where the EndNode is a {@link ProvenanceEntity} with the UUID provided
	 * @param edgetype The {@link ProvenanceGraphEdgeType} of the edge to look for
	 * @param endNodeEntityUUID The {@link String} containing the UUID of the {@link ProvenanceEntity} which is the EndNode of the Edge
	 * @return An {@link Iterable} of {@link ProvenanceEntity} that are the StartNodes of {@link ProvenanceGraphEdge}s, or null if none was found
	 */
	public Iterable<ProvenanceEntity> findAllByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType edgetype,
			String endNodeEntityUUID) {
		return this.provenanceEntityRepository.findAllByProvenanceGraphEdgeTypeAndEndNode(edgetype, endNodeEntityUUID);
	}

	/**
	 * Delete the {@link ProvenanceEntity} given
	 * @param entity The {@link ProvenanceEntity} to be deleted
	 */
	public void deleteProvenanceEntity(ProvenanceEntity entity) {
		this.provenanceEntityRepository.delete(entity);	
	}
}
