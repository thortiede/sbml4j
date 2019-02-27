package org.tts.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.tts.model.NodeEdgeList;
import org.tts.model.NodeNodeEdge;
import org.tts.repository.GraphBaseEntityRepository;
import org.tts.repository.SBMLSimpleTransitionRepository;
import org.tts.service.SimpleModel.NetworkMappingService;

@Service
public class NetworkMappingServiceImpl implements NetworkMappingService {

	GraphBaseEntityRepository graphBaseEntityRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	/*private enum interactionTypes {
		DISSOCIATION ("dissociation"),
		

	}*/
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public NetworkMappingServiceImpl(
			GraphBaseEntityRepository graphBaseEntityRepository,
			SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.sbmlSimpleTransitionRepository = sbmlSimpleTransitionRepository;
	}

	@Override
	public List<String> getTransitionTypes() {
		List<String> transitionTypes = new ArrayList<>();
		this.sbmlSimpleTransitionRepository.getTransitionTypes().forEach(sboString -> {
			transitionTypes.add(translateSBOString(sboString));
		});
		return transitionTypes;
	}
	
	@Override
	public NodeEdgeList getProteinInteractionNetwork(List<String> interactionTypes) {
		String[] bqList = {"BQB_IS", "BQB_HAS_VERSION"};
		String[] functionList = {"noGroup", "groupLeft", "groupRight", "groupBoth"};
		NodeEdgeList allInteractions = new NodeEdgeList();
	
		List<NodeNodeEdge> nodeNodeEdgeList = new ArrayList<>();
		
		Iterable<NodeNodeEdge> loopNNE;
		for(String bqType1 : bqList) {
			for (String  bqType2 : bqList) {
				for (String functionName : functionList) {
					int i = 0;
					loopNNE = getTransitionsBetween(bqType1, bqType2, functionName);
					for (NodeNodeEdge nne : loopNNE) {
						String transitionType = translateSBOString(nne.getEdge());
						if (interactionTypes.contains(transitionType)) {
							/*allInteractions.addListEntry(
									nne.getNode1(), 
									nne.getNode2(), 
									removeWhitespace(transitionType)
							);*/
							nodeNodeEdgeList.add(new NodeNodeEdge(
									nne.getNode1(), 
									nne.getNode2(), 
									removeWhitespace(transitionType)));
							i++;
						}
					}
					logger.info(bqType1 + "_" + bqType2 + "_" + functionName + "_interactions: " + i);
				}
			}
		}
		String allInteractionsName = "ppi";
		for (String interactionType : interactionTypes) {
			allInteractionsName += "_";
			allInteractionsName += interactionType;
		}
		allInteractions.setName(allInteractionsName);
		allInteractions.setListId(1L);
		allInteractions.setNodeNodeEdgeList(nodeNodeEdgeList.stream().distinct().collect(Collectors.toList()));
		return allInteractions;
	}

	private String translateSBOString(String sboString) {
		return sboString == "" ? "undefined in source" : org.sbml.jsbml.SBO.getTerm(sboString).getName();
	}
	
	private String removeWhitespace(String input) {
		return input.replaceAll("\\s", "_");
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
		return getProteinInteractionNetwork(this.getTransitionTypes());
	}

}
