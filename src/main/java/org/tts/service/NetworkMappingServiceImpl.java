package org.tts.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.sbml.jsbml.SBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.NodeNodeEdge;
import org.tts.model.flat.FlatNetworkMapping;
import org.tts.model.flat.FlatSpecies;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLSpeciesRepository;
import org.tts.repository.flat.FlatNetworkMappingRepository;
import org.tts.repository.flat.FlatSpeciesRepository;
import org.tts.repository.simpleModel.SBMLSimpleTransitionRepository;

@Service
public class NetworkMappingServiceImpl implements NetworkMappingService {

	GraphBaseEntityRepository graphBaseEntityRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	FlatNetworkMappingRepository flatNetworkMappingRepository;
	FlatSpeciesRepository flatSpeciesRepository;
	SBMLSpeciesRepository sbmlSpeciesRepository;
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	
	/*private enum interactionTypes {
		DISSOCIATION ("dissociation"),
		

	}*/
	
	private static int QUERY_DEPTH_ZERO = 0;
	
	private static int QUERY_DEPTH_TWO = 2;
	
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public NetworkMappingServiceImpl(
			GraphBaseEntityRepository graphBaseEntityRepository,
			SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository,
			FlatNetworkMappingRepository flatNetworkMappingRepository,
			SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl,
			FlatSpeciesRepository flatSpeciesRepository,
			SBMLSpeciesRepository sbmlSpeciesRepository) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.sbmlSimpleTransitionRepository = sbmlSimpleTransitionRepository;
		this.sbmlSimpleModelUtilityServiceImpl = sbmlSimpleModelUtilityServiceImpl;
		this.flatNetworkMappingRepository = flatNetworkMappingRepository;
		this.flatSpeciesRepository = flatSpeciesRepository;
		this.sbmlSpeciesRepository = sbmlSpeciesRepository;
	}

	@Override
	public List<String> getTransitionTypes() {
		List<String> transitionTypes = new ArrayList<>();
		this.sbmlSimpleTransitionRepository.getTransitionTypes().forEach(sboString -> {
			transitionTypes.add(translateSBOString(sboString));
		});
		return transitionTypes;
	}
	
	private List<String> getNodeTypes() {
		List<String> nodeTypes = new ArrayList<>();
		this.sbmlSpeciesRepository.getNodeTypes().forEach(sboString -> {
			nodeTypes.add(translateSBOString(sboString));
		});
		return nodeTypes;
	}
	
	public Map<String, FilterOptions> getFilterOptions() {
		Map<String, FilterOptions> returnMap = new HashMap<>();
		this.flatNetworkMappingRepository.findAll().forEach(flatNetworkMapping->{
			returnMap.put(flatNetworkMapping.getEntityUUID(), mapNetworkMappingToFilterOptions(flatNetworkMapping));
		});
		return returnMap;
	}
	
	
	private FilterOptions mapNetworkMappingToFilterOptions(FlatNetworkMapping flatNetworkMapping) {
		FilterOptions returnFilterOptions = new FilterOptions();
		returnFilterOptions.setNetworkType(flatNetworkMapping.getNetworkType());
		returnFilterOptions.setTransitionTypes(Arrays.asList(flatNetworkMapping.getRelationshipTypes()));
		returnFilterOptions.setNodeTypes(Arrays.asList(flatNetworkMapping.getNodeTypes()));
		return returnFilterOptions;
	}

	public Iterable<FlatSpecies> generateFlatNetwork(NodeEdgeList nodeEdgeListToGenerateFrom) {
		//NodeEdgeList nodeEdgeListToGenerateFrom = this.getProteinInteractionNetwork();
		List<NodeNodeEdge> allEntries = nodeEdgeListToGenerateFrom.getNodeNodeEdgeList();
		
		Map<String, FlatSpecies> flatSpeciesMap = new HashMap<>();
		
		for (NodeNodeEdge nne : allEntries) {
			if(flatSpeciesMap.containsKey(nne.getNode1UUID())) {
				if(flatSpeciesMap.containsKey(nne.getNode2UUID())) {
					// both flatSpecies already created, we can connect them
					flatSpeciesMap.get(nne.getNode1UUID()).addRelatedSpecies(flatSpeciesMap.get(nne.getNode2UUID()), nne.getEdge());
				} else {
					// second species does not exist yet, create it first
					FlatSpecies node2FlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node2FlatSpecies);
					node2FlatSpecies.setSimpleModelEntityUUID(nne.getNode2UUID());
					flatSpeciesMap.put(nne.getNode2UUID(), node2FlatSpecies);
					flatSpeciesMap.get(nne.getNode1UUID()).addRelatedSpecies(node2FlatSpecies, nne.getEdge());
				}
			} else {
				// node1 does not exist yet, create it first
				FlatSpecies node1FlatSpecies = new FlatSpecies();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node1FlatSpecies);
				node1FlatSpecies.setSimpleModelEntityUUID(nne.getNode1UUID());
				
				if(flatSpeciesMap.containsKey(nne.getNode2UUID())) {
					// second flatSpecies already created, we can connect them
					node1FlatSpecies.addRelatedSpecies(flatSpeciesMap.get(nne.getNode2UUID()), nne.getEdge());	
				} else {
					// second species does not exist yet, create it first
					FlatSpecies node2FlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node2FlatSpecies);
					node2FlatSpecies.setSimpleModelEntityUUID(nne.getNode2UUID());
					flatSpeciesMap.put(nne.getNode2UUID(), node2FlatSpecies);
					node1FlatSpecies.addRelatedSpecies(node2FlatSpecies, nne.getEdge());
					
				}
				// then add species 1
				flatSpeciesMap.put(nne.getNode1UUID(), node1FlatSpecies);
			}
		}
		List<FlatSpecies> returnIterable = new ArrayList<>();
		for(String uuid : flatSpeciesMap.keySet()) {
			returnIterable.add(flatSpeciesMap.get(uuid));
		}
		return returnIterable;
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
						// do not translate sbo term, as it will be used to connect FlatSpecies
						String transitionType = translateSBOString(nne.getEdge());
						if (interactionTypes.contains(transitionType)) {
							/*allInteractions.addListEntry(
									nne.getNode1(), 
									nne.getNode2(), 
									removeWhitespace(transitionType)
							);*/
							nodeNodeEdgeList.add(new NodeNodeEdge(
									nne.getNode1(), 
									nne.getNode1UUID(),
									nne.getNode2(),
									nne.getNode2UUID(),
									nne.getEdge()));
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
	@Deprecated
	public NodeEdgeList getProteinInteractionNetwork() {
		return getProteinInteractionNetwork(this.getTransitionTypes());
	}

	@Override
	public FilterOptions addNetworkMapping(FilterOptions filterOptions) {
		logger.info("Start of NetworkMappingServiceImpl.addNetworkMapping with filterOptions: " + filterOptions.toString());
		if(!filterOptionsExists(filterOptions)) {
			logger.info("FilterOptions do not exist yet. Creating..");
			// make sure it does not exist before we try to create it, just in case
			try {
				FlatNetworkMapping newFlatNetworkMapping = convertToFlatNetworkMapping(filterOptions);
				// persist this node
				newFlatNetworkMapping = this.flatNetworkMappingRepository.save(newFlatNetworkMapping, QUERY_DEPTH_ZERO);
				/*
				 * FUTURE: 
				 * then create the actual mapping with the flat species and relationships
				 * between them store all created nodes in a List, then set this list as
				 * FlatSpeciesList of newFlatNetworkMapping and persist again then the actual
				 * mapping can be retrieved. possibly do this in a separate thread so that this
				 * function can return immediately the function that retrieves the network
				 * itself then needs to check if nodes actually exist it should not be necessary
				 * to have an extra variable to store the status (network created, not created,
				 * in process)
				 */
				
				/* 
				 * CURRENT:
				 * There is only ppi and nodeTypes are ignored
				 * create ppi, if networkType is ppi
				 */
				if (newFlatNetworkMapping.getNetworkType().equals("ppi")) {
					NodeEdgeList ppiWithFilterOptions = getProteinInteractionNetwork(Arrays.asList(newFlatNetworkMapping.getRelationshipTypes()));
					Map<String, FlatSpecies> networkFlatSpecies = new HashMap<>();
					List<FlatSpecies> finalFlatSpeciesList = new ArrayList<>();
					for (NodeNodeEdge nne : ppiWithFilterOptions.getNodeNodeEdgeList()) {
						FlatSpecies node2;
						if (networkFlatSpecies.containsKey(nne.getNode2())) {
							// Node2 already in List, we can build a relationship to it, so extract it
							node2 = networkFlatSpecies.get(nne.getNode2());
						} else {
							// Node 2 does not yet exist, we need to create it:
							node2 = new FlatSpecies();
							this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node2); // this uses the utility for simpleModel, but they are stored in same db, so should be fine
							node2.setSymbol(nne.getNode2());
							node2.setSimpleModelEntityUUID(nne.getNode2UUID());
							networkFlatSpecies.put(node2.getSymbol(), node2);
						}
						// at this point we have node2 (be it already existing, or just created)
						FlatSpecies node1;
						if (networkFlatSpecies.containsKey(nne.getNode1())) {
							// node 1 does already exist
							node1 = networkFlatSpecies.get(nne.getNode1());
						} else {
							// node 1 does not already exist, create it and add relation to node 2
							node1 = new FlatSpecies();
							this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node1);// this uses the utility for simpleModel, but they are stored in same db, so should be fine
							node1.setSymbol(nne.getNode1());
							node1.setSimpleModelEntityUUID(nne.getNode1UUID());
							
						}
						 //int sbo = SBO.convertAlias2SBO(nne.getEdge().toUpperCase());
						node1.addRelatedSpecies(node2, nne.getEdge());
						networkFlatSpecies.put(node1.getSymbol(), node1); // this replaces node1 with the version that has the new relationship
					}
					final String newFlatNetworkMappingEntityUUID = newFlatNetworkMapping.getEntityUUID();
					networkFlatSpecies.forEach((symbol, flatSpecies) -> {
						finalFlatSpeciesList.add(flatSpecies);
						logger.debug("Added FlatSpecies with symbol " + symbol + " to the networkMapping " + newFlatNetworkMappingEntityUUID);
					});
					newFlatNetworkMapping.setFlatSpeciesList(finalFlatSpeciesList);
					return convertToFilterOptions(this.flatNetworkMappingRepository.save(newFlatNetworkMapping));
				} else {
					logger.info("Filter Option does not denote ppi");
					return null;
				}
				
				
			} catch (ClassCastException e) {
				logger.error("Cannot convert filterOptions to flatNetworkMapping. FilterOptions are: " + filterOptions.toString());
				e.printStackTrace();
				return null;
			}
		} else {
			// does already exist
			logger.warn("Network Mapping that should be created does exist");
			return getExistingFilterOptions(filterOptions);
		}
	}

	private FlatNetworkMapping convertToFlatNetworkMapping(FilterOptions filterOptions) throws ClassCastException {
		FlatNetworkMapping newFlatNetworkMapping = new FlatNetworkMapping();
		newFlatNetworkMapping.setNetworkType(filterOptions.getNetworkType());
		newFlatNetworkMapping.setEntityUUID(newFlatNetworkMapping.getNetworkType() + "_" + UUID.randomUUID().toString());
		newFlatNetworkMapping.setNodeTypes(filterOptions.getNodeTypes().toArray(new String[filterOptions.getNodeTypes().size()]));
		newFlatNetworkMapping.setRelationshipTypes(filterOptions.getTransitionTypes().toArray(new String[filterOptions.getTransitionTypes().size()]));
		return newFlatNetworkMapping;
	}

	@Override
	public FilterOptions getFullFilterOptions() {
		FilterOptions fullFilterOptions = new FilterOptions();
		fullFilterOptions.setTransitionTypes(getTransitionTypes());
		fullFilterOptions.setNodeTypes(getNodeTypes());
		fullFilterOptions.setNetworkType(getNetworkTypes());
		return fullFilterOptions;
	}

	/**
	 * Just a hard coded String at this point. Not sure how this is going to be used
	 * Will be used as prefix to the uuid of the mapping.
	 * @return A string containing the available network types to show to the client for filterOptions
	 */
	private String getNetworkTypes() {
		return "One of: ppi, metabolic, signal";
	}

	@Override
	public boolean filterOptionsExists(FilterOptions filterOptions) {
		for (FlatNetworkMapping flatNetworkMapping : this.flatNetworkMappingRepository.findAll(QUERY_DEPTH_ZERO)) {
			if (filterOptions.equals(convertToFilterOptions(flatNetworkMapping))) {
				return true;
			}
		}
		return false;
	}

	private FilterOptions convertToFilterOptions(FlatNetworkMapping flatNetworkMapping) {
		FilterOptions filterOptions = new FilterOptions();
		filterOptions.setMappingUuid(flatNetworkMapping.getEntityUUID());
		filterOptions.setNetworkType(flatNetworkMapping.getNetworkType());
		filterOptions.setNodeTypes(Arrays.asList(flatNetworkMapping.getNodeTypes()));
		filterOptions.setTransitionTypes(Arrays.asList(flatNetworkMapping.getRelationshipTypes()));
		return filterOptions;
	}

	@Override
	public FilterOptions getExistingFilterOptions(FilterOptions filterOptions) {
		for (FlatNetworkMapping flatNetworkMapping : this.flatNetworkMappingRepository.findAll(QUERY_DEPTH_ZERO)) {
			if (filterOptions.equals(convertToFilterOptions(flatNetworkMapping))) {
				return convertToFilterOptions(flatNetworkMapping);
			}
		}
		return null;
	}

	@Override
	public FilterOptions getFilterOptions(String uuid) {
		FlatNetworkMapping existingFlatNetworkMapping = this.flatNetworkMappingRepository.findByEntityUUID(uuid);
		if(existingFlatNetworkMapping != null) {
			return convertToFilterOptions(existingFlatNetworkMapping);
		} else {
			return null;
		}
	}

	@Override
	public NodeEdgeList getMappingFromFilterOptions(FilterOptions filterOptionsFromId) {
		
		NodeEdgeList mapping = new NodeEdgeList();
		
		FlatNetworkMapping flatNetworkMapping = this.flatNetworkMappingRepository.findByEntityUUID(filterOptionsFromId.getMappingUuid(), QUERY_DEPTH_TWO);
		logger.info("Retrieved Flat networkMapping with uuid: " + filterOptionsFromId.getMappingUuid() + ", which has " + flatNetworkMapping.getFlatSpeciesList().size() + " number of nodes.");
		// now convert the FlatSpecies List to NodeEdgeList?
		List<FlatSpecies> flatSpeciesInMapping = flatNetworkMapping.getFlatSpeciesList();
		for (FlatSpecies node1 : flatSpeciesInMapping) {
			node1.getAllRelatedSpecies().forEach((relationType, node2List) -> {
				for (FlatSpecies node2 : node2List) {
					mapping.addListEntry(node1.getSymbol(), node1.getSimpleModelEntityUUID(), node2.getSymbol(), node2.getSimpleModelEntityUUID(), translateSBOString(relationType));
				}
			});
		}
		mapping.setName(flatNetworkMapping.getEntityUUID());
		return mapping;
	}

}
