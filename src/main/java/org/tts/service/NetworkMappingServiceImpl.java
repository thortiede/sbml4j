package org.tts.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.sbml.jsbml.SBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tts.model.api.Input.FilterOptions;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.NodeNodeEdge;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.api.Output.SifFile;
import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatNetworkMapping;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.common.SBMLSpeciesRepository;
import org.tts.repository.flat.FlatEdgeRepository;
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
	FlatEdgeRepository flatEdgeRepository;

	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;
	WarehouseGraphService warehouseGraphService;
	ProvenanceGraphService provenanceGraphService;
	UtilityService utilityService;
	FileService fileService;
	FileStorageService fileStorageService;
	GraphMLService graphMLService;
	FlatEdgeService flatEdgeService;

	private static int QUERY_DEPTH_ZERO = 0;

	private static int QUERY_DEPTH_TWO = 2;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public NetworkMappingServiceImpl(GraphBaseEntityRepository graphBaseEntityRepository,
			SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository,
			FlatNetworkMappingRepository flatNetworkMappingRepository,
			SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl,
			FlatSpeciesRepository flatSpeciesRepository, SBMLSpeciesRepository sbmlSpeciesRepository,
			FlatEdgeRepository flatEdgeRepository, ProvenanceEntityRepository provenanceEntityRepository,
			WarehouseGraphService warehouseGraphService, ProvenanceGraphService provenanceGraphService,
			UtilityService utilityService, FileService fileService, FileStorageService fileStorageService,
			GraphMLService graphMLService, FlatEdgeService flatEdgeService) {
		super();
		this.graphBaseEntityRepository = graphBaseEntityRepository;
		this.sbmlSimpleTransitionRepository = sbmlSimpleTransitionRepository;
		this.sbmlSimpleModelUtilityServiceImpl = sbmlSimpleModelUtilityServiceImpl;
		this.flatNetworkMappingRepository = flatNetworkMappingRepository;
		this.flatSpeciesRepository = flatSpeciesRepository;
		this.sbmlSpeciesRepository = sbmlSpeciesRepository;
		this.flatEdgeRepository = flatEdgeRepository;
		this.provenanceEntityRepository = provenanceEntityRepository;
		this.warehouseGraphService = warehouseGraphService;
		this.provenanceGraphService = provenanceGraphService;
		this.utilityService = utilityService;
		this.fileService = fileService;
		this.fileStorageService = fileStorageService;
		this.graphMLService = graphMLService;
		this.flatEdgeService = flatEdgeService;
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
		this.flatNetworkMappingRepository.findAll().forEach(flatNetworkMapping -> {
			returnMap.put(flatNetworkMapping.getEntityUUID(), mapNetworkMappingToFilterOptions(flatNetworkMapping));
		});
		return returnMap;
	}

	private FilterOptions mapNetworkMappingToFilterOptions(FlatNetworkMapping flatNetworkMapping) {
		FilterOptions returnFilterOptions = new FilterOptions();
		// returnFilterOptions.setNetworkType(flatNetworkMapping.getNetworkType());
		returnFilterOptions.setRelationTypes(Arrays.asList(flatNetworkMapping.getRelationshipTypes()));
		returnFilterOptions.setNodeTypes(Arrays.asList(flatNetworkMapping.getNodeTypes()));
		return returnFilterOptions;
	}

	/*
	 * public Iterable<FlatSpecies> generateFlatNetwork(NodeEdgeList
	 * nodeEdgeListToGenerateFrom) { //NodeEdgeList nodeEdgeListToGenerateFrom =
	 * this.getProteinInteractionNetwork(); List<NodeNodeEdge> allEntries =
	 * nodeEdgeListToGenerateFrom.getNodeNodeEdgeList();
	 * 
	 * Map<String, FlatSpecies> flatSpeciesMap = new HashMap<>();
	 * 
	 * for (NodeNodeEdge nne : allEntries) {
	 * if(flatSpeciesMap.containsKey(nne.getNode1UUID())) {
	 * if(flatSpeciesMap.containsKey(nne.getNode2UUID())) { // both flatSpecies
	 * already created, we can connect them
	 * flatSpeciesMap.get(nne.getNode1UUID()).addRelatedSpecies(flatSpeciesMap.get(
	 * nne.getNode2UUID()), nne.getEdge()); } else { // second species does not
	 * exist yet, create it first FlatSpecies node2FlatSpecies = new FlatSpecies();
	 * this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(
	 * node2FlatSpecies);
	 * node2FlatSpecies.setSimpleModelEntityUUID(nne.getNode2UUID());
	 * flatSpeciesMap.put(nne.getNode2UUID(), node2FlatSpecies);
	 * flatSpeciesMap.get(nne.getNode1UUID()).addRelatedSpecies(node2FlatSpecies,
	 * nne.getEdge()); } } else { // node1 does not exist yet, create it first
	 * FlatSpecies node1FlatSpecies = new FlatSpecies();
	 * this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(
	 * node1FlatSpecies);
	 * node1FlatSpecies.setSimpleModelEntityUUID(nne.getNode1UUID());
	 * 
	 * if(flatSpeciesMap.containsKey(nne.getNode2UUID())) { // second flatSpecies
	 * already created, we can connect them
	 * node1FlatSpecies.addRelatedSpecies(flatSpeciesMap.get(nne.getNode2UUID()),
	 * nne.getEdge()); } else { // second species does not exist yet, create it
	 * first FlatSpecies node2FlatSpecies = new FlatSpecies();
	 * this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(
	 * node2FlatSpecies);
	 * node2FlatSpecies.setSimpleModelEntityUUID(nne.getNode2UUID());
	 * flatSpeciesMap.put(nne.getNode2UUID(), node2FlatSpecies);
	 * node1FlatSpecies.addRelatedSpecies(node2FlatSpecies, nne.getEdge());
	 * 
	 * } // then add species 1 flatSpeciesMap.put(nne.getNode1UUID(),
	 * node1FlatSpecies); } } List<FlatSpecies> returnIterable = new ArrayList<>();
	 * for(String uuid : flatSpeciesMap.keySet()) {
	 * returnIterable.add(flatSpeciesMap.get(uuid)); } return returnIterable; }
	 */

	@Deprecated
	@Override
	public NodeEdgeList getProteinInteractionNetwork(List<String> interactionTypes) {
		String[] bqList = { "BQB_IS", "BQB_HAS_VERSION" };
		String[] functionList = { "noGroup", "groupLeft", "groupRight", "groupBoth" };
		NodeEdgeList allInteractions = new NodeEdgeList();

		List<NodeNodeEdge> nodeNodeEdgeList = new ArrayList<>();

		Iterable<NodeNodeEdge> loopNNE;
		for (String bqType1 : bqList) {
			for (String bqType2 : bqList) {
				for (String functionName : functionList) {
					int i = 0;
					loopNNE = getTransitionsBetween(bqType1, bqType2, functionName);
					for (NodeNodeEdge nne : loopNNE) {
						// do not translate sbo term, as it will be used to connect FlatSpecies
						String transitionType = this.utilityService.translateSBOString(nne.getEdge());
						if (interactionTypes.contains(transitionType)) {
							/*
							 * allInteractions.addListEntry( nne.getNode1(), nne.getNode2(),
							 * removeWhitespace(transitionType) );
							 */
							nodeNodeEdgeList.add(new NodeNodeEdge(nne.getNode1(), nne.getNode1UUID(), nne.getNode2(),
									nne.getNode2UUID(), nne.getEdge()));
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
	@Deprecated
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

	/*
	 * @Override public FilterOptions addNetworkMapping(FilterOptions filterOptions)
	 * { logger.
	 * info("Start of NetworkMappingServiceImpl.addNetworkMapping with filterOptions: "
	 * + filterOptions.toString()); if(!filterOptionsExists(filterOptions)) {
	 * logger.info("FilterOptions do not exist yet. Creating.."); // make sure it
	 * does not exist before we try to create it, just in case try {
	 * FlatNetworkMapping newFlatNetworkMapping =
	 * convertToFlatNetworkMapping(filterOptions); // persist this node
	 * newFlatNetworkMapping =
	 * this.flatNetworkMappingRepository.save(newFlatNetworkMapping,
	 * QUERY_DEPTH_ZERO);
	 * 
	 * FUTURE: then create the actual mapping with the flat species and
	 * relationships between them store all created nodes in a List, then set this
	 * list as FlatSpeciesList of newFlatNetworkMapping and persist again then the
	 * actual mapping can be retrieved. possibly do this in a separate thread so
	 * that this function can return immediately the function that retrieves the
	 * network itself then needs to check if nodes actually exist it should not be
	 * necessary to have an extra variable to store the status (network created, not
	 * created, in process)
	 * 
	 * 
	 * 
	 * CURRENT: There is only ppi and nodeTypes are ignored create ppi, if
	 * networkType is ppi
	 * 
	 * if (newFlatNetworkMapping.getNetworkType().equals("ppi")) { NodeEdgeList
	 * ppiWithFilterOptions =
	 * getProteinInteractionNetwork(Arrays.asList(newFlatNetworkMapping.
	 * getRelationshipTypes())); Map<String, FlatSpecies> networkFlatSpecies = new
	 * HashMap<>(); List<FlatSpecies> finalFlatSpeciesList = new ArrayList<>(); for
	 * (NodeNodeEdge nne : ppiWithFilterOptions.getNodeNodeEdgeList()) { FlatSpecies
	 * node2; if (networkFlatSpecies.containsKey(nne.getNode2())) { // Node2 already
	 * in List, we can build a relationship to it, so extract it node2 =
	 * networkFlatSpecies.get(nne.getNode2()); } else { // Node 2 does not yet
	 * exist, we need to create it: node2 = new FlatSpecies();
	 * this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node2);
	 * // this uses the utility for simpleModel, but they are stored in same db, so
	 * should be fine node2.setSymbol(nne.getNode2());
	 * node2.setSimpleModelEntityUUID(nne.getNode2UUID());
	 * networkFlatSpecies.put(node2.getSymbol(), node2); } // at this point we have
	 * node2 (be it already existing, or just created) FlatSpecies node1; if
	 * (networkFlatSpecies.containsKey(nne.getNode1())) { // node 1 does already
	 * exist node1 = networkFlatSpecies.get(nne.getNode1()); } else { // node 1 does
	 * not already exist, create it and add relation to node 2 node1 = new
	 * FlatSpecies();
	 * this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(node1);//
	 * this uses the utility for simpleModel, but they are stored in same db, so
	 * should be fine node1.setSymbol(nne.getNode1());
	 * node1.setSimpleModelEntityUUID(nne.getNode1UUID());
	 * 
	 * } //int sbo = SBO.convertAlias2SBO(nne.getEdge().toUpperCase());
	 * node1.addRelatedSpecies(node2, nne.getEdge());
	 * networkFlatSpecies.put(node1.getSymbol(), node1); // this replaces node1 with
	 * the version that has the new relationship } final String
	 * newFlatNetworkMappingEntityUUID = newFlatNetworkMapping.getEntityUUID();
	 * networkFlatSpecies.forEach((symbol, flatSpecies) -> {
	 * finalFlatSpeciesList.add(flatSpecies);
	 * logger.debug("Added FlatSpecies with symbol " + symbol +
	 * " to the networkMapping " + newFlatNetworkMappingEntityUUID); });
	 * newFlatNetworkMapping.setFlatSpeciesList(finalFlatSpeciesList); return
	 * convertToFilterOptions(this.flatNetworkMappingRepository.save(
	 * newFlatNetworkMapping)); } else {
	 * logger.info("Filter Option does not denote ppi"); return null; }
	 * 
	 * 
	 * } catch (ClassCastException e) { logger.
	 * error("Cannot convert filterOptions to flatNetworkMapping. FilterOptions are: "
	 * + filterOptions.toString()); e.printStackTrace(); return null; } } else { //
	 * does already exist
	 * logger.warn("Network Mapping that should be created does exist"); return
	 * getExistingFilterOptions(filterOptions); } }
	 */

	@Deprecated
	private FlatNetworkMapping convertToFlatNetworkMapping(FilterOptions filterOptions) throws ClassCastException {
		FlatNetworkMapping newFlatNetworkMapping = new FlatNetworkMapping();
		// newFlatNetworkMapping.setNetworkType(filterOptions.getNetworkType());
		newFlatNetworkMapping
				.setEntityUUID(newFlatNetworkMapping.getNetworkType() + "_" + UUID.randomUUID().toString());
		newFlatNetworkMapping
				.setNodeTypes(filterOptions.getNodeTypes().toArray(new String[filterOptions.getNodeTypes().size()]));
		newFlatNetworkMapping.setRelationshipTypes(
				filterOptions.getRelationTypes().toArray(new String[filterOptions.getRelationTypes().size()]));
		return newFlatNetworkMapping;
	}

	@Override
	public FilterOptions getFullFilterOptions() {
		FilterOptions fullFilterOptions = new FilterOptions();
		fullFilterOptions.setRelationTypes(getTransitionTypes());
		fullFilterOptions.setNodeTypes(getNodeTypes());
		// fullFilterOptions.setNetworkType(getNetworkTypes());
		return fullFilterOptions;
	}

	/**
	 * Just a hard coded String at this point. Not sure how this is going to be used
	 * Will be used as prefix to the uuid of the mapping.
	 * 
	 * @return A string containing the available network types to show to the client
	 *         for filterOptions
	 */
	@Deprecated
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
		// filterOptions.setNetworkType(flatNetworkMapping.getNetworkType());
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
		if (existingFlatNetworkMapping != null) {
			return convertToFilterOptions(existingFlatNetworkMapping);
		} else {
			return null;
		}
	}

	/*
	 * @Override public NodeEdgeList getMappingFromFilterOptions(FilterOptions
	 * filterOptionsFromId) {
	 * 
	 * NodeEdgeList mapping = new NodeEdgeList();
	 * 
	 * FlatNetworkMapping flatNetworkMapping = this.flatNetworkMappingRepository
	 * .findByEntityUUID(filterOptionsFromId.getMappingUuid(), QUERY_DEPTH_TWO);
	 * logger.info("Retrieved Flat networkMapping with uuid: " +
	 * filterOptionsFromId.getMappingUuid() + ", which has " +
	 * flatNetworkMapping.getFlatSpeciesList().size() + " number of nodes."); // now
	 * convert the FlatSpecies List to NodeEdgeList? List<FlatSpecies>
	 * flatSpeciesInMapping = flatNetworkMapping.getFlatSpeciesList(); for
	 * (FlatSpecies node1 : flatSpeciesInMapping) {
	 * node1.getAllRelatedSpecies().forEach((relationType, node2List) -> { for
	 * (FlatEdge node2Edge : node2List) { mapping.addListEntry(node1.getSymbol(),
	 * node1.getSimpleModelEntityUUID(),
	 * node2Edge.getOutputFlatSpecies().getSymbol(),
	 * node2Edge.getOutputFlatSpecies().getSimpleModelEntityUUID(),
	 * this.utilityService.translateSBOString(relationType)); } }); }
	 * mapping.setName(flatNetworkMapping.getEntityUUID()); return mapping; }
	 */

	@Override
	public MappingNode createMappingFromPathway(PathwayNode pathway, NetworkMappingType type, IDSystem idSystem,
			ProvenanceGraphActivityNode activityNode, ProvenanceGraphAgentNode agentNode) throws Exception {

		Instant startTime = Instant.now();
		logger.info("Started Creating Mapping from Pathway at " + startTime.toString());
		// need a Warehouse Node of Type Mapping, set NetworkMappingTypes
		String mappingName = "Map_" + type.name() + "_" + pathway.getPathwayIdString() + "_" + idSystem.name();

		MappingNode mappingFromPathway = this.warehouseGraphService.createMappingNode(pathway, type, mappingName);
		logger.info("Created MappingNode with uuid:" + mappingFromPathway.getEntityUUID());
		Set<String> relationTypes = new HashSet<>();
		Set<String> nodeTypes = new HashSet<>();
		Set<String> nodeSymbols = new HashSet<>();
		List<String> nodeSBOTerms = new ArrayList<>();

		// connect MappingNode to PathwayNode with wasDerivedFrom
		this.provenanceGraphService.connect(mappingFromPathway, pathway, ProvenanceGraphEdgeType.wasDerivedFrom);
		// connect MappingNode to Activity with wasGeneratedBy
		this.provenanceGraphService.connect(mappingFromPathway, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
		// attribute the new Mapping to the agent
		this.provenanceGraphService.connect(mappingFromPathway, agentNode, ProvenanceGraphEdgeType.wasAttributedTo);

		Map<String, FlatSpecies> sbmlSpeciesEntityUUIDToFlatSpeciesMap = new HashMap<>();
		Map<String, FlatSpecies> sbmlSimpleReactionToFlatSpeciesMap = new HashMap<>();

		List<FlatSpecies> allFlatSpecies = new ArrayList<>();
		List<FlatEdge> allFlatEdges = new ArrayList<>();

		Iterable<FlatSpecies> persistedFlatSpecies;

		// TODO divert here for different mapping types
		if (type.equals(NetworkMappingType.METABOLIC)) {

			// gather nodeTypes
			this.warehouseGraphService.getAllDistinctSpeciesSboTermsOfPathway(pathway.getEntityUUID())
					.forEach(sboTerm -> nodeSBOTerms.add(sboTerm));
			Iterable<MetabolicPathwayReturnType> metPathwayResult = this.graphBaseEntityRepository
					.getAllMetabolicPathwayReturnTypes(pathway.getEntityUUID(), nodeSBOTerms);

			FlatSpecies startFlatSpecies;
			FlatSpecies endFlatSpecies;
			FlatEdge metabolicFlatEdge;
			Iterator<MetabolicPathwayReturnType> metIt = metPathwayResult.iterator();
			MetabolicPathwayReturnType current;

			List<String> flatReactionLabel = new ArrayList<>();
			flatReactionLabel.add("FlatReaction");

			boolean startFlatSpeciesExists;
			boolean endFlatSpeciesExists;
			while (metIt.hasNext()) {
				startFlatSpeciesExists = false;
				endFlatSpeciesExists = false;
				current = metIt.next();
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getSpecies().getEntityUUID())) {
					startFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(current.getSpecies().getEntityUUID());
					startFlatSpeciesExists = true;
				} else {
					startFlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(startFlatSpecies);
					startFlatSpecies.setSymbol(current.getSpecies().getsBaseName());
					nodeSymbols.add(current.getSpecies().getsBaseName());
					startFlatSpecies.setSboTerm(current.getSpecies().getsBaseSboTerm());
					nodeTypes.add(current.getSpecies().getsBaseSboTerm());
					startFlatSpecies.setSimpleModelEntityUUID(current.getSpecies().getEntityUUID());
					sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(current.getSpecies().getEntityUUID(), startFlatSpecies);
				}
				if (sbmlSimpleReactionToFlatSpeciesMap.containsKey(current.getReaction().getEntityUUID())) {
					endFlatSpecies = sbmlSimpleReactionToFlatSpeciesMap.get(current.getReaction().getEntityUUID());
					endFlatSpeciesExists = true;
				} else {
					endFlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(endFlatSpecies,
							flatReactionLabel);
					endFlatSpecies.setSymbol(current.getReaction().getsBaseName());
					nodeSymbols.add(current.getReaction().getsBaseName());
					endFlatSpecies.setSboTerm(current.getReaction().getsBaseSboTerm());
					nodeTypes.add(current.getReaction().getsBaseSboTerm());
					endFlatSpecies.setSimpleModelEntityUUID(current.getReaction().getEntityUUID());
					sbmlSimpleReactionToFlatSpeciesMap.put(current.getReaction().getEntityUUID(), endFlatSpecies);
				}
				if (startFlatSpeciesExists && endFlatSpeciesExists) {
					// since both species already exist, they now get an additional relationship
					// within the metabolic network
					logger.info("SBMLSpecies with entityUUID: " + current.getSpecies().getEntityUUID()
							+ " and SBMLSimpleReaction with entityUUID: " + current.getReaction().getEntityUUID()
							+ " have more than one relationship in pathway with entityUUID: "
							+ pathway.getEntityUUID());
				}
				metabolicFlatEdge = this.flatEdgeService.createFlatEdge(current.getTypeOfRelation());
				this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(metabolicFlatEdge);
				metabolicFlatEdge.setSymbol(startFlatSpecies.getSymbol() + "-" + current.getTypeOfRelation() + "->"
						+ endFlatSpecies.getSymbol());
				metabolicFlatEdge.setInputFlatSpecies(startFlatSpecies);
				metabolicFlatEdge.setOutputFlatSpecies(endFlatSpecies);
				// TODO: The relationType needs to be determined in a better way.
				relationTypes.add(current.getTypeOfRelation().equals("IS_PRODUCT") ? "PRODUCTOF"
						: (current.getTypeOfRelation().equals("IS_REACTANT") ? "REACTANTOF"
								: (current.getTypeOfRelation().equals("IS_CATALYST") ? "CATALYSES" : "UNKNOWN")));
				allFlatEdges.add(metabolicFlatEdge);
			}
			// collect and persist the FlatSpecies
			for (String entry : sbmlSpeciesEntityUUIDToFlatSpeciesMap.keySet()) {
				allFlatSpecies.add(sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(entry));
			}
			for (String entry : sbmlSimpleReactionToFlatSpeciesMap.keySet()) {
				allFlatSpecies.add(sbmlSimpleReactionToFlatSpeciesMap.get(entry));
			}
			persistedFlatSpecies = this.persistListOfFlatSpecies(allFlatSpecies);

			// persist the FlatEdges
			Iterable<FlatEdge> persistedFlatEdges = flatEdgeRepository.saveAll(allFlatEdges);

		} else {
			Map<String, FlatEdge> sbmlSimpleTransitionToFlatEdgeMap = new HashMap<>();
			List<String> transitionSBOTerms = new ArrayList<>();

			if (type.equals(NetworkMappingType.PATHWAYMAPPING)) {
				// gather nodeTypes
				this.warehouseGraphService.getAllDistinctSpeciesSboTermsOfPathway(pathway.getEntityUUID())
						.forEach(sboTerm -> nodeSBOTerms.add(sboTerm));

				// gather transitionTypes
				this.warehouseGraphService.getAllDistinctTransitionSboTermsOfPathway(pathway.getEntityUUID())
						.forEach(sboTerm -> transitionSBOTerms.add(sboTerm));

				// flatTransitionsOfPathway =
				// this.graphBaseEntityRepository.getAllFlatTransitionsForPathway(pathway.getEntityUUID(),
				// idSystem);

			} else if (type.equals(NetworkMappingType.METABOLIC)) {
				// flatTransitionsOfPathway =
				// this.graphBaseEntityRepository.getFlatMetabolicReactionsForPathway(pathway.getEntityUUID(),
				// idSystem);

			} else if (type.equals(NetworkMappingType.PPI)) {
				nodeSBOTerms.add("SBO:0000252");
				String conversionSBO = SBO.getTerm(182).getId();
				transitionSBOTerms.add(conversionSBO);
				for (String conversionChildSBO : this.utilityService.getAllSBOChildren(conversionSBO)) {
					transitionSBOTerms.add(conversionChildSBO);
				}
				String biochemicalOrTransportReactionSBO = SBO.getTerm(167).getId();
				transitionSBOTerms.add(biochemicalOrTransportReactionSBO);
				for (String biochemicalOrTransportReactionChildSBO : this.utilityService
						.getAllSBOChildren(biochemicalOrTransportReactionSBO)) {
					transitionSBOTerms.add(biochemicalOrTransportReactionChildSBO);
				}
				String molecularInteractionSBO = SBO.getTerm(344).getId();
				transitionSBOTerms.add(molecularInteractionSBO);
				for (String molecularInteractionChildSBO : this.utilityService
						.getAllSBOChildren(molecularInteractionSBO)) {
					transitionSBOTerms.add(molecularInteractionChildSBO);
				}

				// flatTransitionsOfPathway =
				// this.graphBaseEntityRepository.getFlatTransitionsForPathway(pathway.getEntityUUID(),
				// idSystem, transitionSBOTerms);

			} else if (type.equals(NetworkMappingType.REGULATORY)) {
				nodeSBOTerms.add("SBO:0000252");
				nodeSBOTerms.add("SBO:0000247");
				String controlSBO = "SBO:0000168";
				transitionSBOTerms.add(controlSBO);
				for (String controlChildSBO : this.utilityService.getAllSBOChildren(controlSBO)) {
					transitionSBOTerms.add(controlChildSBO);
				}
				// flatTransitionsOfPathway =
				// this.graphBaseEntityRepository.getFlatTransitionsForPathway(pathway.getEntityUUID(),
				// idSystem, transitionSBOTerms, nodeSBOTerms);

			} else if (type.equals(NetworkMappingType.SIGNALLING)) {
				nodeSBOTerms.add("SBO:0000252");
				transitionSBOTerms.add("SBO:0000656");
				transitionSBOTerms.add("SBO:0000170");
				transitionSBOTerms.add("SBO:0000169");
				// flatTransitionsOfPathway =
				// this.graphBaseEntityRepository.getFlatTransitionsForPathway(pathway.getEntityUUID(),
				// idSystem, transitionSBOTerms, nodeSBOTerms);
			}

			Iterable<NonMetabolicPathwayReturnType> nonMetPathwayResult = this.graphBaseEntityRepository
					.getFlatTransitionsForPathwayUsingSpecies(pathway.getEntityUUID(), idSystem, transitionSBOTerms,
							nodeSBOTerms);

			Iterator<NonMetabolicPathwayReturnType> it = nonMetPathwayResult.iterator();
			NonMetabolicPathwayReturnType current;
			FlatSpecies inputFlatSpecies;
			FlatSpecies outputFlatSpecies;
			FlatEdge transitionFlatEdge;
			while (it.hasNext()) {
				current = it.next();
				boolean inputExists = false;
				boolean outputExists = false;
				boolean transitionExists = false;
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getInputSpecies().getEntityUUID())) {
					inputFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap
							.get(current.getInputSpecies().getEntityUUID());
					inputExists = true;
				} else {
					inputFlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(inputFlatSpecies);
					inputFlatSpecies.setSymbol(current.getInputSpecies().getsBaseName());
					nodeSymbols.add(current.getInputSpecies().getsBaseName());
					inputFlatSpecies.setSboTerm(current.getInputSpecies().getsBaseSboTerm());
					nodeTypes.add(current.getInputSpecies().getsBaseSboTerm());
					inputFlatSpecies.setSimpleModelEntityUUID(current.getInputSpecies().getEntityUUID());
					sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(current.getInputSpecies().getEntityUUID(),
							inputFlatSpecies);
				}
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getOutputSpecies().getEntityUUID())) {
					outputFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap
							.get(current.getOutputSpecies().getEntityUUID());
					outputExists = true;
				} else {
					outputFlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(outputFlatSpecies);
					outputFlatSpecies.setSymbol(current.getOutputSpecies().getsBaseName());
					nodeSymbols.add(current.getOutputSpecies().getsBaseName());
					outputFlatSpecies.setSboTerm(current.getOutputSpecies().getsBaseSboTerm());
					nodeTypes.add(current.getOutputSpecies().getsBaseSboTerm());
					outputFlatSpecies.setSimpleModelEntityUUID(current.getOutputSpecies().getEntityUUID());
					sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(current.getOutputSpecies().getEntityUUID(),
							outputFlatSpecies);
				}
				if (sbmlSimpleTransitionToFlatEdgeMap.containsKey(current.getTransition().getEntityUUID())) {
					transitionFlatEdge = sbmlSimpleTransitionToFlatEdgeMap.get(current.getTransition().getEntityUUID());
					logger.info("Reusing Transition " + current.getTransition().getEntityUUID());
					transitionExists = true;
					if (!(transitionFlatEdge.getInputFlatSpecies().equals(inputFlatSpecies)
							&& transitionFlatEdge.getOutputFlatSpecies().equals(outputFlatSpecies))) {
						// this should be identical, if the transition really is the same
						logger.info("Transition does not have the same input and output.. Aborting");
						throw new Exception("Reusing Transition " + current.getTransition().getEntityUUID()
								+ ", but input and output are not the same");
					} else {
						// already have this transition, continue
						continue;
					}
				} else {
					transitionFlatEdge = this.flatEdgeService.createFlatEdge(current.getTransition().getsBaseSboTerm());
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(transitionFlatEdge);
					transitionFlatEdge.setInputFlatSpecies(inputFlatSpecies);
					transitionFlatEdge.setOutputFlatSpecies(outputFlatSpecies);
					transitionFlatEdge.addAnnotation("SBOTerm", current.getTransition().getsBaseSboTerm());

					transitionFlatEdge.addAnnotationType("SBOTerm", "String");
					transitionFlatEdge.addAnnotation("TransitionId", current.getTransition().getTransitionId());
					transitionFlatEdge.addAnnotationType("TransitionId", "String");
					transitionFlatEdge.setSymbol(inputFlatSpecies.getSymbol() + "-" + transitionFlatEdge.getTypeString()
							+ "->" + outputFlatSpecies.getSymbol());
					relationTypes.add(transitionFlatEdge.getTypeString());
					transitionFlatEdge.addAnnotation("sbmlSimpleTransitionEntityUUID",
							current.getTransition().getEntityUUID());
					transitionFlatEdge.addAnnotationType("sbmlSimpleTransitionEntityUUID", "String");
					sbmlSimpleTransitionToFlatEdgeMap.put(current.getTransition().getEntityUUID(), transitionFlatEdge);
				}
			}

			for (String entry : sbmlSpeciesEntityUUIDToFlatSpeciesMap.keySet()) {
				allFlatSpecies.add(sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(entry));
			}
			persistedFlatSpecies = this.persistListOfFlatSpecies(allFlatSpecies);

			for (String entry : sbmlSimpleTransitionToFlatEdgeMap.keySet()) {
				allFlatEdges.add(sbmlSimpleTransitionToFlatEdgeMap.get(entry));
			}
			Iterable<FlatEdge> persistedFlatEdges = this.flatEdgeRepository.saveAll(allFlatEdges);

		}

		Instant endTime = Instant.now();
		mappingFromPathway.addWarehouseAnnotation("creationstarttime", startTime.toString());
		mappingFromPathway.addWarehouseAnnotation("creationendtime", endTime.toString());
		mappingFromPathway.addWarehouseAnnotation("numberofnodes", String.valueOf(allFlatSpecies.size()));
		mappingFromPathway.addWarehouseAnnotation("numberofrelations", String.valueOf(allFlatEdges.size()));
		mappingFromPathway.setMappingNodeTypes(nodeTypes);
		mappingFromPathway.setMappingRelationTypes(relationTypes);
		mappingFromPathway.setMappingNodeSymbols(nodeSymbols);

		MappingNode persistedMappingOfPathway = (MappingNode) this.warehouseGraphService
				.saveWarehouseGraphNodeEntity(mappingFromPathway, 0);
		logger.info(Instant.now().toString() + ": Persisted FlatSpecies. Starting to connect");
		int fsConnectCounter = 1;

		for (FlatSpecies fs : persistedFlatSpecies) {
			logger.info(Instant.now().toString() + ": Building ConnectionSet #" + fsConnectCounter++);
			this.provenanceGraphService.connect(fs,
					this.provenanceEntityRepository.findByEntityUUID(fs.getSimpleModelEntityUUID()),
					ProvenanceGraphEdgeType.wasDerivedFrom);
			this.provenanceGraphService.connect(fs, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			this.warehouseGraphService.connect(persistedMappingOfPathway, fs, WarehouseGraphEdgeType.CONTAINS);
		}
		return persistedMappingOfPathway;
	}

	/**
	 * Convert a NodeEdgeList to a returnable Resource Object
	 * 
	 * @param nodeEdgeList The nodeEdgeList to convert
	 * @return a sifResource as ByteArrayResource
	 */
	@Override
	@Deprecated
	public Resource getResourceFromNodeEdgeList(NodeEdgeList nodeEdgeList, String type) {
		switch (type) {
		case "sif":

			SifFile sifFile = this.fileService.getSifFromNodeEdgeList(nodeEdgeList);

			// Resource resource = fileStorageService.loadFileAsResource(fileName);
			if (sifFile != null) {
				Resource resource = this.fileStorageService.getSifAsResource(sifFile);
				return resource;
			} else {
				return null;
			}
		case "graphml":

			ByteArrayOutputStream bo = this.graphMLService.getGraphMLByteArrayOutputStream(nodeEdgeList,
					new ByteArrayOutputStream());

			// String graphMLString = graphMLService.getGraphMLString(nodeEdgeList);

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
