package org.tts.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

public class NodeEdgeList extends ResourceSupport {
	
	private Long listId = null;
	
	private String name = null;
	
	private List<NodeNodeEdge> nodeNodeEdgeList;
	
	
	
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
	
	public NodeEdgeList addListEntry(String node1, String node2, String edge) {
		this.getNodeNodeEdgeList().add(new NodeNodeEdge(node1, node2, edge));
		return this;
	}
		
}
