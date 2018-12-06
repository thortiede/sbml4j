package org.tts.model;

import org.springframework.hateoas.ResourceSupport;

public class ReturnModelOverviewEntry extends ResourceSupport {

	private String modelName;
	private String modelOriginalFileName;
	private Long modelId;
	private int numNodes;
	private int numEdges;
	
	public ReturnModelOverviewEntry(GraphModel model) {
		if(model != null) {
			this.modelId = model.getId();
			this.modelName = model.getModelName();
			this.numNodes = model.getNumNodes();
			this.numEdges = model.getNumEdges();
			this.modelOriginalFileName = model.getModelOriginalFileName();
			// TODO: Need to account for non-metabolic networks as well
			// So, a model needs to know whether it is metabolic or not
		}
	}
	
	
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public Long getModelId() {
		return modelId;
	}
	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}
	public int getNumNodes() {
		return numNodes;
	}
	public void setNumNodes(int numNodes) {
		this.numNodes = numNodes;
	}
	public int getNumEdges() {
		return numEdges;
	}
	public void setNumEdges(int numEdges) {
		this.numEdges = numEdges;
	}


	public String getModelOriginalFileName() {
		return modelOriginalFileName;
	}


	public void setModelOriginalFileName(String modelOriginalFileName) {
		this.modelOriginalFileName = modelOriginalFileName;
	}
	
	
	
}
