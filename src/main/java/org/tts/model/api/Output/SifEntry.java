package org.tts.model.api.Output;

import java.util.List;

public class SifEntry {

	private String startnode;
	
	private String edge;
	
	private List<String> endnodes;

	public SifEntry() {
	
	}

	public SifEntry(String startnode, String edge, List<String> endnodes) {
		super();
		this.startnode = startnode;
		this.edge = edge;
		this.endnodes = endnodes;
	}
	
	

	public String getStartnode() {
		return startnode;
	}

	public void setStartnode(String startnode) {
		this.startnode = startnode;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public List<String> getEndnodes() {
		return endnodes;
	}

	public void setEndnodes(List<String> endnodes) {
		this.endnodes = endnodes;
	}
	
	public SifEntry addEndnode(String node) {
		getEndnodes().add(node);
		return this;
	}
	
	public String toString() {
		return String.join(" ", getStartnode(), getEdge(), String.join(" ", getEndnodes()));
		
	}
	
}
