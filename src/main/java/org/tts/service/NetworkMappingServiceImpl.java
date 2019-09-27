package org.tts.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tomcat.jni.Time;
import org.sbml.jsbml.SBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.NodeNodeEdge;
import org.tts.model.api.Output.SifFile;
import org.tts.model.common.ContentGraphNode;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.flat.FlatNetworkMapping;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLSpeciesRepository;
import org.tts.repository.flat.FlatNetworkMappingRepository;
import org.tts.repository.flat.FlatSpeciesRepository;
import org.tts.repository.provenance.ProvenanceEntityRepository;
import org.tts.repository.simpleModel.SBMLSimpleTransitionRepository;

@Service
public class NetworkMappingServiceImpl implements NetworkMappingService {

	GraphBaseEntityRepository graphBaseEntityRepository;
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	FlatNetworkMappingRepository flatNetworkMappingRepository;
	FlatSpeciesRepository flatSpeciesRepository;
	SBMLSpeciesRepository sbmlSpeciesRepository;
	ProvenanceEntityRepository provenanceEntityRepository;
	
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	WarehouseGraphService warehouseGraphService;
	ProvenanceGraphService provenanceGraphService;
	UtilityService utilityService;
	FileService fileService;
	FileStorageService fileStorageService;
	GraphMLService graphMLService;
	
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
			SBMLSpeciesRepository sbmlSpeciesRepository,
			ProvenanceEntityRepository provenanceEntityRepository,
			WarehouseGraphService warehouseGraphService,
			ProvenanceGraphService provenanceGraphService,
			UtilityService utilityService,
			FileService fileService,
			FileStorageService fileStorageService,
			GraphMLService graphMLService) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.sbmlSimpleTransitionRepository = sbmlSimpleTransitionRepository;
		this.sbmlSimpleModelUtilityServiceImpl = sbmlSimpleModelUtilityServiceImpl;
		this.flatNetworkMappingRepository = flatNetworkMappingRepository;
		this.flatSpeciesRepository = flatSpeciesRepository;
		this.sbmlSpeciesRepository = sbmlSpeciesRepository;
		this.provenanceEntityRepository = provenanceEntityRepository;
		this.warehouseGraphService = warehouseGraphService;
		this.provenanceGraphService = provenanceGraphService;
		this.utilityService = utilityService;
		this.fileService = fileService;
		this.fileStorageService = fileStorageService;
		this.graphMLService = graphMLService;
	}

	@Override
	public List<String> getTransitionTypes() {
		List<String> transitionTypes = new ArrayList<>();
		this.sbmlSimpleTransitionRepository.getTransitionTypes().forEach(sboString -> {
			transitionTypes.add(this.utilityService.translateSBOString(sboString));
		});
		return transitionTypes;
	}
	
	private List<String> getNodeTypes() {
		List<String> nodeTypes = new ArrayList<>();
		this.sbmlSpeciesRepository.getNodeTypes().forEach(sboString -> {
			nodeTypes.add(this.utilityService.translateSBOString(sboString));
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
		//returnFilterOptions.setNetworkType(flatNetworkMapping.getNetworkType());
		returnFilterOptions.setRelationTypes(Arrays.asList(flatNetworkMapping.getRelationshipTypes()));
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
						String transitionType = this.utilityService.translateSBOString(nne.getEdge());
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
		//newFlatNetworkMapping.setNetworkType(filterOptions.getNetworkType());
		newFlatNetworkMapping.setEntityUUID(newFlatNetworkMapping.getNetworkType() + "_" + UUID.randomUUID().toString());
		newFlatNetworkMapping.setNodeTypes(filterOptions.getNodeTypes().toArray(new String[filterOptions.getNodeTypes().size()]));
		newFlatNetworkMapping.setRelationshipTypes(filterOptions.getRelationTypes().toArray(new String[filterOptions.getRelationTypes().size()]));
		return newFlatNetworkMapping;
	}

	@Override
	public FilterOptions getFullFilterOptions() {
		FilterOptions fullFilterOptions = new FilterOptions();
		fullFilterOptions.setRelationTypes(getTransitionTypes());
		fullFilterOptions.setNodeTypes(getNodeTypes());
	//	fullFilterOptions.setNetworkType(getNetworkTypes());
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
		//filterOptions.setNetworkType(flatNetworkMapping.getNetworkType());
		filterOptions.setNodeTypes(Arrays.asList(flatNetworkMapping.getNodeTypes()));
		filterOptions.setRelationTypes(Arrays.asList(flatNetworkMapping.getRelationshipTypes()));
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
					mapping.addListEntry(node1.getSymbol(), node1.getSimpleModelEntityUUID(), node2.getSymbol(), node2.getSimpleModelEntityUUID(), this.utilityService.translateSBOString(relationType));
				}
			});
		}
		mapping.setName(flatNetworkMapping.getEntityUUID());
		return mapping;
	}

	@Override
	public MappingNode createMappingFromPathway(PathwayNode pathway, NetworkMappingType type, IDSystem idSystem, ProvenanceGraphActivityNode activityNode, ProvenanceGraphAgentNode agentNode) {
		
		Instant startTime = Instant.now();
		logger.info("Started Creating Mapping from Pathway at " + startTime.toString());
		int numberOfRelations = 0;
		// need a Warehouse Node of Type Mapping, set NetworkMappingTypes
		String mappingName = "Map_" + type.name() + "_" + pathway.getPathwayIdString() + "_" + idSystem.name();
		
		MappingNode mappingFromPathway = this.warehouseGraphService.createMappingNode(pathway, type, mappingName);
		logger.info("Created MappingNode with uuid:" + mappingFromPathway.getEntityUUID());
		Set<String> relationTypes = new HashSet<>();
		Set<String> nodeTypes = new HashSet<>();
		List<String> transitionSBOTerms = new ArrayList<>();
		List<String> nodeSBOTerms = new ArrayList<>();
		Iterable<NodeNodeEdge> flatTransitionsOfPathway = new ArrayList<>();;
		// connect MappingNode to PathwayNode with wasDerivedFrom
		this.provenanceGraphService.connect(mappingFromPathway, pathway, ProvenanceGraphEdgeType.wasDerivedFrom);
		// connect MappingNode to Activity with wasGeneratedBy
		this.provenanceGraphService.connect(mappingFromPathway, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
		// attribute the new Mapping to the agent
		this.provenanceGraphService.connect(mappingFromPathway, agentNode, ProvenanceGraphEdgeType.wasAttributedTo);
		
		// TODO divert here for different mapping types
		if(type.equals(NetworkMappingType.PATHWAYMAPPING)) {
			// get all Transitions from Pathway
			flatTransitionsOfPathway = 
						this.graphBaseEntityRepository.getAllFlatTransitionsForPathway(pathway.getEntityUUID(), idSystem);
			
		} else if (type.equals(NetworkMappingType.METABOLIC)) {
			flatTransitionsOfPathway = 
					this.graphBaseEntityRepository.getFlatMetabolicReactionsForPathway(pathway.getEntityUUID(), idSystem);
			
		} else if (type.equals(NetworkMappingType.PPI)) {
			nodeSBOTerms.add("SBO:0000252");
			String conversionSBO = SBO.getTerm(182).getId();
			transitionSBOTerms.add(conversionSBO);
			for (String conversionChildSBO : this.utilityService.getAllSBOChildren(conversionSBO)) {
				transitionSBOTerms.add(conversionChildSBO);
			}
			String biochemicalOrTransportReactionSBO = SBO.getTerm(167).getId();
			transitionSBOTerms.add(biochemicalOrTransportReactionSBO);
			for (String biochemicalOrTransportReactionChildSBO : this.utilityService.getAllSBOChildren(biochemicalOrTransportReactionSBO)) {
				transitionSBOTerms.add(biochemicalOrTransportReactionChildSBO);
			}
			String molecularInteractionSBO = SBO.getTerm(344).getId();
			transitionSBOTerms.add(molecularInteractionSBO);
			for (String molecularInteractionChildSBO : this.utilityService.getAllSBOChildren(molecularInteractionSBO)) {
				transitionSBOTerms.add(molecularInteractionChildSBO);
			}
			
			flatTransitionsOfPathway = 
					this.graphBaseEntityRepository.getFlatTransitionsForPathway(pathway.getEntityUUID(), idSystem, transitionSBOTerms);
		
		} else if (type.equals(NetworkMappingType.REGULATORY)) {
			nodeSBOTerms.add("SBO:0000252");
			nodeSBOTerms.add("SBO:0000247");
			String controlSBO = "SBO:0000168";
			transitionSBOTerms.add(controlSBO);
			for (String controlChildSBO : this.utilityService.getAllSBOChildren(controlSBO)) {
				transitionSBOTerms.add(controlChildSBO);
			}
			flatTransitionsOfPathway = 
					this.graphBaseEntityRepository.getFlatTransitionsForPathway(pathway.getEntityUUID(), idSystem, transitionSBOTerms, nodeSBOTerms);
			
		} else if (type.equals(NetworkMappingType.SIGNALLING)) {
			nodeSBOTerms.add("SBO:0000252");
			transitionSBOTerms.add("SBO:0000656");
			transitionSBOTerms.add("SBO:0000170");
			transitionSBOTerms.add("SBO:0000169");
			flatTransitionsOfPathway = 
					this.graphBaseEntityRepository.getFlatTransitionsForPathway(pathway.getEntityUUID(), idSystem, transitionSBOTerms, nodeSBOTerms);
		} 
		
		// create FlatSpecies for QueryResults
		Map<String, FlatSpecies> networkFlatSpecies = new HashMap<>();
		int counter = 1;
		for(NodeNodeEdge nne : flatTransitionsOfPathway) {
			logger.info(Instant.now().toString() + ": Processing NNE #" + counter++);
			// on creation of Flat Species, provenanceConnect them to the initialEntities (and to the Transition?)
			FlatSpecies node2;
			if (networkFlatSpecies.containsKey(nne.getNode2())) {
				// Node2 already in List, we can build a relationship to it, so extract it
				node2 = networkFlatSpecies.get(nne.getNode2());
				logger.info(Instant.now().toString() + ": Using Flat Species for Node2 with symbol " + node2.getSymbol());
			} else {
				// Node 2 does not yet exist, we need to create it:
				node2 = new FlatSpecies();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node2); // this uses the utility for simpleModel, but they are stored in same db, so should be fine
				node2.setSymbol(nne.getNode2());
				node2.setSboTerm(nne.getNode2Type());
				//logger.info(Instant.now().toString() + ": Persisting Flat Species for Node2 with symbol " + nne.getNode2());
				//node2 = this.flatSpeciesRepository.save(node2);
				nodeTypes.add(nne.getNode2Type());
				node2.setSimpleModelEntityUUID(nne.getNode2UUID());
				//this.provenanceGraphService.connect(node2, this.provenanceEntityRepository.findByEntityUUID(nne.getNode2UUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				//this.provenanceGraphService.connect(node2, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				//this.warehouseGraphService.connect(mappingFromPathway, node2, WarehouseGraphEdgeType.CONTAINS);
				networkFlatSpecies.put(node2.getSymbol(), node2);
				//logger.info(Instant.now().toString() + ": Persisting Flat Species for Node2 with symbol " + nne.getNode2() + " Created prov and warehouse Links");
			}
			// at this point we have node2 (be it already existing, or just created)
			FlatSpecies node1;
			if (networkFlatSpecies.containsKey(nne.getNode1())) {
				// node 1 does already exist
				node1 = networkFlatSpecies.get(nne.getNode1());
				logger.info(Instant.now().toString() + ": Using Flat Species for Node1 with symbol " + node1.getSymbol());
			} else {
				// node 1 does not already exist, create it and add relation to node 2
				node1 = new FlatSpecies();
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node1);// this uses the utility for simpleModel, but they are stored in same db, so should be fine
				node1.setSymbol(nne.getNode1());
				node1.setSboTerm(nne.getNode1Type());
				//logger.info(Instant.now().toString() + ": Persisting Flat Species for Node1 with symbol " + nne.getNode1());
				//node1 = this.flatSpeciesRepository.save(node1);
				nodeTypes.add(nne.getNode1Type());
				node1.setSimpleModelEntityUUID(nne.getNode1UUID());
				//this.provenanceGraphService.connect(node1, this.provenanceEntityRepository.findByEntityUUID(nne.getNode1UUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
				//this.provenanceGraphService.connect(node1, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				//this.warehouseGraphService.connect(mappingFromPathway, node1, WarehouseGraphEdgeType.CONTAINS);
				//logger.info(Instant.now().toString() + ": Persisting Flat Species for Node1 with symbol " + nne.getNode1() + " Created prov and warehouse Links");
			}
			 //int sbo = SBO.convertAlias2SBO(nne.getEdge().toUpperCase());
			node1.addRelatedSpecies(node2, nne.getEdge());
			numberOfRelations++;
			relationTypes.add(nne.getEdge());
			//logger.info(Instant.now().toString() + ": Adding relationship " + nne.getEdge());
			//node1 = this.flatSpeciesRepository.save(node1);			
			networkFlatSpecies.put(node1.getSymbol(), node1); // this replaces node1 with the version that has the new relationship
			logger.info(Instant.now().toString() + ": Adding relationship " + nne.getEdge() + " ..Done");
		}
		Instant endTime = Instant.now();
		mappingFromPathway.addWarehouseAnnotation("creationstarttime", startTime.toString());
		mappingFromPathway.addWarehouseAnnotation("creationendtime", endTime.toString());
		mappingFromPathway.addWarehouseAnnotation("numberofnodes",  String.valueOf(networkFlatSpecies.size()));
		mappingFromPathway.addWarehouseAnnotation("numberofrelations", String.valueOf(numberOfRelations));
		mappingFromPathway.setMappingNodeTypes(nodeTypes);
		mappingFromPathway.setMappingRelationTypes(relationTypes);
		
		logger.info(Instant.now().toString() + ": Finished Building internal Map. Collecting FlatSpecies for persistence..");
		List<FlatSpecies> allFlatSpeciesOfMapping = new ArrayList<>();
		for (String key : networkFlatSpecies.keySet()) {
			FlatSpecies fs = networkFlatSpecies.get(key);
			allFlatSpeciesOfMapping.add(fs);
		}
		logger.info(Instant.now().toString() + ": Finished Collecting Flat Species for persistence.");
		MappingNode persistedMappingOfPathway = (MappingNode) this.warehouseGraphService.saveWarehouseGraphNodeEntity(mappingFromPathway, 0);
		logger.info(Instant.now().toString() + ": Persisted Mapping Node. Starting Persistence of FlatSpecies");
		Iterable<FlatSpecies> persistedFlatSpeciesOfMapping = this. persistListOfFlatSpecies(allFlatSpeciesOfMapping);// saveAll(allFlatSpeciesOfMapping);
		logger.info(Instant.now().toString() + ": Persisted FlatSpecies. Starting to connect");
		int fsConnectCounter = 1;
		for (FlatSpecies fs : persistedFlatSpeciesOfMapping) {
			logger.info(Instant.now().toString() + ": Building ConnectionSet #" + fsConnectCounter++);
			this.provenanceGraphService.connect(fs, this.provenanceEntityRepository.findByEntityUUID(fs.getSimpleModelEntityUUID()), ProvenanceGraphEdgeType.wasDerivedFrom);
			this.provenanceGraphService.connect(fs, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			this.warehouseGraphService.connect(persistedMappingOfPathway, fs, WarehouseGraphEdgeType.CONTAINS);
		}
		logger.info("Finished Creating Mapping from Pathway at " + endTime.toString());
		return persistedMappingOfPathway;
	}
	
	/**
	 * Convert a NodeEdgeList to a returnable Resource Object
	 * @param nodeEdgeList The nodeEdgeList to convert
	 * @return a sifResource as ByteArrayResource
	 */
	@Override
	public Resource getResourceFromNodeEdgeList(NodeEdgeList nodeEdgeList, String type) {
		switch (type) {
		case "sif":
		
			SifFile sifFile = this.fileService.getSifFromNodeEdgeList(nodeEdgeList);
		
		    //Resource resource = fileStorageService.loadFileAsResource(fileName);
			if(sifFile != null) {
			    Resource resource = this.fileStorageService.getSifAsResource(sifFile);
			    return resource;
			} else {
				return null;
			}
		case "graphml":
			
			ByteArrayOutputStream bo = this.graphMLService.getGraphMLByteArrayOutputStream(nodeEdgeList, new ByteArrayOutputStream());
			
			
			//String graphMLString = graphMLService.getGraphMLString(nodeEdgeList);
			
			return new ByteArrayResource(bo.toByteArray(), "network.graphml");
		
		default:
			return null;		
		}
	}
	
	

	@Override
	public Iterable<FlatSpecies> persistListOfFlatSpecies(List<FlatSpecies> flatSpeciesList) {
		return this.flatSpeciesRepository.save(flatSpeciesList, 1);
	}
	
	@Override
	public FlatSpecies persistFlatSpecies(FlatSpecies species) {
		return this.flatSpeciesRepository.save(species, 1);
	}
	
}
