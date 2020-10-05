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
import org.tts.config.SBML4jConfig;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.common.GraphEnum.AnnotationName;
import org.tts.model.common.GraphEnum.ExternalResourceType;
import org.tts.model.common.GraphEnum.NetworkMappingType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.simple.SBMLSimpleTransition;
import org.tts.model.warehouse.MappingNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.repository.common.BiomodelsQualifierRepository;
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
	PathwayService pathwayService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;

	@Autowired
	SBMLSpeciesService sbmlSpeciesSevice;

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
	 * @return The created <a href="#{@link}">{@link MappingNpde}</a> that contains the new Mapping
	 */
	@Override
	public MappingNode createMappingFromPathway(PathwayNode pathway, NetworkMappingType type,
			ProvenanceGraphActivityNode activityNode, ProvenanceGraphAgentNode agentNode) throws Exception {

		Instant startTime = Instant.now();
		logger.info("Started Creating Mapping from Pathway at " + startTime.toString());
		// need a Warehouse Node of Type Mapping, set NetworkMappingTypes
		String mappingName = "Map_" + type.name() + "_" + pathway.getPathwayIdString();

		MappingNode mappingFromPathway = this.mappingNodeService.createMappingNode(pathway, type, mappingName);
		logger.info("Created MappingNode with uuid:" + mappingFromPathway.getEntityUUID());
		Set<String> relationTypes = new HashSet<>();
		Set<String> nodeTypes = new HashSet<>();
		Set<String> nodeSymbols = new HashSet<>();
		Set<String> relationSymbols = new HashSet<>();
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
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getSpecies().getEntityUUID())) {
					startFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(current.getSpecies().getEntityUUID());
					startFlatSpeciesExists = true;
				} else {
					startFlatSpecies = new FlatSpecies();
					this.graphBaseEntityService.setGraphBaseEntityProperties(startFlatSpecies);
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
					this.graphBaseEntityService.setGraphBaseEntityProperties(endFlatSpecies);
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
		
		Map<String, FlatEdge> sbmlSimpleTransitionToFlatEdgeMap = new HashMap<>();
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
		// do we have non metabolic elements we need to process?
		if (processTransitionsNeeded) {
			Iterable<NonMetabolicPathwayReturnType> nonMetPathwayResult = this.pathwayService
					.getAllNonMetabolicPathwayReturnTypes(UUID.fromString(pathway.getEntityUUID()), transitionSBOTerms,
							nodeSBOTerms);

			Iterator<NonMetabolicPathwayReturnType> it = nonMetPathwayResult.iterator();
			NonMetabolicPathwayReturnType current;
			FlatSpecies inputFlatSpecies = null;
			FlatSpecies outputFlatSpecies = null;
			FlatEdge transitionFlatEdge;
			while (it.hasNext()) {
				List<FlatSpecies> inputGroupFlatSpecies = null;
				List<FlatSpecies> outputGroupFlatSpecies = null;
				current = it.next();
				
				// input species
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getInputSpecies().getEntityUUID())) {
					inputFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap
							.get(current.getInputSpecies().getEntityUUID());
				} else {
					if(current.getInputSpecies().getsBaseSboTerm().equals("SBO:0000253")) {
						// This is a group node, we need to treat that
						// the member species should have transitions together with the sbo 
						inputGroupFlatSpecies = new ArrayList<>();
						boolean areAllProteins = true;
						for(SBMLSpecies groupSpecies : this.sbmlSpeciesSevice.getSBMLSpeciesOfGroup(current.getInputSpecies().getEntityUUID())) {
							if(areAllProteins) {
								// if up to this point all have been proteins (so sbo term has always been 252, then check this one
								// if one has not been 252 (so a compound or such), it will have been set to false, and must stay this way).
								areAllProteins = groupSpecies.getsBaseSboTerm().equals("SBO:0000252");
							}
							if(sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(groupSpecies.getEntityUUID())) {
								inputGroupFlatSpecies.add(sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(groupSpecies.getEntityUUID()));
							} else {
								// haven't seen this Species before
								FlatSpecies newGroupFlatSpecies = createFlatSpeciesFromSBMLSpecies(groupSpecies);
								sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(groupSpecies.getEntityUUID(),
										newGroupFlatSpecies);
								nodeTypes.add(groupSpecies.getsBaseSboTerm());
								nodeSymbols.add(groupSpecies.getsBaseName());
								// finally add this new species to the group specific list for connection
								inputGroupFlatSpecies.add(newGroupFlatSpecies);
							}
						}
						// now groupFlatSpecies contains all FlatSpecies that belong to this group.
						// connect them with flatEdges
						//	  a) if all participants are proteins: SBO:0000526 protein complex formation
						//	  b) otherwise SBO:0000344 molecular interaction
						for (int i = 0; i != inputGroupFlatSpecies.size(); i++) {
							for (int j = 0; j != inputGroupFlatSpecies.size(); j++) {
								if (i != j) {
									// add two flat edges (i -> j and j -> i)
									transitionFlatEdge = this.flatEdgeService.createFlatEdge(areAllProteins ? "SBO:0000526" : "SBO:0000344");
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
									transitionFlatEdge.addAnnotation("sbmlSimpleTransitionEntityUUID",
											current.getInputSpecies().getEntityUUID());
									transitionFlatEdge.addAnnotationType("sbmlSimpleTransitionEntityUUID", "String");
									sbmlSimpleTransitionToFlatEdgeMap.put(current.getInputSpecies().getEntityUUID(), transitionFlatEdge);
									relationSymbols.add(symbolAndTransitionIdString);
								}
							}
						}
					} else {
					inputFlatSpecies = this.createFlatSpeciesFromSBMLSpecies(current.getInputSpecies());
					nodeSymbols.add(current.getInputSpecies().getsBaseName());
					nodeTypes.add(current.getInputSpecies().getsBaseSboTerm());
					sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(current.getInputSpecies().getEntityUUID(),
							inputFlatSpecies);
					}
				}
				// output species
				if (sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(current.getOutputSpecies().getEntityUUID())) {
					outputFlatSpecies = sbmlSpeciesEntityUUIDToFlatSpeciesMap
							.get(current.getOutputSpecies().getEntityUUID());
				} else {
					if(current.getOutputSpecies().getsBaseSboTerm().equals("SBO:0000253")) {
						// This is a group node, we need to treat that
						// the member species should have transitions together with the sbo 
						outputGroupFlatSpecies = new ArrayList<>();
						boolean areAllProteins = true;
						for(SBMLSpecies groupSpecies : this.sbmlSpeciesSevice.getSBMLSpeciesOfGroup(current.getOutputSpecies().getEntityUUID())) {
							if(areAllProteins) {
								// if up to this point all have been proteins (so sbo term has always been 252, then check this one
								// if one has not been 252 (so a compound or such), it will have been set to false, and must stay this way).
								areAllProteins = groupSpecies.getsBaseSboTerm().equals("SBO:0000252");
							}
							if(sbmlSpeciesEntityUUIDToFlatSpeciesMap.containsKey(groupSpecies.getEntityUUID())) {
								outputGroupFlatSpecies.add(sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(groupSpecies.getEntityUUID()));
							} else {
								// haven't seen this Species before
								FlatSpecies newGroupFlatSpecies = createFlatSpeciesFromSBMLSpecies(groupSpecies);
								sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(groupSpecies.getEntityUUID(),
										newGroupFlatSpecies);
								nodeTypes.add(groupSpecies.getsBaseSboTerm());
								nodeSymbols.add(groupSpecies.getsBaseName());
								// finally add this new species to the group specific list for connection
								outputGroupFlatSpecies.add(newGroupFlatSpecies);
							}
						}
						// now groupFlatSpecies contains all FlatSpecies that belong to this group.
						// connect them with flatEdges
						//	  a) if all participants are proteins: SBO:0000526 protein complex formation
						//	  b) otherwise SBO:0000344 molecular interaction
						for (int i = 0; i != outputGroupFlatSpecies.size(); i++) {
							for (int j = 0; j != outputGroupFlatSpecies.size(); j++) {
								if (i != j) {
									// add two flat edges (i -> j and j -> i)
									transitionFlatEdge = this.flatEdgeService.createFlatEdge(areAllProteins ? "SBO:0000526" : "SBO:0000344");
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
									transitionFlatEdge.addAnnotation("sbmlSimpleTransitionEntityUUID",
											current.getOutputSpecies().getEntityUUID());
									transitionFlatEdge.addAnnotationType("sbmlSimpleTransitionEntityUUID", "String");
									sbmlSimpleTransitionToFlatEdgeMap.put(current.getOutputSpecies().getEntityUUID(), transitionFlatEdge);
									relationSymbols.add(symbolAndTransitionIdString);
								}
							}
						}
					} else {
						outputFlatSpecies = this.createFlatSpeciesFromSBMLSpecies(current.getOutputSpecies());
						nodeSymbols.add(current.getOutputSpecies().getsBaseName());
						nodeTypes.add(current.getOutputSpecies().getsBaseSboTerm());
						sbmlSpeciesEntityUUIDToFlatSpeciesMap.put(current.getOutputSpecies().getEntityUUID(),
								outputFlatSpecies);
					}
				}
				
				// transition
				
				// now we need to look at whether we had one or two groups
				SBMLSimpleTransition currentTransition = current.getTransition();
				
				if (inputFlatSpecies != null) {
					
					if (outputFlatSpecies != null) {
						// no groups, so two normal species, this should be the standard case
						if (sbmlSimpleTransitionToFlatEdgeMap.containsKey(currentTransition.getEntityUUID())) {
							transitionFlatEdge = sbmlSimpleTransitionToFlatEdgeMap.get(currentTransition.getEntityUUID());
							logger.info("Reusing Transition " + currentTransition.getEntityUUID());
							
							if (!(transitionFlatEdge.getInputFlatSpecies().equals(inputFlatSpecies)
									&& transitionFlatEdge.getOutputFlatSpecies().equals(outputFlatSpecies))) {
								// this should be identical, if the transition really is the same
								logger.info("Transition does not have the same input and output.. Aborting");
								throw new Exception("Reusing Transition " + currentTransition.getEntityUUID()
										+ ", but input and output are not the same");
							} else {
								// already have this transition, continue
								continue;
							}
						} else {
							transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpecies, outputFlatSpecies, currentTransition);
							relationTypes.add(transitionFlatEdge.getTypeString());
							relationSymbols.add(transitionFlatEdge.getSymbol());
							sbmlSimpleTransitionToFlatEdgeMap.put(currentTransition.getEntityUUID(), transitionFlatEdge);
						}
					} else {
						// input single, output group
						for (FlatSpecies outputFlatSpeciesOfGroup : outputGroupFlatSpecies) {
							String transitionUUID = currentTransition.getEntityUUID() + "]->(" + outputFlatSpeciesOfGroup.getEntityUUID();
							if (sbmlSimpleTransitionToFlatEdgeMap.containsKey(transitionUUID)) {
								transitionFlatEdge = sbmlSimpleTransitionToFlatEdgeMap.get(transitionUUID);
								logger.info("Reusing Transition To Group " + transitionUUID);
							} else {
								transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpecies, outputFlatSpeciesOfGroup, currentTransition);
								relationTypes.add(transitionFlatEdge.getTypeString());
								relationSymbols.add(transitionFlatEdge.getSymbol());
								sbmlSimpleTransitionToFlatEdgeMap.put(transitionUUID, transitionFlatEdge);
							}
						}
					}
				} else if (outputFlatSpecies != null) {
					for (FlatSpecies inputFlatSpeciesOfGroup : inputGroupFlatSpecies) {
						String transitionUUID = inputFlatSpeciesOfGroup.getEntityUUID() + ")->[" + currentTransition.getEntityUUID();
						if(sbmlSimpleTransitionToFlatEdgeMap.containsKey(transitionUUID)) {
							transitionFlatEdge = sbmlSimpleTransitionToFlatEdgeMap.get(transitionUUID);
							logger.info("Reusing Transition from Group " + transitionUUID);
						} else {
							transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpeciesOfGroup, outputFlatSpecies, currentTransition);
							relationTypes.add(transitionFlatEdge.getTypeString());
							relationSymbols.add(transitionFlatEdge.getSymbol());
							sbmlSimpleTransitionToFlatEdgeMap.put(transitionUUID, transitionFlatEdge);
						}
					}
				} else {
					for (int i = 0; i != inputGroupFlatSpecies.size(); i++) {
						FlatSpecies inputFlatSpeciesOfGroup = inputGroupFlatSpecies.get(i);
						for (int j = 0; j != outputGroupFlatSpecies.size(); j ++) {
							FlatSpecies outputFlatSpeciesOfGroup = outputGroupFlatSpecies.get(j);
							String transitionUUID = inputFlatSpeciesOfGroup.getEntityUUID() + ")->[" + currentTransition.getEntityUUID() + "]->(" + outputFlatSpeciesOfGroup.getEntityUUID();
							if(sbmlSimpleTransitionToFlatEdgeMap.containsKey(transitionUUID)) {
								transitionFlatEdge = sbmlSimpleTransitionToFlatEdgeMap.get(transitionUUID);
								logger.info("Reusing Transition to and from Group " + transitionUUID);
							}else {
								transitionFlatEdge = createFlatEdgeFromSimpleTransition(inputFlatSpeciesOfGroup, outputFlatSpeciesOfGroup, currentTransition);
								relationTypes.add(transitionFlatEdge.getTypeString());
								relationSymbols.add(transitionFlatEdge.getSymbol());
								sbmlSimpleTransitionToFlatEdgeMap.put(transitionUUID, transitionFlatEdge);
							}
						}
					}
				} 
			}
		}
		for (String entry : sbmlSpeciesEntityUUIDToFlatSpeciesMap.keySet()) {
			allFlatSpecies.add(sbmlSpeciesEntityUUIDToFlatSpeciesMap.get(entry));
		}
		
		// TODO: Create and Add FlatSpecies for those SBMLSpecies that do not have any connection (aka unconnected nodes)
		
		persistedFlatSpecies = this.flatSpeciesService.save(allFlatSpecies, QUERY_DEPTH_ZERO);

		for (String entry : sbmlSimpleTransitionToFlatEdgeMap.keySet()) {
			allFlatEdges.add(sbmlSimpleTransitionToFlatEdgeMap.get(entry));
		}
		this.flatEdgeService.save(allFlatEdges, QUERY_DEPTH_ZERO);

		Instant endTime = Instant.now();
		mappingFromPathway.addWarehouseAnnotation("creationstarttime", startTime.toString());
		mappingFromPathway.addWarehouseAnnotation("creationendtime", endTime.toString());
		mappingFromPathway.addWarehouseAnnotation("numberofnodes", String.valueOf(allFlatSpecies.size()));
		mappingFromPathway.addWarehouseAnnotation("numberofrelations", String.valueOf(allFlatEdges.size()));
		mappingFromPathway.setMappingNodeTypes(nodeTypes);
		mappingFromPathway.setMappingRelationTypes(relationTypes);
		mappingFromPathway.setMappingNodeSymbols(nodeSymbols);
		mappingFromPathway.setMappingRelationSymbols(relationSymbols);
		MappingNode persistedMappingOfPathway = (MappingNode) this.warehouseGraphService
				.saveWarehouseGraphNodeEntity(mappingFromPathway, QUERY_DEPTH_ZERO);
		logger.info(Instant.now().toString() + ": Persisted FlatSpecies. Starting to connect");
		int fsConnectCounter = 1;

		for (FlatSpecies fs : persistedFlatSpecies) {
			logger.info(Instant.now().toString() + ": Building ConnectionSet #" + fsConnectCounter++);
			this.provenanceGraphService.connect(fs,	fs.getSimpleModelEntityUUID(), ProvenanceGraphEdgeType.wasDerivedFrom);
			this.warehouseGraphService.connect(fs, fs.getSimpleModelEntityUUID(), WarehouseGraphEdgeType.DERIVEDFROM);
			this.provenanceGraphService.connect(fs, activityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			this.warehouseGraphService.connect(persistedMappingOfPathway, fs, WarehouseGraphEdgeType.CONTAINS);
		}
		return persistedMappingOfPathway;
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
		
		transitionFlatEdge.addAnnotation("sbmlSimpleTransitionEntityUUID",
				simpleTransition.getEntityUUID());
		transitionFlatEdge.addAnnotationType("sbmlSimpleTransitionEntityUUID", "String");
		return transitionFlatEdge;
	}

	/**
	 * Create a <a href="#{@link}">{@link FlatSpecies}</a> from a <a href="#{@link}">{@link SBMLSpecies}</a>
	 * 
	 * @param sbmlSpecies The <a href="#{@link}">{@link SBMLSpecies}</a> to be used as source for the new <a href="#{@link}">{@link FlatSpecies}</a>
	 * @return The created <a href="#{@link}">{@link FlatSpecies}</a>
	 */
	private FlatSpecies createFlatSpeciesFromSBMLSpecies(SBMLSpecies sbmlSpecies) {
		FlatSpecies newFlatSpecies = new FlatSpecies();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newFlatSpecies);
		newFlatSpecies.setSymbol(sbmlSpecies.getsBaseName());
		
		newFlatSpecies.setSboTerm(sbmlSpecies.getsBaseSboTerm());
		
		newFlatSpecies.setSimpleModelEntityUUID(sbmlSpecies.getEntityUUID());
		getBQAnnotations(sbmlSpecies.getEntityUUID(), newFlatSpecies);
		getPathwayAnnotations(UUID.fromString(sbmlSpecies.getEntityUUID()), newFlatSpecies);
		return newFlatSpecies;
	}

	/**
	 * Query the biomodelsQualifierRepository for all Biomodels Qualifiers of Type HAS_VERSION, IS, HAS_PROPERTY, IS_ENCODED_BY and IS_DESCRIBED_BY
	 * 
	 * @param parentEntityUUID The Node to which the Biomodels Qualifier should be fetched for
	 * @param target The Flat Species those Qualifiers should be added as Annotations (always as type "string"
	 */
	private void getBQAnnotations(String parentEntityUUID, FlatSpecies target) {
		boolean doAppend = this.sbml4jConfig.getAnnotationConfigProperties().isAppend();
		boolean annotateWithLinks = this.sbml4jConfig.getAnnotationConfigProperties().isAnnotateWithLinks();
		List<BiomodelsQualifier> bqList = this.biomodelsQualifierRepository.findAllForSBMLSpecies(parentEntityUUID);
		for(BiomodelsQualifier bq : bqList) {
			//System.out.println(bq.getEndNode().getUri());
			
			if (bq.getQualifier().equals(Qualifier.BQB_HAS_VERSION) 
					|| bq.getQualifier().equals(Qualifier.BQB_IS)
					|| bq.getQualifier().equals(Qualifier.BQB_HAS_PROPERTY)
					|| bq.getQualifier().equals(Qualifier.BQB_IS_ENCODED_BY)
					|| bq.getQualifier().equals(Qualifier.BQB_IS_DESCRIBED_BY)
				) {
				String uri = bq.getEndNode().getUri();
				if (uri.contains("identifiers.org")) {
				
					String[] splitted = uri.split("/");
					this.graphBaseEntityService.addAnnotation(target, splitted[splitted.length-2].replace('.', '_'), "string", (annotateWithLinks ? uri : splitted[splitted.length-1]), doAppend);
					
					if (bq.getEndNode().getType() != null && bq.getEndNode().getType().equals(ExternalResourceType.KEGGGENES) && bq.getEndNode().getSecondaryNames() != null) {
						StringBuilder sb = new StringBuilder();
						boolean first = true;
						for (String secName : bq.getEndNode().getSecondaryNames()) {
							if (!first) {
								sb.append(", ");
							} else {
								first = false;
							}
							sb.append(secName);
						}
						GraphBaseEntity annotatedEntity = this.graphBaseEntityService.addAnnotation(target, AnnotationName.SECONDARYNAMES.getAnnotationName(), "string", sb.toString(), doAppend);
						String secondaryNames = (String) annotatedEntity.getAnnotation().get(AnnotationName.SECONDARYNAMES.getAnnotationName());
						int numSecondaryNames = secondaryNames.split(", ").length;
						if (numSecondaryNames > bq.getEndNode().getSecondaryNames().length && !secondaryNames.contains(bq.getEndNode().getName())) {
							secondaryNames += ", ";
							secondaryNames += bq.getEndNode().getName();
							annotatedEntity.getAnnotation().put(AnnotationName.SECONDARYNAMES.getAnnotationName(), secondaryNames);
						}
					}
				} else if (bq.getEndNode().getType().equals(ExternalResourceType.MDANDERSON)) { // uri.contains("mdanderson")) {
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
