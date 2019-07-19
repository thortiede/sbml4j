package org.tts.model.api.Output;

import java.util.List;

public class NetworkInventoryItemDetail extends NetworkInventoryItem {

	public NetworkInventoryItemDetail() {
		super();
	}

	public NetworkInventoryItemDetail(NetworkInventoryItem item) {
		super(item);
	}

	private List<String> nodes;

	public List<String> getNodes() {
		return nodes;
	}

	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}
	
}
