package org.tts.service.simpleModel;

import java.util.List;
import java.util.Map;

import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.NodeEdgeList;

public interface NetworkMappingService {

	public NodeEdgeList getProteinInteractionNetwork(List<String> interactionTypes);
	
	public NodeEdgeList getProteinInteractionNetwork();

	public List<String> getTransitionTypes();
	
	public Map<String, FilterOptions> getFilterOptions();

	public FilterOptions addNetworkMapping(FilterOptions filterOptions);

	public FilterOptions getFullFilterOptions();

	public boolean filterOptionsExists(FilterOptions filterOptions);

	public FilterOptions getExistingFilterOptions(FilterOptions filterOptions);

	public FilterOptions getFilterOptions(String uuid);

	public NodeEdgeList getMappingFromFilterOptions(FilterOptions filterOptionsFromId);
	
}
