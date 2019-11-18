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
	private SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
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
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(provenanceGraphAgentNode);
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
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(provenanceGraphActivityNode);
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
			this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(newEdge);
			newEdge.setProvenanceGraphEdgeType(edgetype);
			newEdge.setStartNode(source);
			newEdge.setEndNode(target);
			
			this.provenanceEntityRepository.save(newEdge, 0);
		}
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
	
	
}
