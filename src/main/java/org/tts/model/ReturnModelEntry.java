package org.tts.model;

import org.springframework.hateoas.ResourceSupport;

public class ReturnModelEntry extends ResourceSupport {

	private String modelName;
	public ReturnModelEntry(GraphModel model) {
		if(model != null) {
			this.model = model;
			this.modelName = model.getModelName();
			this.modelId = model.getId();
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

	public GraphModel getModel() {
		return model;
	}

	public void setModel(GraphModel model) {
		this.model = model;
	}

	private Long modelId;
	
	private GraphModel model;
	
	
}
