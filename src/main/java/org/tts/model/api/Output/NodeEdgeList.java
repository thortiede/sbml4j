package org.tts.model.api.Output;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.ResourceSupport;

public class NodeEdgeList extends ResourceSupport {
	
	private Long listId = null;
	
	private String name = null;
	
	private List<NodeNodeEdge> nodeNodeEdgeList;
	
	private Map<String, String> annotationType;
	
	
	/**
	 * Default Constructor
	 * Needed for building the beans
	 */
	public NodeEdgeList() {
		this.nodeNodeEdgeList = new ArrayList<NodeNodeEdge>();
	}



	public Long getListId() {
		return listId;
	}



	public void setListId(Long id) {
		this.listId = id;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public List<NodeNodeEdge> getNodeNodeEdgeList() {
		return nodeNodeEdgeList;
	}

	public void setNodeNodeEdgeList(List<NodeNodeEdge> nodeNodeEdgeList) {
		this.nodeNodeEdgeList = nodeNodeEdgeList;
	}
	
	public Map<String, String> getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(Map<String, String> annotationType) {
		this.annotationType = annotationType;
	}
	
	public NodeEdgeList addListEntry(String node1, String node1UUID, String node2, String node2UUID, String edge) {
		this.getNodeNodeEdgeList().add(new NodeNodeEdge(node1, node1UUID, node2, node2UUID, edge));
		return this;
	}
	
	public NodeEdgeList addListEntry(String node1, String node1UUID, Map<String, Object> node1Annotation, String node2, String node2UUID, Map<String, Object> node2Annotation, String edge) {
		this.getNodeNodeEdgeList().add(new NodeNodeEdge(node1, node1UUID, node1Annotation, node2, node2UUID, node2Annotation, edge));
		return this;
	}
	
	
}
