package org.tts.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.NodeEdgeList;
import org.tts.model.NodeNodeEdge;
import org.tts.repository.GraphBaseEntityRepository;
import org.tts.service.SimpleModel.NetworkMappingService;

@Service
public class NetworkMappingServiceImpl implements NetworkMappingService {

	GraphBaseEntityRepository graphBaseEntityRepository;
	
	/*private enum interactionTypes {
		DISSOCIATION ("dissociation"),
		

	}*/
	@Autowired
	public NetworkMappingServiceImpl(GraphBaseEntityRepository graphBaseEntityRepository) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
	}

	@Override
	public NodeEdgeList getProteinInteractionNetwork(List<String> interactionTypes) {
		if(interactionTypes == null) {
			// no filter given, return network with all interaction types
			Iterable<NodeNodeEdge> isIsInteractions = graphBaseEntityRepository.getInteractionCustom1("BQB_IS", "BQB_IS");
			NodeEdgeList allInteractions = new NodeEdgeList();
			for (NodeNodeEdge nne : isIsInteractions) {
				allInteractions.addListEntry(nne.getNode1(), nne.getNode2(), nne.getEdge());
			}
			return allInteractions;
		} else {
			// filter the network by the given types.
			// how would you implement that efficiently?
		}
		return null;
	}

	@Override
	public NodeEdgeList getProteinInteractionNetwork() {
		return getProteinInteractionNetwork(null);
	}

}
