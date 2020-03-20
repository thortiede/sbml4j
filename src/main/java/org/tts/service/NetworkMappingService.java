package org.tts.service;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;

public interface NetworkMappingService {

	public NodeEdgeList getProteinInteractionNetwork(List<String> interactionTypes);
	
	public NodeEdgeList getProteinInteractionNetwork();

	public List<String> getTransitionTypes();
	
	public Map<String, FilterOptions> getFilterOptions();

	//public FilterOptions addNetworkMapping(FilterOptions filterOptions);

	public FilterOptions getFullFilterOptions();

	public boolean filterOptionsExists(FilterOptions filterOptions);

	public FilterOptions getExistingFilterOptions(FilterOptions filterOptions);

	public FilterOptions getFilterOptions(String uuid);

	//public NodeEdgeList getMappingFromFilterOptions(FilterOptions filterOptionsFromId);
	
	
	
	public MappingNode createMappingFromPathway(PathwayNode pathway, NetworkMappingType type, IDSystem idSystem, ProvenanceGraphActivityNode activityNode, ProvenanceGraphAgentNode agentNode) throws Exception;

	FlatSpecies persistFlatSpecies(FlatSpecies species);

	Iterable<FlatSpecies> persistListOfFlatSpecies(List<FlatSpecies> flatSpeciesList);

	
	
}
