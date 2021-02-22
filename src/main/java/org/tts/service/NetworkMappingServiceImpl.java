/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.SBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.Exception.NetworkAlreadyExistsException;
import org.tts.config.SBML4jConfig;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.common.GraphEnum.AnnotationName;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.NameNode;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.simple.SBMLSimpleTransition;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.repository.common.BiomodelsQualifierRepository;
import org.tts.service.SimpleSBML.SBMLQualSpeciesService;
import org.tts.service.SimpleSBML.SBMLSimpleTransitionService;
import org.tts.service.SimpleSBML.SBMLSpeciesService;
import org.tts.service.warehouse.MappingNodeService;

/**
 * Service for creating <a href="#{@link}">{@link MappingNode}</a>s from <a href="#{@link}">{@link PathwayNodes}</a>
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class NetworkMappingServiceImpl implements NetworkMappingService {
	
	@Autowired
	FlatEdgeService flatEdgeService;
	
	@Autowired
	FlatSpeciesService flatSpeciesService;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NameNodeService nameNodeService;
	
	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	SBMLSpeciesService sbmlSpeciesService;
	
	@Autowired
	SBMLQualSpeciesService sbmlQualSpeciesService;
	
	@Autowired
	SBMLSimpleTransitionService sbmlSimpleTransitionService;

	@Autowired
	UtilityService utilityService;

	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	BiomodelsQualifierRepository biomodelsQualifierRepository;
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	private static int QUERY_DEPTH_ZERO = 0;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Create a <a href="#{@link}">{@link MappingNpde}</a> from a <a href="#{@link}">{@link PathwayNode}</a> by mapping entities
	 * @param pathway The <a href="#{@link}">{@link PathwayNode}</a> to be mapped
	 * @param type The <a href="#{@link}">{@link NetworkMappingType}</a> that should be applied in the mappingProcess
	 * @param activityNode The <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> this mapping is generated from
	 * @param agentNode The The <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a> that this mapping was attributed to
	 * @param newMappingName A string representing the name of the Mapping to be created (Needs to be checked for uniqueness before passing it in this method)
	 * @return The created <a href="#{@link}">{@link MappingNpde}</a> that contains the new Mapping
	 */
	@Override
	public MappingNode createMappingFromPathway(PathwayNode pathway, NetworkMappingType type,
			ProvenanceGraphActivityNode activityNode, ProvenanceGraphAgentNode agentNode, String newMappingName) throws NetworkAlreadyExistsException, Exception {

		Instant startTime = Instant.now();
		logger.info("Started Creating Mapping from Pathway at " + startTime.toString());
		// need a Warehouse Node of Type Mapping, set NetworkMappingTypes
		
		MappingNode mappingFromPathway = this.mappingNodeService.createMappingNode(pathway, type, newMappingName);
		logger.info("Created MappingNode with uuid:" + mappingFromPathway.getEntityUUID());
		Set<String> relationTypes = new HashSet<>();
		Set<String> nodeTypes = new HashSet<>();
		Set<String> flatSpeciesSymbols = new HashSet<>();
		Set<String> relationSymbols = new HashSet<>();
		List<String> nodeSBOTerms = new ArrayList<>();
		int numberOfReactions = 0;

		// connect MappingNode to PathwayNode with wasDerivedFrom
		this.provenanceGraphService.connect(mappingFromPathway, pathway, ProvenanceGraphEdgeType.wasDerivedFrom);
		// connect MappingNode to Activity with wasGeneratedBy
		this.provenanceGraphService.connect(mappingFromPathway, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
		// attribute the new Mapping to the agent
		this.provenanceGraphService.connect(mappingFromPathway, agentNode, ProvenanceGraphEdgeType.wasAttributedTo);

		Map<String, FlatSpecies> sbmlSBaseEntityUUIDToFlatSpeciesMap = new HashMap<>();
		Map<String, FlatSpecies> sbmlSimpleReactionToFlatSpeciesMap = new HashMap<>();

		List<FlatSpecies> allFlatSpecies = new ArrayList<>();
		List<FlatEdge> allFlatEdges = new ArrayList<>();
		
		Iterable<FlatSpecies> persistedFlatSpecies;
		
		// divert here for different mapping types
		if (type.equals(NetworkMappingType.METABOLIC) || type.equals(NetworkMappingType.PATHWAYMAPPING)) {

			// gather nodeTypes
			this.pathwayService.getAllDistinctSpeciesSboTermsOfPathway(pathway.getEntityUUID())
					.forEach(sboTerm -> nodeSBOTerms.add(sboTerm));
			Iterable<MetabolicPathwayReturnType> metPathwayResult = this.pathwayService
					.getAllMetabolicPathwayReturnTypes(UUID.fromString(pathway.getEntityUUID()), nodeSBOTerms);

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
				if (sbmlSBaseEntityUUIDToFlatSpeciesMap.containsKey(current.getSpecies().getEntityUUID())) {
					startFlatSpecies = sbmlSBaseEntityUUIDToFlatSpeciesMap.get(current.getSpecies().getEntityUUID());
					startFlatSpeciesExists = true;
				} else {
					startFlatSpecies = new FlatSpecies();
					this.graphBaseEntityService.setGraphBaseEntityProperties(startFlatSpecies);
					startFlatSpecies.setSymbol(current.getSpecies().getsBaseName());
					startFlatSpecies.setSboTerm(current.getSpecies().getsBaseSboTerm());
					nodeTypes.add(current.getSpecies().getsBaseSboTerm());
					startFlatSpecies.setSimpleModelEntityUUID(current.getSpecies().getEntityUUID());
					sbmlSBaseEntityUUIDToFlatSpeciesMap.put(current.getSpecies().getEntityUUID(), startFlatSpecies);
					if (!flatSpeciesSymbols.add(startFlatSpecies.getSymbol())) {
						logger.warn("Duplicate Symbol " + startFlatSpecies.getSymbol() + " in Mapping. Offending FlatSpecies has entityUUID: " + startFlatSpecies.getEntityUUID());
					}
				}
				if (sbmlSimpleReactionToFlatSpeciesMap.containsKey(current.getReaction().getEntityUUID())) {
					endFlatSpecies = sbmlSimpleReactionToFlatSpeciesMap.get(current.getReaction().getEntityUUID());
					endFlatSpeciesExists = true;
				} else {
					endFlatSpecies = new FlatSpecies();
					this.graphBaseEntityService.setGraphBaseEntityProperties(endFlatSpecies);
					endFlatSpecies.addLabel(flatReactionLabel);
					endFlatSpecies.setSymbol(current.getReaction().getsBaseName());
					endFlatSpecies.setSboTerm(current.getReaction().getsBaseSboTerm());
					nodeTypes.add(current.getReaction().getsBaseSboTerm());
					endFlatSpecies.setSimpleModelEntityUUID(current.getReaction().getEntityUUID());
					sbmlSimpleReactionToFlatSpeciesMap.put(current.getReaction().getEntityUUID(), endFlatSpecies);
					numberOfReactions++;
					if (!flatSpeciesSymbols.add(endFlatSpecies.getSymbol())) {
						logger.warn("Duplicate Symbol " + endFlatSpecies.getSymbol() + " in Mapping. Offending FlatSpecies has entityUUID: " + endFlatSpecies.getEntityUUID());
					}
				}
				if (startFlatSpeciesExists && endFlatSpeciesExists) {
					// since both species already exist, they now get an additional relationship
					// within the metabolic network
					logger.debug("SBMLSpecies with entityUUID: " + current.getSpecies().getEntityUUID()
							+ " and SBMLSimpleReaction with entityUUID: " + current.getReaction().getEntityUUID()
							+ " have more than one relationship in pathway with entityUUID: "
							+ pathway.getEntityUUID());
				}
				metabolicFlatEdge = this.flatEdgeService.createFlatEdge(current.getTypeOfRelation());
				this.graphBaseEntityService.setGraphBaseEntityProperties(metabolicFlatEdge);
				String relationSymbol = startFlatSpecies.getSymbol() + "-" + current.getTypeOfRelation() + "->"
						+ endFlatSpecies.getSymbol();
				metabolicFlatEdge.setSymbol(relationSymbol);
				metabolicFlatEdge.setInputFlatSpecies(startFlatSpecies);
				metabolicFlatEdge.setOutputFlatSpecies(endFlatSpecies);
				// TODO: The relationType needs to be determined in a better way.
				relationTypes.add(current.getTypeOfRelation().equals("IS_PRODUCT") ? "PRODUCTOF"
						: (current.getTypeOfRelation().equals("IS_REACTANT") ? "REACTANTOF"
								: (current.getTypeOfRelation().equals("IS_CATALYST") ? "CATALYSES" : "UNKNOWN")));
				allFlatEdges.add(metabolicFlatEdge);
				relationSymbols.add(relationSymbol);
			}
			
			for (String entry : sbmlSimpleReactionToFlatSpeciesMap.keySet()) {
				allFlatSpecies.add(sbmlSimpleReactionToFlatSpeciesMap.get(entry));
			}
		}
		
		
		List<String> transitionSBOTerms = new ArrayList<>();
		boolean processTransitionsNeeded = false;
		if (type.equals(NetworkMappingType.PATHWAYMAPPING)) {
			// gather nodeTypes
			this.pathwayService.getAllDistinctSpeciesSboTermsOfPathway(pathway.getEntityUUID())
					.forEach(sboTerm -> nodeSBOTerms.add(sboTerm));

			// gather transitionTypes
			this.pathwayService.getAllDistinctTransitionSboTermsOfPathway(pathway.getEntityUUID())
					.forEach(sboTerm -> transitionSBOTerms.add(sboTerm));
			processTransitionsNeeded = true;
			
		} else if (type.equals(NetworkMappingType.PPI)) {
			nodeSBOTerms.add("SBO:0000252");
			nodeSBOTerms.add("SBO:0000253");
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
			processTransitionsNeeded = true;
			
		} else if (type.equals(NetworkMappingType.REGULATORY)) {
			nodeSBOTerms.add("SBO:0000252");
			nodeSBOTerms.add("SBO:0000253");
			nodeSBOTerms.add("SBO:0000247");
			String controlSBO = "SBO:0000168";
			transitionSBOTerms.add(controlSBO);
			for (String controlChildSBO : this.utilityService.getAllSBOChildren(controlSBO)) {
				transitionSBOTerms.add(controlChildSBO);
			}
			processTransitionsNeeded = true;
			
		} else if (type.equals(NetworkMappingType.SIGNALLING)) {
			nodeSBOTerms.add("SBO:0000252");
			nodeSBOTerms.add("SBO:0000253");
			transitionSBOTerms.add("SBO:0000656");
			transitionSBOTerms.add("SBO:0000170");
			transitionSBOTerms.add("SBO:0000169");
			processTransitionsNeeded = true;
			
		}
	
		if (processTransitionsNeeded) {
			Iterable<SBMLSimpleTransition> pathwayTransitions = 
					this.sbmlSimpleTransitionService.findMatchingTransitionsInPathway(
															pathway.getEntityUUID(),
															transitionSBOTerms, 
															nodeSBOTerms);
			
			Iterator<SBMLSimpleTransition> it = pathwayTransitions.iterator();
			SBMLSimpleTransition currentTransition;

			Set<String> groupNodesAlreadyFlattened = new HashSet<>();
			Map<String, List<FlatSpecies>> groupNodeUUIDToGroupFlatSpecies = new HashMap<>();
			while (it.hasNext()) {
				FlatSpecies inputFlatSpecies = null;
				FlatSpecies outputFlatSpecies = null;
				
				List<FlatSpecies> inputGroupFlatSpecies = null;
				List<FlatSpecies> outputGroupFlatSpecies = null;
				currentTransition = it.next();
				
				// input species
				String inputSpeciesUUID = currentTransition.getInputSpecies().getEntityUUID();
				if (sbmlSBaseEntityUUIDToFlatSpeciesMap.containsKey(inputSpeciesUUID)) {
					inputFlatSpecies = sbmlSBaseEntityUUIDToFlatSpeciesMap
							.get(inputSpeciesUUID);
				} else {
					if (currentTransition.getInputSpecies().getsBaseSboTerm().equals("SBO:0000253")) {
						// This is a group node, we need to treat that
						// First, did we already process this group node?
						if (!groupNodesAlreadyFlattened.add(inputSpeciesUUID)) {
							inputGroupFlatSpecies = groupNodeUUIDToGroupFlatSpecies.get(inputSpeciesUUID);
						} else {
							// the member species should have transitions together with the sbo 
							inputGroupFlatSpecies = new ArrayList<>();
							boolean areAllProteins = true;
							for(SBMLQualSpecies groupSpecies : this.sbmlQualSpeciesService.getSBMLQualSpeciesOfGroup(inputSpeciesUUID)) {
								if(areAllProteins) {
									// if up to this point all have been proteins (so sbo term has always been 252, then check this one
									// if one has not been 252 (so a compound or such), it will have been set to false, and must stay this way).
									areAllProteins = groupSpecies.getsBaseSboTerm().equals("SBO:0000252");
								}
								if (sbmlSBaseEntityUUIDToFlatSpeciesMap.containsKey(inputSpeciesUUID)) {
									inputGroupFlatSpecies.add(sbmlSBaseEntityUUIDToFlatSpeciesMap.get(groupSpecies.getEntityUUID()));
								} else {
									// haven't seen this Species before
									FlatSpecies newGroupFlatSpecies = createFlatSpeciesFromSBMLSBaseEntity(groupSpecies);
									if (!flatSpeciesSymbols.add(newGroupFlatSpecies.getSymbol())) {
										logger.warn("Duplicate Symbol " + newGroupFlatSpecies.getSymbol() + " in Mapping. Offending FlatSpecies has entityUUID: " + newGroupFlatSpecies.getEntityUUID());
									}
									sbmlSBaseEntityUUIDToFlatSpeciesMap.put(groupSpecies.getEntityUUID(),
											newGroupFlatSpecies);
									nodeTypes.add(groupSpecies.getsBaseSboTerm());
									// finally add this new species to the group specific list for connection
									inputGroupFlatSpecies.add(newGroupFlatSpecies);
								}
							}
							// add these flatSpecies to map for later reference when we see this group again
							groupNodeUUIDToGroupFlatSpecies.put(inputSpeciesUUID, inputGroupFlatSpecies);
							
							// now groupFlatSpecies contains all FlatSpecies that belong to this group.
							// connect them with flatEdges
							//	  a) if all participants are proteins: SBO:0000526 protein complex formation
							//	  b) otherwise SBO:0000344 molecular interaction
							for (int i = 0; i != inputGroupFlatSpecies.size(); i++) {
								for (int j = 0; j != inputGroupFlatSpecies.size(); j++) {
									if (i != j) {
										// add two flat edges (i -> j and j -> i)
										FlatEdge transitionFlatEdge = this.flatEdgeService.createFlatEdge(areAllProteins ? "SBO:0000526" : "SBO:0000344");
										this.graphBaseEntityService.setGraphBaseEntityProperties(transitionFlatEdge);
										transitionFlatEdge.setInputFlatSpecies(inputGroupFlatSpecies.get(i));
										transitionFlatEdge.setOutputFlatSpecies(inputGroupFlatSpecies.get(j));
										transitionFlatEdge.addAnnotation("SBOTerm", areAllProteins ? "SBO:0000526" : "SBO:0000344");
		
										transitionFlatEdge.addAnnotationType("SBOTerm", "String");
										String symbolAndTransitionIdString = inputGroupFlatSpecies.get(i).getSymbol() + "-" 
												+ (areAllProteins ? "proteincomplexformation" : "molecularinteraction") + "->" + inputGroupFlatSpecies.get(j).getSymbol();
										transitionFlatEdge.addAnnotation("TransitionId", symbolAndTransitionIdString);
										transitionFlatEdge.addAnnotationType("TransitionId", "String");
										transitionFlatEdge.setSymbol(symbolAndTransitionIdString);
										relationTypes.add(transitionFlatEdge.getTypeString());
										transitionFlatEdge.addAnnotation("sbmlSimpleModelEntityUUID",
												inputSpeciesUUID);
										transitionFlatEdge.addAnnotationType("sbmlSimpleModelEntityUUID", "String");
										//sbmlSimpleTransitionToFlatEdgeMap.put(currentTransition.getInputSpecies().getEntityUUID(), transitionFlatEdge);
										allFlatEdges.add(transitionFlatEdge);
										relationSymbols.add(symbolAndTransitionIdString);
									}
								}
							}
						}
					} else {
						inputFlatSpecies = this.createFlatSpeciesFromSBMLSBaseEntity(currentTransition.getInputSpecies());
						if (!flatSpeciesSymbols.add(inputFlatSpecies.getSymbol())) {
							logger.warn("Duplicate Symbol " + inputFlatSpecies.getSymbol() + " in Mapping. Offending FlatSpecies has entityUUID: " + inputFlatSpecies.getEntityUUID());
						}
						nodeTypes.add(currentTransition.getInputSpecies().getsBaseSboTerm());
						sbmlSBaseEntityUUIDToFlatSpeciesMap.put(inputSpeciesUUID,
								inputFlatSpecies);
					}
				}
				// output species
				String outputSpeciesUUID = currentTransition.getOutputSpecies().getEntityUUID();
				if (sbmlSBaseEntityUUIDToFlatSpeciesMap.containsKey(outputSpeciesUUID)) {
					outputFlatSpecies = sbmlSBaseEntityUUIDToFlatSpeciesMap
							.get(outputSpeciesUUID);
				} else {
					if (currentTransition.getOutputSpecies().getsBaseSboTerm().equals("SBO:0000253")) {
						// This is a group node, we need to treat that// First, did we already process this group node?
						if (!groupNodesAlreadyFlattened.add(outputSpeciesUUID)) {
							outputGroupFlatSpecies = groupNodeUUIDToGroupFlatSpecies.get(outputSpeciesUUID);
						} else {
							// the member species should have transitions together with the sbo 
							outputGroupFlatSpecies = new ArrayList<>();
							boolean areAllProteins = true;
							for (SBMLQualSpecies groupSpecies : this.sbmlQualSpeciesService.getSBMLQualSpeciesOfGroup(outputSpeciesUUID)) {
								if(areAllProteins) {
									// if up to this point all have been proteins (so sbo term has always been 252, then check this one
									// if one has not been 252 (so a compound or such), it will have been set to false, and must stay this way).
									areAllProteins = groupSpecies.getsBaseSboTerm().equals("SBO:0000252");
								}
								if (sbmlSBaseEntityUUIDToFlatSpeciesMap.containsKey(groupSpecies.getEntityUUID())) {
									outputGroupFlatSpecies.add(sbmlSBaseEntityUUIDToFlatSpeciesMap.get(groupSpecies.getEntityUUID()));
								} else {
									// haven't seen this Species before
									FlatSpecies newGroupFlatSpecies = createFlatSpeciesFromSBMLSBaseEntity(groupSpecies);
									if (!flatSpeciesSymbols.add(newGroupFlatSpecies.getSymbol())) {
										logger.warn("Duplicate Symbol " + newGroupFlatSpecies.getSymbol() + " in Mapping. Offending FlatSpecies has entityUUID: " + newGroupFlatSpecies.getEntityUUID());
									}
									sbmlSBaseEntityUUIDToFlatSpeciesMap.put(groupSpecies.getEntityUUID(),
											newGroupFlatSpecies);
									nodeTypes.add(groupSpecies.getsBaseSboTerm());
									// finally add this new species to the group specific list for connection
									outputGroupFlatSpecies.add(newGroupFlatSpecies);
								}
							}
							// add these flatSpecies to map for later reference when we see this group again
							groupNodeUUIDToGroupFlatSpecies.put(outputSpeciesUUID, outputGroupFlatSpecies);
							
							// now groupFlatSpecies contains all FlatSpecies that belong to this group.
							// connect them with flatEdges
							//	  a) if all participants are proteins: SBO:0000526 protein complex formation
							//	  b) otherwise SBO:0000344 molecular interaction
							for (int i = 0; i != outputGroupFlatSpecies.size(); i++) {
								for (int j = 0; j != outputGroupFlatSpecies.size(); j++) {
									if (i != j) {
										// add two flat edges (i -> j and j -> i)
										FlatEdge transitionFlatEdge = this.flatEdgeService.createFlatEdge(areAllProteins ? "SBO:0000526" : "SBO:0000344");
										this.graphBaseEntityService.setGraphBaseEntityProperties(transitionFlatEdge);
										transitionFlatEdge.setInputFlatSpecies(outputGroupFlatSpecies.get(i));
										transitionFlatEdge.setOutputFlatSpecies(outputGroupFlatSpecies.get(j));
										transitionFlatEdge.addAnnotation("SBOTerm", areAllProteins ? "SBO:0000526" : "SBO:0000344");
		
										transitionFlatEdge.addAnnotationType("SBOTerm", "String");
										String symbolAndTransitionIdString = outputGroupFlatSpecies.get(i).getSymbol() + "-" 
												+ (areAllProteins ? "proteincomplexformation" : "molecularinteraction") + "->" + outputGroupFlatSpecies.get(j).getSymbol();
										transitionFlatEdge.addAnnotation("TransitionId", symbolAndTransitionIdString);
										transitionFlatEdge.addAnnotationType("TransitionId", "String");
										transitionFlatEdge.setSymbol(symbolAndTransitionIdString);
										relationTypes.add(transitionFlatEdge.getTypeString());
										transitionFlatEdge.addAnnotation("sbmlSimpleModelEntityUUID",
												outputSpeciesUUID);
										transitionFlatEdge.addAnnotationType("sbmlSimpleModelEntityUUID", "String");
										//sbmlSimpleTransitionToFlatEdgeMap.put(currentTransition.getOutputSpecies().getEntityUUID(), transitionFlatEdge);
										allFlatEdges.add(transitionFlatEdge);
										relationSymbols.add(symbolAndTransitionIdString);
									}
								}
							}
						}
					} else {
						outputFlatSpecies = this.createFlatSpeciesFromSBMLSBaseEntity(currentTransition.getOutputSpecies());
						if (!flatSpeciesSymbols.add(outputFlatSpecies.getSymbol())) {
							logger.warn("Duplicate Symbol " + outputFlatSpecies.getSymbol() + " in Mapping. Offending FlatSpecies has entityUUID: " + outputFlatSpecies.getEntityUUID());
						}
						nodeTypes.add(currentTransition.getOutputSpecies().getsBaseSboTerm());
						sbmlSBaseEntityUUIDToFlatSpeciesMap.put(outputSpeciesUUID,
								outputFlatSpecies);
					}
				}
				
				// now build the transition or transitions, if we had one or two groups
				
				if (inputFlatSpecies != null) {
					
					if (outputFlatSpecies != null) {
						// no groups, so two normal species, this should be the standard case
						FlatEdge transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpecies, outputFlatSpecies, currentTransition);
						relationTypes.add(transitionFlatEdge.getTypeString());
						relationSymbols.add(transitionFlatEdge.getSymbol());
						allFlatEdges.add(transitionFlatEdge);
						if (inputFlatSpecies.getLabels().contains("Drug")) {
							FlatEdge targetsEdge = createTargetsEdge(inputFlatSpecies, outputFlatSpecies);
							allFlatEdges.add(targetsEdge);
						}
					} else {
						// input is single, output is group
						for (FlatSpecies outputFlatSpeciesOfGroup : outputGroupFlatSpecies) {
							FlatEdge transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpecies, outputFlatSpeciesOfGroup, currentTransition);
							relationTypes.add(transitionFlatEdge.getTypeString());
							relationSymbols.add(transitionFlatEdge.getSymbol());
							allFlatEdges.add(transitionFlatEdge);
							if (inputFlatSpecies.getLabels().contains("Drug")) {
								FlatEdge targetsEdge = createTargetsEdge(inputFlatSpecies, outputFlatSpeciesOfGroup);
								allFlatEdges.add(targetsEdge);
							}
						}
					}
				} else if (outputFlatSpecies != null) {
					// input is group, output is single
					for (FlatSpecies inputFlatSpeciesOfGroup : inputGroupFlatSpecies) {
						FlatEdge transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpeciesOfGroup, outputFlatSpecies, currentTransition);
						relationTypes.add(transitionFlatEdge.getTypeString());
						relationSymbols.add(transitionFlatEdge.getSymbol());
						allFlatEdges.add(transitionFlatEdge);
						if (inputFlatSpeciesOfGroup.getLabels().contains("Drug")) {
							FlatEdge targetsEdge = createTargetsEdge(inputFlatSpeciesOfGroup, outputFlatSpecies);
							allFlatEdges.add(targetsEdge);
						}
					}
				} else {
					// input is group, output is group
					for (int i = 0; i != inputGroupFlatSpecies.size(); i++) {
						FlatSpecies inputFlatSpeciesOfGroup = inputGroupFlatSpecies.get(i);
						for (int j = 0; j != outputGroupFlatSpecies.size(); j ++) {
							FlatSpecies outputFlatSpeciesOfGroup = outputGroupFlatSpecies.get(j);
							FlatEdge transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpeciesOfGroup, outputFlatSpeciesOfGroup, currentTransition);
							relationTypes.add(transitionFlatEdge.getTypeString());
							relationSymbols.add(transitionFlatEdge.getSymbol());
							allFlatEdges.add(transitionFlatEdge);
							if (inputFlatSpeciesOfGroup.getLabels().contains("Drug") ) {
								FlatEdge targetsEdge = createTargetsEdge(inputFlatSpeciesOfGroup, outputFlatSpeciesOfGroup);
								allFlatEdges.add(targetsEdge);
							}
						}
					}
				} 
			}
		}
		for (String entry : sbmlSBaseEntityUUIDToFlatSpeciesMap.keySet()) {
			allFlatSpecies.add(sbmlSBaseEntityUUIDToFlatSpeciesMap.get(entry));
		}
		
		// TODO: Create and Add FlatSpecies for those SBMLSpecies that do not have any connection (aka unconnected nodes)
		
		persistedFlatSpecies = this.flatSpeciesService.save(allFlatSpecies, QUERY_DEPTH_ZERO);

		this.flatEdgeService.save(allFlatEdges, QUERY_DEPTH_ZERO);

		Instant endTime = Instant.now();
		mappingFromPathway.addWarehouseAnnotation("creationstarttime", startTime.toString());
		mappingFromPathway.addWarehouseAnnotation("creationendtime", endTime.toString());
		mappingFromPathway.addWarehouseAnnotation("numberofnodes", String.valueOf(allFlatSpecies.size())); // this includes reactions
		mappingFromPathway.addWarehouseAnnotation("numberofrelations", String.valueOf(allFlatEdges.size()));
		mappingFromPathway.addWarehouseAnnotation("numberofreactions", String.valueOf(numberOfReactions));
		mappingFromPathway.setMappingNodeTypes(nodeTypes);
		mappingFromPathway.setMappingRelationTypes(relationTypes);
		mappingFromPathway.setMappingNodeSymbols(flatSpeciesSymbols);
		mappingFromPathway.setMappingRelationSymbols(relationSymbols);
		mappingFromPathway.setActive(true);
		MappingNode persistedMappingOfPathway = (MappingNode) this.warehouseGraphService
				.saveWarehouseGraphNodeEntity(mappingFromPathway, QUERY_DEPTH_ZERO);
		logger.info(Instant.now().toString() + ": Persisted FlatSpecies. Starting to connect");
		int fsConnectCounter = 1;

		for (FlatSpecies fs : persistedFlatSpecies) {
			logger.debug("Building ConnectionSet #" + fsConnectCounter++ + ": FlatSpecies: " + fs.getEntityUUID());
			// do not build the wasDerivedFrom and DERIVEDFROM connections. They take very long and are never traversed/used later on
			//this.provenanceGraphService.connect(fs,	fs.getSimpleModelEntityUUID(), ProvenanceGraphEdgeType.wasDerivedFrom);
			//this.warehouseGraphService.connect(fs, fs.getSimpleModelEntityUUID(), WarehouseGraphEdgeType.DERIVEDFROM);
			this.provenanceGraphService.connect(fs, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			this.warehouseGraphService.connect(persistedMappingOfPathway, fs, WarehouseGraphEdgeType.CONTAINS);
		}
		return persistedMappingOfPathway;
	}

	private FlatEdge createTargetsEdge(FlatSpecies inputFlatSpecies, FlatSpecies outputFlatSpecies) {
		FlatEdge targetsEdge = this.flatEdgeService.createFlatEdge("targets");
		this.graphBaseEntityService.setGraphBaseEntityProperties(targetsEdge);
		targetsEdge.setInputFlatSpecies(inputFlatSpecies);
		targetsEdge.setOutputFlatSpecies(outputFlatSpecies);
		targetsEdge.setSymbol(inputFlatSpecies.getSymbol() + "-TARGETS->"
				+ outputFlatSpecies.getSymbol());
		return targetsEdge;
	}

	/**
	 * Creates <a href="#{@link}">{@link FlatEdge}</a> entity from a <a href="#{@link}">{@link SBMLSimpleTransition}</a>
	 * 
	 * @param inputFlatSpecies The startNode <a href="#{@link}">{@link FlatSpecies}</a> of the new <a href="#{@link}">{@link FlatEdge}</a> 
	 * @param outputFlatSpecies The endNode <a href="#{@link}">{@link FlatSpecies}</a>of the new <a href="#{@link}">{@link FlatEdge}</a>
	 * @param simpleTransition The The startNode <a href="#{@link}">{@link SBMLSimpleTransition}</a> entity to use as source for the new <a href="#{@link}">{@link FlatEdge}</a>
	 * @return The created <a href="#{@link}">{@link FlatEdge}</a>
	 */
	private FlatEdge createFlatEdgeFromSimpleTransition(FlatSpecies inputFlatSpecies, FlatSpecies outputFlatSpecies,
			SBMLSimpleTransition simpleTransition) {
		FlatEdge transitionFlatEdge;
		transitionFlatEdge = this.flatEdgeService.createFlatEdge(simpleTransition.getsBaseSboTerm());
		this.graphBaseEntityService.setGraphBaseEntityProperties(transitionFlatEdge);
		transitionFlatEdge.setInputFlatSpecies(inputFlatSpecies);
		transitionFlatEdge.setOutputFlatSpecies(outputFlatSpecies);
		transitionFlatEdge.addAnnotation("SBOTerm", simpleTransition.getsBaseSboTerm());

		transitionFlatEdge.addAnnotationType("SBOTerm", "String");
		transitionFlatEdge.addAnnotation("TransitionId", simpleTransition.getTransitionId());
		transitionFlatEdge.addAnnotationType("TransitionId", "String");
		transitionFlatEdge.setSymbol(inputFlatSpecies.getSymbol() + "-" + transitionFlatEdge.getTypeString()
				+ "->" + outputFlatSpecies.getSymbol());
		
		transitionFlatEdge.addAnnotation("sbmlSimpleModelEntityUUID",
				simpleTransition.getEntityUUID());
		transitionFlatEdge.addAnnotationType("sbmlSimpleModelEntityUUID", "String");
		return transitionFlatEdge;
	}

	/**
	 * Create a <a href="#{@link}">{@link FlatSpecies}</a> from a <a href="#{@link}">{@link SBMLSpecies}</a>
	 * 
	 * @param sbmlSBaseEntity The <a href="#{@link}">{@link SBMLSBaseEntity}</a> to be used as source for the new <a href="#{@link}">{@link FlatSpecies}</a>
	 * @return The created <a href="#{@link}">{@link FlatSpecies}</a>
	 */
	private FlatSpecies createFlatSpeciesFromSBMLSBaseEntity(SBMLSBaseEntity sbmlSBaseEntity) {
		FlatSpecies newFlatSpecies = new FlatSpecies();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newFlatSpecies);
		newFlatSpecies.setSymbol(sbmlSBaseEntity.getsBaseName());
		
		newFlatSpecies.setSboTerm(sbmlSBaseEntity.getsBaseSboTerm());
		
		newFlatSpecies.setSimpleModelEntityUUID(sbmlSBaseEntity.getEntityUUID());
		getBQAnnotations(sbmlSBaseEntity.getEntityUUID(), newFlatSpecies);
		getPathwayAnnotations(UUID.fromString(sbmlSBaseEntity.getEntityUUID()), newFlatSpecies);
		return newFlatSpecies;
	}

	/**
	 * Query the biomodelsQualifierRepository for all Biomodels Qualifiers of Type HAS_VERSION, IS, HAS_PROPERTY, IS_ENCODED_BY and IS_DESCRIBED_BY
	 * 
	 * @param parentEntityUUID The Node to which the Biomodels Qualifier should be fetched for
	 * @param target The Flat Species those Qualifiers should be added as Annotations (always as type "string")
	 */
	private void getBQAnnotations(String parentEntityUUID, FlatSpecies target) {
		boolean doAppend = this.sbml4jConfig.getAnnotationConfigProperties().isAppend();
		boolean annotateWithLinks = this.sbml4jConfig.getAnnotationConfigProperties().isAnnotateWithLinks();
		List<BiomodelsQualifier> bqList = this.biomodelsQualifierRepository.findAllForSBaseEntity(parentEntityUUID);
		for(BiomodelsQualifier bq : bqList) {
			//logger.debug(bq.getEndNode().getUri());
			
			if (bq.getQualifier().equals(Qualifier.BQB_HAS_VERSION) 
					|| bq.getQualifier().equals(Qualifier.BQB_IS)
					|| bq.getQualifier().equals(Qualifier.BQB_HAS_PROPERTY)
					|| bq.getQualifier().equals(Qualifier.BQB_IS_ENCODED_BY)
					|| bq.getQualifier().equals(Qualifier.BQB_IS_DESCRIBED_BY)
				) {
				ExternalResourceEntity endNode = bq.getEndNode();
				String uri = endNode.getUri();
				if (uri.contains("identifiers.org")) {
				
					String[] splitted = uri.split("/");
					this.graphBaseEntityService.addAnnotation(target, splitted[splitted.length-2].replace('.', '_'), "string", (annotateWithLinks ? uri : splitted[splitted.length-1]), doAppend);
					
					if (bq.getEndNode().getType() != null && endNode.getType().equals(ExternalResourceType.KEGGGENES)) {
						// get all NameNodes connected to the external resource 
						Iterable<NameNode> allNames = this.nameNodeService.findAllByExternalResource(endNode.getEntityUUID());
						Iterator<NameNode> it = allNames.iterator();
						while (it.hasNext()) {
							target.addSecondaryName(it.next().getName());
						}
					} else if (bq.getEndNode().getType() != null && bq.getEndNode().getType().equals(ExternalResourceType.KEGGDRUG)) {
						target.addLabel("Drug");
					}
				} else if (bq.getEndNode().getType().equals(ExternalResourceType.MDANDERSON)) {
					this.graphBaseEntityService.addAnnotation(target, "mdanderson", "string", uri, doAppend);
				}
			}
		}
	}

	/**
	 * Get the pathways of an sBaseNode and add them to the annotation of the <a href="#{@link}">{@link FlatSpecies}</a> target
	 * 
	 * @param parentEntityUUID The entityUUID of the entity to get the pathways from
	 * @param target The <a href="#{@link}">{@link FlatSpecies}</a> to add the pathway names to
	 */
	private void getPathwayAnnotations(UUID parentEntityUUID, FlatSpecies target) {
		List<PathwayNode> pathwayUUIDs = this.pathwayService.getPathwayNodesOfSBase(parentEntityUUID);
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (PathwayNode pathway : pathwayUUIDs) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			try {
				sb.append(pathway.getPathwayIdString().split("_")[1]);
			} catch (Exception e) {
				logger.warn("Pathway with uuid " + pathway.getEntityUUID() + " has abnormal pathwayIdString " + pathway.getPathwayIdString());
				sb.append(pathway.getPathwayIdString());
			}			
		}
		this.graphBaseEntityService.addAnnotation(target, AnnotationName.PATHWAYS.getAnnotationName(), "string", sb.toString(), true); // always append pathways, if not use this: this.sbml4jConfig.getAnnotationConfigProperties().isAppend());
	}

}
