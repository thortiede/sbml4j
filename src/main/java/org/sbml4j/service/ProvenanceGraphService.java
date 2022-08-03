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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.sbml4j.model.api.ApiRequestItem;
import org.sbml4j.model.api.ApiResponseItem;
import org.sbml4j.model.api.network.AnnotationItem;
import org.sbml4j.model.api.network.FilterOptions;
import org.sbml4j.model.api.network.NodeList;
import org.sbml4j.model.api.network.OverviewNetworkItem;
import org.sbml4j.model.api.pathway.PathwayCollectionCreationItem;
import org.sbml4j.model.api.pathway.PathwayCollectionItem;
import org.sbml4j.model.api.provenance.ActivityItem;
import org.sbml4j.model.api.provenance.ActivityItemParams;
import org.sbml4j.model.api.provenance.AgentItem;
import org.sbml4j.model.api.provenance.ProvenanceInfoItem;
import org.sbml4j.model.api.warehouse.DatabaseInventoryItem;
import org.sbml4j.model.api.warehouse.FileInventoryItem;
import org.sbml4j.model.base.GraphEnum.Operation;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphAgentType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.provenance.ProvenanceGraphEdge;
import org.sbml4j.model.provenance.ProvenanceMetaDataNode;
import org.sbml4j.model.warehouse.DatabaseNode;
import org.sbml4j.model.warehouse.FileNode;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.model.warehouse.PathwayCollectionNode;
import org.sbml4j.model.warehouse.PathwayNode;
import org.sbml4j.repository.provenance.ProvenanceEntityRepository;
import org.sbml4j.repository.provenance.ProvenanceGraphActivityNodeRepository;
import org.sbml4j.repository.provenance.ProvenanceGraphAgentNodeRepository;
import org.sbml4j.repository.provenance.ProvenanceGraphEdgeRepository;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.sbml4j.service.networks.NetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


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
	@Autowired
	private PathwayService pathwayService;
	@Autowired
	private NetworkService networkService;
	
	
	public boolean addProvenanceAnnotationMap(ProvenanceEntity entity, Map<String,Map<String,Object> > provenanceAnnotation) {
		int depth = 1;
		int maxSubDepth = 0;
		for (String key : provenanceAnnotation.keySet()) {
			Map<String, Object> val = provenanceAnnotation.get(key);
			
			ProvenanceMetaDataNode subelement = this.createProvenanceMetaDataNode(key);
			int subdepth = this.addProvenanceAnnotation(subelement, val);
			if (subdepth > maxSubDepth) {
				maxSubDepth = subdepth;
			}
			entity.addProvenanceAnnotationSubelement(subelement);
		}
		this.provenanceEntityRepository.save(entity, depth + maxSubDepth);
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public int addProvenanceAnnotation(ProvenanceEntity entity, Map<String,Object> provenanceAnnotation) {
		try {
			int depth = 0;
			for (String key : provenanceAnnotation.keySet()) {
				Object val = provenanceAnnotation.get(key);
				if (val instanceof Map) {
					depth = 1;
					ProvenanceMetaDataNode subelement = this.createProvenanceMetaDataNode(key);
					int subdepth = this.addProvenanceAnnotation(subelement, (Map<String,Object>) val);
					depth += subdepth;
					entity.addProvenanceAnnotationSubelement(subelement);
				} else if (val instanceof Integer) {
					entity.addProvenance(key, ((Integer) val).longValue());
				} else if (val instanceof Double) {
					entity.addProvenance(key, (Double) val);
				} else if (val instanceof Float) {
					entity.addProvenance(key, ((Float) val).doubleValue());	
				} else if (val instanceof Long) {
					entity.addProvenance(key, (Long) val);
				} else if (val instanceof Boolean) {
					entity.addProvenance(key, (Boolean) val);
				} else {
					entity.addProvenance(key, (String) val);
				}
			}
			
			//entity.addProvenance(provenanceAnnotation);
			
			//this.provenanceEntityRepository.save(entity, 0);
			return depth;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}	
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
	 * Creates a {@link ProvenanceGraphActivityNode}
	 * Method is synchonized, only one thread at a time is allowed to access or create {@link ProvenanceGraphActivityNode}s
	 * @param activityNodeProperties Properties map to be put in the provenance properties filed of the node
	 * @return the persisted node
	 */
	public synchronized ProvenanceGraphActivityNode createProvenanceGraphActivityNode(
			Map<String, Object> activityNodeProperties) {
	
		ProvenanceGraphActivityNode provenanceGraphActivityNode = new ProvenanceGraphActivityNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(provenanceGraphActivityNode);
		
		provenanceGraphActivityNode.setGraphActivityType((ProvenanceGraphActivityType) activityNodeProperties.get("graphactivitytype"));
		provenanceGraphActivityNode.setGraphActivityName((String) activityNodeProperties.get("graphactivityname"));
		
		return this.provenanceGraphActivityNodeRepository.save(provenanceGraphActivityNode);
	}

	public ProvenanceMetaDataNode createProvenanceMetaDataNode(String name) {
		ProvenanceMetaDataNode node = new ProvenanceMetaDataNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(node);
		node.setProvenanceName(name);
		//this.addProvenanceAnnotation(node, data);
		//node.addProvenance(data);
		return node;
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
	 * Delete the {@link ProvenanceEntity} given
	 * @param entity The {@link ProvenanceEntity} to be deleted
	 */
	public void deleteProvenanceEntity(ProvenanceEntity entity) {
		this.provenanceEntityRepository.delete(entity);	
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
	 * Find a {@link ProvenanceEntity} that is the EndNode of a {@link ProvenanceGraphEdge}
	 * of type {@link ProvenanceGraphEdgeType} where the StartNode is a {@link ProvenanceEntity} with the UUID provided
	 * @param edgetype The {@link ProvenanceGraphEdgeType} of the edge to look for
	 * @param startNodeEntityUUID The {@link String} containing the UUID of the {@link ProvenanceEntity} which is the StartNode of the Edge
	 * @return The {@link ProvenanceEntity} that is the EndNode of the {@link ProvenanceGraphEdge}, or null if none was found
	 */
	@Deprecated
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
	@Deprecated
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
	 * Search through all {@link ProvenanceEntity}ies that are connected to the warehouseEntity with the provided UUID and return the
	 * one entity that was generated last (by searching the createDate property)
	 * @param warehouseEntityNodeEntityUUID The entityUUID of the WarehouseEntity connected to the ProvenanceGraphActivityNode
	 * @return {@link ProvenanceEntity} that was created last and generated the 
	 */
	public ProvenanceEntity findLatestGraphActivityNodeForWarehouseEntity(String warehouseEntityNodeEntityUUID) {
		// find latest activity for network and add the annotation to it
		Iterable<ProvenanceEntity> activities = this.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, warehouseEntityNodeEntityUUID);
		ProvenanceEntity lastActivity = StreamSupport.stream(activities.spliterator(), false).max(Comparator.comparing(ProvenanceEntity::getCreateDate)).get();
		return lastActivity;
	}
	
	/**
	 * Retrieve {@link ProvenanceEntity} with given UUID
	 * @param entityUUID The {@link String} containing the UUID of the entity to find
	 * @return The found {@link ProvenanceEntity} or null if not found
	 */
	public ProvenanceEntity getByEntityUUID(String entityUUID) {
		return this.provenanceEntityRepository.findByEntityUUID(entityUUID);
	}

	public List<ProvenanceMetaDataNode> findAllProvenanceSubelements(String entityUUID) {
		return this.provenanceEntityRepository.findAllProvenanceMetaDataNodes(entityUUID);
	}
	
	public ActivityItem getActivityItem(ProvenanceGraphActivityNode activity) {
		ProvenanceGraphActivityType type = activity.getGraphActivityType();
		ActivityItem activityItem = new ActivityItem().name(activity.getGraphActivityName()).type(type.toString());
		
		List<Map<String, Object> > activityProvenanceList = null;
		String bodyString = null;
		boolean isBodyPresent = false;
		// follow the PROV_SUBELEMENT connections
		Iterable<ProvenanceMetaDataNode> metaDataNodes = this.findAllProvenanceSubelements(activity.getEntityUUID());
		for (ProvenanceMetaDataNode metaDataNode : metaDataNodes) {
			String annotationName = metaDataNode.getProvenanceName();
			Map<String, Object> provenanceAnnotation = metaDataNode.getProvenance();
			// process the annotation
			switch (annotationName) {
			case "endpoint":
				activityItem.endpoint((String) provenanceAnnotation.get("endpoint")).operation((String) provenanceAnnotation.get("operation"));
				break;
			case "params":
				for (String key : provenanceAnnotation.keySet()) {
					activityItem.addParamsItem(new ActivityItemParams().parameter(key).value(String.valueOf(provenanceAnnotation.get(key))));
				}
				break;
			case "body":
				// There is a body element.
				// Save it and process at the end (since I need the endpoint and operation data for the creation of the correct element
				bodyString = (String) provenanceAnnotation.get("body");
				isBodyPresent = true;
				
				break;
			default:
				if (activityProvenanceList == null) {
					activityProvenanceList = new ArrayList<>();
				}
				// process subelements along the PROV_SUBELEMENT edges
				// here the list consists of Map<String, Object> elements
				// where each entry is composed of the annotationName as the key and the contents of the Subelement as the Object, which is again a Map<String,Object>
				// this has to be built recursively.
				// the singular elements are found in the provenanceAnnotation Map
				// the submaps need to be parsed through the subelements
				activityProvenanceList.add(this.createProvenanceListEntry(metaDataNode));
	
				break;
			}
		}
		// process body
		if (isBodyPresent && bodyString != null) {
			ApiRequestItem bodyItem = null;
			try {	
				switch(type) {
				case addCsvAnnotation:
					// csv file
					break;
				case addJsonAnnotation:
					// AnnotationItem
					bodyItem = new ObjectMapper().readValue(bodyString, AnnotationItem.class);
					break;
				case addMyDrugNodes:
					break;
				case copyNetwork:
					break;
				case createContext:
					// OverviewNetworkItem, if name contains overview
					if (activityItem.getEndpoint().contains("/overview")) {
						bodyItem = new ObjectMapper().readValue(bodyString, OverviewNetworkItem.class);
					}
					// NodeList, if op=POST
					else if (activityItem.getOperation().equals(Operation.POST.getOperation())) {
						bodyItem = new ObjectMapper().readValue(bodyString, NodeList.class);
					}
					break;
				case createMapping:
					break;
				case createPathwayCollection:
					// PathwayCollectionCreationItem
					bodyItem = new ObjectMapper().readValue(bodyString, PathwayCollectionCreationItem.class);
					break;
				case filterNetwork:
					// FilterOptions
					bodyItem = new ObjectMapper().readValue(bodyString, FilterOptions.class);
					break;
				case persistFile:
					// SBML file, if FileType = SBML
					// GraphML file, if FileType = GraphML
					break;
				case runAlgorithm:
					break;
				default:
					break;
					
				}
				// Set the body element, if the bodyItem was created.
				if (bodyItem != null) activityItem.body(bodyItem); 
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// add the provenanceList to the activityItem
		if (activityProvenanceList != null) {
			activityItem.setProvenance(activityProvenanceList);
		}
		
		
		return activityItem;
	}
	
	

	public ProvenanceInfoItem getProvenanceInfoItem(String uuid) {
		ProvenanceInfoItem item = new ProvenanceInfoItem();
		// 1. Find activity
		Iterable<ProvenanceEntity> generators = this.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasGeneratedBy, uuid);
		Iterator<ProvenanceEntity> iter = generators.iterator();
		while (iter.hasNext()) {
			ProvenanceEntity generator = iter.next();
			//Map<String, Object> provenance = generator.getProvenance();
			//generator.getProvenance()
			if (ProvenanceGraphActivityNode.class.isInstance(generator)) {
				item.addWasGeneratedByItem(this.getActivityItem((ProvenanceGraphActivityNode) generator));		
			}
			
		}
		
		// 2a. Find members if they exist
		boolean hadMember = false;
		Iterable<ProvenanceEntity> members = this.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.hadMember, uuid);
		for (ProvenanceEntity member : members) {
			ProvenanceInfoItem memberItem = this.getProvenanceInfoItem(member.getEntityUUID());
			item.addWasDerivedFromItem(memberItem);
			hadMember = true;
		}
				
				
		// 2b. Recurse through all wasDerivedFrom Elements
		if (!hadMember) {
			Iterable<ProvenanceEntity> derivers = this.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasDerivedFrom, uuid);
			Iterator<ProvenanceEntity> deriveIter = derivers.iterator();
			while (deriveIter.hasNext() ) {
				ProvenanceEntity deriver = deriveIter.next();
				ProvenanceInfoItem deriverItem = getProvenanceInfoItem(deriver.getEntityUUID());
				item.addWasDerivedFromItem(deriverItem);
			}
		}
		
		// 3. Get the content
		ApiResponseItem content = this.getApiResponseItem(uuid);
		item.setContents(content);
		
		// 4. Get the type
		item.setType(this.getProvenanceInfoItemType(uuid));
		
		// 5. get attributedTo
		Iterable<ProvenanceEntity> agents = this.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.wasAttributedTo, uuid);
		Iterator<ProvenanceEntity> agentIter = agents.iterator();
		boolean hasAgent = false;
		while (agentIter.hasNext()) {
			ProvenanceEntity agent = agentIter.next();
			if (!hasAgent)
				if (ProvenanceGraphAgentNode.class.isInstance(agent)) {
				item.setWasAttributedTo(this.getAgentItem((ProvenanceGraphAgentNode) agent));
				hasAgent = true;
				} else {
					log.warn("Endnode of ProvenanceEdge with type wasAttributedTo is not of type ProvenanceGraphAgentNode (uuid: " + agent.getEntityUUID() + ")");
			} else {
				log.warn("Multiple ProvenanceEdges with type wasAttributedTo from entity with uuid: " + uuid);
			}
		}
		
		return item;
	}
	
	private AgentItem getAgentItem(ProvenanceGraphAgentNode agent) {
		return new AgentItem().name(agent.getGraphAgentName()).type(agent.getGraphAgentType().name());
	}

	private ApiResponseItem getApiResponseItem(String uuid) {
		ProvenanceEntity entity = this.getByEntityUUID(uuid);
		ApiResponseItem item = null;
		if (DatabaseNode.class.isInstance(entity)) {
			DatabaseNode db = (DatabaseNode) entity;
			item = new DatabaseInventoryItem().source(db.getSource()).version(db.getSourceVersion());
		} else if (FileNode.class.isInstance(entity)) {
			FileNode f = (FileNode) entity;
			item = new FileInventoryItem().filename(f.getFilename()).fileNodeType(f.getFileNodeType().name()).md5sum(f.getMd5sum());
		} else if (MappingNode.class.isInstance(entity)) {
			item = this.networkService.getNetworkInventoryItem(uuid);
		} else if (PathwayNode.class.isInstance(entity)) {
			item = this.pathwayService.getPathwayInventoryItem(uuid);
		} else if (PathwayCollectionNode.class.isInstance(entity)) {
			PathwayCollectionNode pwc = (PathwayCollectionNode) entity;
			PathwayCollectionItem pwcItem = new PathwayCollectionItem().name(pwc.getPathwayCollectionName()).description(pwc.getPathwayCollectionDescription());
			Iterable<ProvenanceEntity> members = this.findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType.hadMember, entity.getEntityUUID());
			for (ProvenanceEntity member : members) {
				pwcItem.addHadMemberItem(member.getEntityUUID());
			}
			item = pwcItem;
		}
		if (item == null) {
			item = new ApiResponseItem();
		}
		return item;
	}
	
	private String getProvenanceInfoItemType(String uuid) {
		ProvenanceEntity entity = this.getByEntityUUID(uuid);
		String type = null;
		if (DatabaseNode.class.isInstance(entity)) {
			type = "Database";
		} else if (FileNode.class.isInstance(entity)) {
			type = "File";
		} else if (MappingNode.class.isInstance(entity)) {
			type = "Network";
		} else if (PathwayNode.class.isInstance(entity)) {
			type="Pathway";
		} else if (PathwayCollectionNode.class.isInstance(entity)) {
			type = "PathwayCollection";
		}

		return type;
	}
	
	private Map<String, Object> createProvenanceListEntry(ProvenanceMetaDataNode metaDataNode) {
		
		Map<String, Object> provenanceListEntry = new HashMap<>();
				
		Map<String, Object> nodeProvenance = metaDataNode.getProvenance();
		if (nodeProvenance == null) {
			nodeProvenance = new HashMap<>();
		}
		
		for (ProvenanceMetaDataNode subNode : this.findAllProvenanceSubelements(metaDataNode.getEntityUUID())) {
			nodeProvenance.putAll(this.createProvenanceListEntry(subNode));
			//nodeProvenance.put(subNode.getProvenanceName(), this.createProvenanceListEntry(subNode));
		}
		provenanceListEntry.put(metaDataNode.getProvenanceName(), nodeProvenance);
		return provenanceListEntry;
	}
}

