package org.tts.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public NetworkMappingServiceImpl(GraphBaseEntityRepository graphBaseEntityRepository) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
	}

	@Override
	public NodeEdgeList getProteinInteractionNetwork(List<String> interactionTypes) {
		String[] bqList = {"BQB_IS", "BQB_HAS_VERSION"};
		String[] functionList = {"noGroup", "groupLeft", "groupRight", "groupBoth"};
		NodeEdgeList allInteractions = new NodeEdgeList();
		if(interactionTypes == null) {
			// no filter given, return network with all interaction types
			Iterable<NodeNodeEdge> loopNNE;
			for(String bqType1 : bqList) {
				for (String  bqType2 : bqList) {
					for (String functionName : functionList) {
						int i = 0;
						loopNNE = getTransitionsBetween(bqType1, bqType2, functionName);
						for (NodeNodeEdge nne : loopNNE) {
							allInteractions.addListEntry(nne.getNode1(), nne.getNode2(), org.sbml.jsbml.SBO.getTerm(nne.getEdge()).getName());
							i++;
						}
						logger.info(bqType1 + "_" + bqType2 + "_" + functionName + "_interactions: " + i);
					}
				}
			}
			
			
			
			
			return allInteractions;
		} else {
			// filter the network by the given types.
			// how would you implement that efficiently?
		}
		return null;
	}

	/**
	 * @param bqType1
	 * @param bqType2
	 * @param functionName
	 * @return
	 */
	private Iterable<NodeNodeEdge> getTransitionsBetween(String bqType1, String bqType2, String functionName) {
		Iterable<NodeNodeEdge> interactions;
		switch (functionName) {
		case "noGroup":
			interactions = graphBaseEntityRepository.getInteractionCustomNoGroup(bqType1, bqType2);
			break;
		case "groupLeft":
			interactions = graphBaseEntityRepository.getInteractionCustomGroupOnInput(bqType1, bqType2);
			break;
		case "groupRight":
			interactions = graphBaseEntityRepository.getInteractionCustomGroupOnOutput(bqType1, bqType2);
			break;
		case "groupBoth":
			interactions = graphBaseEntityRepository.getInteractionCustomGroupOnBoth(bqType1, bqType2);
			break;
		default:
			return null;
		}
		return interactions;
	}

	@Override
	public NodeEdgeList getProteinInteractionNetwork() {
		return getProteinInteractionNetwork(null);
	}

}
