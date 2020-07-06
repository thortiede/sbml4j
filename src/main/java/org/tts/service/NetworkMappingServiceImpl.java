package org.tts.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.SBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.GraphEnum.IDSystem;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.repository.common.BiomodelsQualifierRepository;
import org.tts.repository.common.GraphBaseEntityRepository;
import org.tts.repository.flat.FlatEdgeRepository;
import org.tts.repository.flat.FlatSpeciesRepository;
import org.tts.repository.provenance.ProvenanceEntityRepository;
import org.tts.repository.warehouse.PathwayNodeRepository;

@Service
public class NetworkMappingServiceImpl implements NetworkMappingService {

	@Autowired
	GraphBaseEntityRepository graphBaseEntityRepository;
	
	@Autowired
	FlatSpeciesRepository flatSpeciesRepository;

	@Autowired
	ProvenanceEntityRepository provenanceEntityRepository;

	@Autowired
	FlatEdgeRepository flatEdgeRepository;

	@Autowired
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityServiceImpl;

	@Autowired
	WarehouseGraphService warehouseGraphService;

	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	UtilityService utilityService;

	@Autowired
	FlatEdgeService flatEdgeService;

	@Autowired
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	
	@Autowired
	PathwayNodeRepository pathwayNodeRepository;
	
	private static int QUERY_DEPTH_ZERO = 0;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

		// divert here for different mapping types
		if (type.equals(NetworkMappingType.METABOLIC) || type.equals(NetworkMappingType.PATHWAYMAPPING)) {

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

			String flatReactionLabel = "FlatReaction";
			
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
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(endFlatSpecies);
					endFlatSpecies.addLabel(flatReactionLabel);
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
			
			// do this at the end
			// collect and persist the FlatSpecies
			//for (String entry : sbmlSpeciesEntityUUIDToFlatSpeciesMap.keySet()) {
			//	allFlatSpecies.add(sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(entry));
			//}
			for (String entry : sbmlSimpleReactionToFlatSpeciesMap.keySet()) {
				allFlatSpecies.add(sbmlSimpleReactionToFlatSpeciesMap.get(entry));
			}
			// do not persist just yet
			//persistedFlatSpecies = this.persistListOfFlatSpecies(allFlatSpecies);

			// persist the FlatEdges - not just yet
			//Iterable<FlatEdge> persistedFlatEdges = flatEdgeRepository.saveAll(allFlatEdges);

		} //else {
		
		Map<String, FlatEdge> sbmlSimpleTransitionToFlatEdgeMap = new HashMap<>();
		List<String> transitionSBOTerms = new ArrayList<>();
		boolean processTransitionsNeeded = false;
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
			processTransitionsNeeded = true;
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
			processTransitionsNeeded = true;
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
			processTransitionsNeeded = true;
		} else if (type.equals(NetworkMappingType.SIGNALLING)) {
			nodeSBOTerms.add("SBO:0000252");
			transitionSBOTerms.add("SBO:0000656");
			transitionSBOTerms.add("SBO:0000170");
			transitionSBOTerms.add("SBO:0000169");
			// flatTransitionsOfPathway =
			// this.graphBaseEntityRepository.getFlatTransitionsForPathway(pathway.getEntityUUID(),
			// idSystem, transitionSBOTerms, nodeSBOTerms);
			processTransitionsNeeded = true;
		}
		// do we have non metabolic elements we need to process?
		if (processTransitionsNeeded) {
			Iterable<NonMetabolicPathwayReturnType> nonMetPathwayResult = this.graphBaseEntityRepository
					.getFlatTransitionsForPathwayUsingSpecies(pathway.getEntityUUID(), transitionSBOTerms,
							nodeSBOTerms);

			Iterator<NonMetabolicPathwayReturnType> it = nonMetPathwayResult.iterator();
			NonMetabolicPathwayReturnType current;
			FlatSpecies inputFlatSpecies;
			FlatSpecies outputFlatSpecies;
			FlatEdge transitionFlatEdge;
			while (it.hasNext()) {
				current = it.next();
				
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getInputSpecies().getEntityUUID())) {
					inputFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap
							.get(current.getInputSpecies().getEntityUUID());
				} else {
					inputFlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(inputFlatSpecies);
					inputFlatSpecies.setSymbol(current.getInputSpecies().getsBaseName());
					nodeSymbols.add(current.getInputSpecies().getsBaseName());
					inputFlatSpecies.setSboTerm(current.getInputSpecies().getsBaseSboTerm());
					nodeTypes.add(current.getInputSpecies().getsBaseSboTerm());
					inputFlatSpecies.setSimpleModelEntityUUID(current.getInputSpecies().getEntityUUID());
					getBQAnnotations(current.getInputSpecies().getEntityUUID(), inputFlatSpecies);
					getPathwayAnnotations(current.getInputSpecies().getEntityUUID(), inputFlatSpecies);
					sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(current.getInputSpecies().getEntityUUID(),
							inputFlatSpecies);
				}
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getOutputSpecies().getEntityUUID())) {
					outputFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap
							.get(current.getOutputSpecies().getEntityUUID());
				} else {
					outputFlatSpecies = new FlatSpecies();
					this.sbmlSimpleModelUtilityServiceImpl.setGraphBaseEntityProperties(outputFlatSpecies);
					outputFlatSpecies.setSymbol(current.getOutputSpecies().getsBaseName());
					nodeSymbols.add(current.getOutputSpecies().getsBaseName());
					outputFlatSpecies.setSboTerm(current.getOutputSpecies().getsBaseSboTerm());
					nodeTypes.add(current.getOutputSpecies().getsBaseSboTerm());
					outputFlatSpecies.setSimpleModelEntityUUID(current.getOutputSpecies().getEntityUUID());
					getBQAnnotations(current.getOutputSpecies().getEntityUUID(), outputFlatSpecies);
					getPathwayAnnotations(current.getOutputSpecies().getEntityUUID(), outputFlatSpecies);
					sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(current.getOutputSpecies().getEntityUUID(),
							outputFlatSpecies);
				}
				if (sbmlSimpleTransitionToFlatEdgeMap.containsKey(current.getTransition().getEntityUUID())) {
					transitionFlatEdge = sbmlSimpleTransitionToFlatEdgeMap.get(current.getTransition().getEntityUUID());
					logger.info("Reusing Transition " + current.getTransition().getEntityUUID());
					
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
		}
		for (String entry : sbmlSpeciesEntityUUIDToFlatSpeciesMap.keySet()) {
			allFlatSpecies.add(sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(entry));
		}
		// TODO: Ensure that the save - depth here is correct with 0, it was 1, but that was with old FlatSpecies
		persistedFlatSpecies = this.flatSpeciesRepository.save(allFlatSpecies, QUERY_DEPTH_ZERO);

		for (String entry : sbmlSimpleTransitionToFlatEdgeMap.keySet()) {
			allFlatEdges.add(sbmlSimpleTransitionToFlatEdgeMap.get(entry));
		}
		Iterable<FlatEdge> persistedFlatEdges = this.flatEdgeRepository.saveAll(allFlatEdges);

		Instant endTime = Instant.now();
		mappingFromPathway.addWarehouseAnnotation("creationstarttime", startTime.toString());
		mappingFromPathway.addWarehouseAnnotation("creationendtime", endTime.toString());
		mappingFromPathway.addWarehouseAnnotation("numberofnodes", String.valueOf(allFlatSpecies.size()));
		mappingFromPathway.addWarehouseAnnotation("numberofrelations", String.valueOf(allFlatEdges.size()));
		mappingFromPathway.setMappingNodeTypes(nodeTypes);
		mappingFromPathway.setMappingRelationTypes(relationTypes);
		mappingFromPathway.setMappingNodeSymbols(nodeSymbols);

		MappingNode persistedMappingOfPathway = (MappingNode) this.warehouseGraphService
				.saveWarehouseGraphNodeEntity(mappingFromPathway, QUERY_DEPTH_ZERO);
		logger.info(Instant.now().toString() + ": Persisted FlatSpecies. Starting to connect");
		int fsConnectCounter = 1;

		for (FlatSpecies fs : persistedFlatSpecies) {
			logger.info(Instant.now().toString() + ": Building ConnectionSet #" + fsConnectCounter++);
			this.provenanceGraphService.connect(fs,
					this.provenanceEntityRepository.findByEntityUUID(fs.getSimpleModelEntityUUID()),
					ProvenanceGraphEdgeType.wasDerivedFrom);
			this.warehouseGraphService.connect(fs, this.provenanceEntityRepository.findByEntityUUID(fs.getSimpleModelEntityUUID()), WarehouseGraphEdgeType.DERIVEDFROM);
			this.provenanceGraphService.connect(fs, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			this.warehouseGraphService.connect(persistedMappingOfPathway, fs, WarehouseGraphEdgeType.CONTAINS);
		}
		return persistedMappingOfPathway;
	}

	/**
	 * Query the biomodelsQualifierRepository for all Biomodels Qualifiers of Type HAS_VERSION and IS
	 * @param parentEntityUUID The Node to which the Biomodels Qualifier should be fetched for
	 * @param target The Flat Species those Qualifiers should be added as Annotations (always as type "string"
	 */
	private void getBQAnnotations(String parentEntityUUID, FlatSpecies target) {
		List<BiomodelsQualifier> bqList = this.biomodelsQualifierRepository.findAllForSBMLSpecies(parentEntityUUID);
		for(BiomodelsQualifier bq : bqList) {
			System.out.println(bq.getEndNode().getUri());
			if (bq.getQualifier().equals(Qualifier.BQB_HAS_VERSION) || bq.getQualifier().equals(Qualifier.BQB_IS)) {
				String[] splitted = bq.getEndNode().getUri().split("/");
				target.addAnnotation(splitted[splitted.length-2].replace('.', '_'), splitted[splitted.length-1]);
				target.addAnnotationType(splitted[splitted.length-2].replace('.', '_'), "string");
			}
		}
	}

	private void getPathwayAnnotations(String parentEntityUUID, FlatSpecies target) {
		List<String> pathwayUUIDs = this.pathwayNodeRepository.getPathwayNodeUUIDsOfSBase(parentEntityUUID);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String pathwayUUID : pathwayUUIDs) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(pathwayUUID);
			if (first) {
				first = false;
			}
			
		}
		target.addAnnotation("pathways", sb.toString());
		target.addAnnotationType("pathways", "string");
	}

}
