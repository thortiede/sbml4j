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
package org.sbml4j.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.api.FilterOptions;
import org.sbml4j.model.api.Output.ApocPathReturnType;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.service.networks.NetworkService;
import org.sbml4j.service.warehouse.MappingNodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for creating context networks for genes and geneSets
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class ContextService {
	
	Logger log = LoggerFactory.getLogger(ContextService.class);
	
	@Autowired
	ApocService apocService;
	
	@Autowired
	FlatEdgeService flatEdgeService;
	
	@Autowired
	MappingNodeService mappingNodeService;
	
	@Autowired
	NetworkService networkService;
	
	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	/**
	 * Get List of <a href="#{@link}">{@link FlatEdge}</a> entities that make up the gene context in a network around a set of input genes
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @param genes A List of gene names to find the context for
	 * @param minSize The minimal step size to take for a context path
	 * @param maxSize The maximal step size to take for a context path
	 * @param terminateAtDrug Whether the context should always terminate in a Drug node (yes), or in any node (false). Attn: Requires MyDrug Nodes in the Network
	 * @param direction The direction to expand the context to (one of upstream, downstream, both)
	 * @return List of <a href="#{@link}">{@link FlatEdge}</a> entities that make up the context
	 */
	public List<FlatEdge> getNetworkContextFlatEdges(String networkEntityUUID, List<String> genes, int minSize,
			int maxSize, String terminateAt, String direction, String weightproperty) {
		if (genes == null) {
			return null;
		}
		
		List<String> uniqueGenes = genes.stream().distinct().collect(Collectors.toList());
		log.info("Gathering context edges for input: " + uniqueGenes.toString() + " on network with uuid " + networkEntityUUID);
		log.info("TerminateAt is " + (terminateAt != null ? terminateAt : "not given") + ", direction is " + direction + ", sizes are:" + minSize + "/" + maxSize);
		FilterOptions filterOptions = this.mappingNodeService.getFilterOptions(networkEntityUUID);
		String relationShipApocString = this.apocService.getRelationShipOrString(filterOptions.getRelationTypes(), new HashSet<>(), direction);
		Set<String> networkNodeLabels = this.networkService.getNetworkNodeLabels(networkEntityUUID);
		List<FlatEdge> allEdges = new ArrayList<>();
		Set<String> seenEdges = new HashSet<>();
		List<FlatSpecies> geneListFlatSpecies = new ArrayList<>();
		
		// get nodeApocString
		String nodeApocString = this.apocService.getNodeOrString(networkNodeLabels, terminateAt);
		log.info("Node string for path.expand: " + nodeApocString);
		Set<String> geneUUIDSet = new HashSet<>();
		Map<String, String> entityUUIDToDesiredSymbolMap = new HashMap<String, String>(); 
		for (String gene : uniqueGenes) {
			List<FlatSpecies> geneFlatSpecies = this.networkService.getFlatSpeciesOfSymbolInNetwork(networkEntityUUID, gene);
			if (geneFlatSpecies != null && !geneFlatSpecies.isEmpty()) {
				for (FlatSpecies fs : geneFlatSpecies) {
					if (geneUUIDSet.add(fs.getEntityUUID())){
						geneListFlatSpecies.add(fs);
						if (!fs.getSymbol().equals(gene)) {
							// we found a gene, but it only has the symbol we gave in gene as a secondary name
							// we would like to use the symbol we gave as the primary name in the context network
							entityUUIDToDesiredSymbolMap.put(fs.getEntityUUID(), gene);
						}
					}
				}
			}
		}
		if (uniqueGenes.size() < 1 || geneUUIDSet.size() < 1) {
			// should be caught by the controller
			return null;
		} else if (geneUUIDSet.size() == 1) {
			// 3. getContext
			Iterable<ApocPathReturnType> contextNet = this.apocService.pathExpand(geneUUIDSet.iterator().next(), relationShipApocString, nodeApocString, minSize, maxSize);

			this.apocService.extractFlatEdgesFromApocPathReturnType(allEdges, seenEdges, contextNet);
		} else {
			// multi gene context
			// do not use old shared pathway search
			// allEdges = this.getNetworkContextUsingSharedPathwaySearch(networkEntityUUID, geneListFlatSpecies, minSize, maxSize, terminateAt, direction);
			allEdges = this.getMultiGeneNetworkContext(networkEntityUUID, geneListFlatSpecies, minSize, maxSize, terminateAt, direction, weightproperty);
		}
		log.info("Finished calculating context network.");
		if (!entityUUIDToDesiredSymbolMap.isEmpty()) {
			log.info("Replacing symbols of Nodes with found secondaryNames");
			
			for (FlatEdge edge : allEdges) {
				if (entityUUIDToDesiredSymbolMap.isEmpty()) {
					break;
				}
				if (entityUUIDToDesiredSymbolMap.containsKey(edge.getInputFlatSpecies().getEntityUUID())) {
					FlatSpecies species = edge.getInputFlatSpecies();
					String symbol = species.getSymbol();
					String speciesEntityUUID = species.getEntityUUID();
					String desiredSymbol = entityUUIDToDesiredSymbolMap.get(speciesEntityUUID);
					log.debug("Replacing symbol " + symbol + " with " + desiredSymbol + " in species with uuid " + speciesEntityUUID);
					species.addSecondaryName(symbol);
					species.setSymbol(desiredSymbol);
					entityUUIDToDesiredSymbolMap.remove(speciesEntityUUID);
				}
				if (entityUUIDToDesiredSymbolMap.containsKey(edge.getOutputFlatSpecies().getEntityUUID())) {
					FlatSpecies species = edge.getOutputFlatSpecies();
					String symbol = species.getSymbol();
					String speciesEntityUUID = species.getEntityUUID();
					String desiredSymbol = entityUUIDToDesiredSymbolMap.get(speciesEntityUUID);
					log.debug("Replacing symbol " + symbol + " with " + desiredSymbol + " in species with uuid " + speciesEntityUUID);
					species.addSecondaryName(symbol);
					species.setSymbol(desiredSymbol);
					entityUUIDToDesiredSymbolMap.remove(speciesEntityUUID);
				}
			}
		}
		return allEdges;
	}
	
	private List<FlatEdge> getMultiGeneNetworkContext(String networkEntityUUID, List<FlatSpecies> geneListFlatSpecies,
			int minSize, int maxSize, String terminateAt, String direction, String weightproperty) {
		
		MappingNode mappingNode = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
		Set<String> networkNodeLabels = this.networkService.getNetworkNodeLabels(networkEntityUUID);
		
		Set<String> networkRelationTypes = mappingNode.getMappingRelationTypes();
		List<FlatEdge> allEdges = new ArrayList<>();
		List<FlatSpecies> targetSpecies = new ArrayList<>();
		Set<String> targetSpeciesUUID = new HashSet<>();
		Set<String> seenEdges = new HashSet<>();
		if (geneListFlatSpecies.size() < 2) {
			return null;
		} else {
			Collections.sort(geneListFlatSpecies, Comparator.comparing(FlatSpecies::getSymbol));
			boolean foundInitialPath = false;
			FlatSpecies first = null;
			FlatSpecies second = null;
			for (int i = 0; i != geneListFlatSpecies.size()-1; i++) {
				if(foundInitialPath) {
					break;
				} else {
					first = geneListFlatSpecies.get(i);
					for (int j = i+1; j != geneListFlatSpecies.size(); j++) {
						second = geneListFlatSpecies.get(j);
						log.info("Attempting to connect initial genes ("+first.getSymbol() + " and " + second.getSymbol() + ") with shortest path.");
						List<FlatEdge> newEdges = getShortestPathEdges(direction, networkRelationTypes, weightproperty, seenEdges, first, second);
						if (newEdges.isEmpty()) {
							log.info("Failed to connect initial genes ("+first.getSymbol() + " and " + second.getSymbol() + ").");
						} else {
							foundInitialPath = true;
							for (FlatEdge edge : newEdges) {
								if (seenEdges.add(edge.getSymbol())) {
									allEdges.add(edge);
									if (targetSpeciesUUID.add(edge.getInputFlatSpecies().getEntityUUID())) {
										targetSpecies.add(edge.getInputFlatSpecies());
									}
									if (targetSpeciesUUID.add(edge.getOutputFlatSpecies().getEntityUUID())) {
										targetSpecies.add(edge.getOutputFlatSpecies());
									}
								}
							}
							break;
						}
					}
				}
			}
			if (!foundInitialPath) {
				// failed to connect any of the genes, no context can be calculated
				return null; // rather throw an exception here TODO
			} else {
				log.info("Connected gene "+ first.getSymbol() + " and " + second.getSymbol() + " with a shortest path");
				
			}
			// now we have the initial path between the Species first and second
			// also add the contexts around those genes to the target lists and remove them from the geneListFlatSpcecies
			
			// add the context around the first gene to the context net
			String nodeOrString = this.apocService.getNodeOrString(networkNodeLabels, terminateAt);
			Iterable<ApocPathReturnType> firstGeneContextNet = this.apocService.pathExpand(
					first.getEntityUUID(), 
					this.apocService.getRelationShipOrString(networkRelationTypes, new HashSet<>(), direction), 
					nodeOrString,
					minSize, 
					maxSize);
			List<FlatEdge> firstGeneContextFlatEdges = this.apocService.getFlatEdgesFromApocPathReturnTypeWithoutSideeffect(seenEdges, firstGeneContextNet);
			if (firstGeneContextFlatEdges == null) {
				log.warn("Could not generate direct context of gene with symbol: " + first.getSymbol());
			}
			for (FlatEdge targetEdge : firstGeneContextFlatEdges) {
				if (seenEdges.add(targetEdge.getSymbol())) {
					allEdges.add(targetEdge);
				
					if (targetSpeciesUUID.add(targetEdge.getInputFlatSpecies().getEntityUUID())) {
						targetSpecies.add(targetEdge.getInputFlatSpecies());
					}
					if (targetSpeciesUUID.add(targetEdge.getOutputFlatSpecies().getEntityUUID())) {
						targetSpecies.add(targetEdge.getOutputFlatSpecies());
					}
				}
			}
			log.info("Calculated context around gene with symbol: " +first.getSymbol());
			
			// add the context around the second gene to the context net
			Iterable<ApocPathReturnType> secondGeneContextNet = this.apocService.pathExpand(
					second.getEntityUUID(), 
					this.apocService.getRelationShipOrString(networkRelationTypes, new HashSet<>(), direction), 
					nodeOrString,
					minSize, 
					maxSize);
			List<FlatEdge> secondGeneContextFlatEdges = this.apocService.getFlatEdgesFromApocPathReturnTypeWithoutSideeffect(seenEdges, secondGeneContextNet);
			if (secondGeneContextFlatEdges == null) {
				log.warn("Could not generate direct context of gene with symbol: " + first.getSymbol());
			}
			for (FlatEdge targetEdge : secondGeneContextFlatEdges) {
				if (seenEdges.add(targetEdge.getSymbol())) {
					allEdges.add(targetEdge);
				
					if (targetSpeciesUUID.add(targetEdge.getInputFlatSpecies().getEntityUUID())) {
						targetSpecies.add(targetEdge.getInputFlatSpecies());
					}
					if (targetSpeciesUUID.add(targetEdge.getOutputFlatSpecies().getEntityUUID())) {
						targetSpecies.add(targetEdge.getOutputFlatSpecies());
					}
				}
			}
			log.info("Calculated context around gene with symbol: " + second.getSymbol());
			
			// now remove first and second
			geneListFlatSpecies.remove(first);
			geneListFlatSpecies.remove(second);
			
			
			
			// now we connected the first two genes with a shortest path
			// allEdges contains the edges that make up that path
			// targetSpecies contains all the Species on this path (including the first and second species from our named list
			
			// connect the other species to this path and extend the path
			for (FlatSpecies fs: geneListFlatSpecies) {
				int smallestNumberOfEdges = Integer.MAX_VALUE;
				List<FlatEdge> shortestPathEdges = new ArrayList<>();
				for (FlatSpecies ts : targetSpecies) {
					if (smallestNumberOfEdges < 2) {
						break;
					}
					List<FlatEdge> targetSpeciesEdges = getShortestPathEdges(direction, networkRelationTypes, weightproperty,
								seenEdges, fs, ts);
					
					if(targetSpeciesEdges.isEmpty()) continue;
					if (targetSpeciesEdges.size() < smallestNumberOfEdges) {
						smallestNumberOfEdges = targetSpeciesEdges.size();
						shortestPathEdges = targetSpeciesEdges;
					}
				}
				if (shortestPathEdges.size() == 0) {
					log.warn("Unable to connect " + fs.getSymbol() + " to the multi gene context.");
					continue;
				}
				// now shortestPathEdges holds the shortest connection to the already existent context
				// add those edges to our context
				for (FlatEdge targetEdge : shortestPathEdges) {
					if (seenEdges.add(targetEdge.getSymbol())) {
						allEdges.add(targetEdge);
					
						if (targetSpeciesUUID.add(targetEdge.getInputFlatSpecies().getEntityUUID())) {
							targetSpecies.add(targetEdge.getInputFlatSpecies());
						}
						if (targetSpeciesUUID.add(targetEdge.getOutputFlatSpecies().getEntityUUID())) {
							targetSpecies.add(targetEdge.getOutputFlatSpecies());
						}
					}
				}
				log.info("Connected gene "+ fs.getSymbol() + " to the context with a shortest path.");
				
				// add the context around this gene also to the context net
				Iterable<ApocPathReturnType> geneContextNet = this.apocService.pathExpand(
						fs.getEntityUUID(), 
						this.apocService.getRelationShipOrString(networkRelationTypes, new HashSet<>(), direction), 
						this.apocService.getNodeOrString(this.networkService.getNetworkNodeLabels(networkEntityUUID), terminateAt),
						minSize, 
						maxSize);
				List<FlatEdge> geneContextFlatEdges = this.apocService.getFlatEdgesFromApocPathReturnTypeWithoutSideeffect(seenEdges, geneContextNet);
				if (geneContextFlatEdges == null) {
					log.warn("Could not generate direct context of gene with symbol: " + fs.getSymbol());
				}
				for (FlatEdge targetEdge : geneContextFlatEdges) {
					if (seenEdges.add(targetEdge.getSymbol())) {
						allEdges.add(targetEdge);
					
						if (targetSpeciesUUID.add(targetEdge.getInputFlatSpecies().getEntityUUID())) {
							targetSpecies.add(targetEdge.getInputFlatSpecies());
						}
						if (targetSpeciesUUID.add(targetEdge.getOutputFlatSpecies().getEntityUUID())) {
							targetSpecies.add(targetEdge.getOutputFlatSpecies());
						}
					}
				}
				log.info("Calculated context around gene with symbol: " + fs.getSymbol());
			}
		}
		return allEdges;
	}

	private List<FlatEdge> getShortestPathEdges(String direction, Set<String> networkRelationTypes, String weightproperty,
			Set<String> seenEdges, FlatSpecies first, FlatSpecies second) {
		String weight;
		if (weightproperty != null)
			weight = "annotation." + weightproperty;
		else
			weight = "none";
		Iterable<ApocPathReturnType> contextNet = this.apocService.dijkstraWithDefaultWeight(
				first.getEntityUUID(), 
				second.getEntityUUID(), 
				this.apocService.getRelationShipOrString(networkRelationTypes, new HashSet<>(), direction), 
				weight, 
				1.0f);
		List<FlatEdge> newEdges = this.apocService.getFlatEdgesFromApocPathReturnTypeWithoutSideeffect(seenEdges, contextNet);
		return newEdges;
	}

	/**
	 * Calculate the multi-gene context using shared pathway search. 
	 * Looks for the one pathway that contains the most number of genes in the input and uses that as base for the context. 
	 * The other genes get added to that base via APOC Dijkstra searches.
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to search the context in
	 * @param genes The List of genenames to find the context for
	 * @param minSize The minimal step size to take for a context path
	 * @param maxSizeThe maximal step size to take for a context path
	 * @param terminateAtDrug Whether the context should always terminate in a Drug node (yes), or in any node (false). Attn: Requires MyDrug Nodes in the Network
	 * @param direction The direction to expand the context to (one of upstream, downstream, both)
	 * @return List of <a href="#{@link}">{@link FlatEdge}</a> entities that make up the context
	 */ 	
//	private List<FlatEdge> getNetworkContextUsingSharedPathwaySearch(String networkEntityUUID, List<FlatSpecies> genes, int minSize,
//			int maxSize, String terminateAt, String direction){
//		
//		log.debug("Entering method getNetworkContextUsingSharedPathwaySearch with usingSharedPathwaySearch being " + this.sbml4jConfig.getNetworkConfigProperties().isUseSharedPathwaySearch());
//		
// 		List<FlatEdge> allFlatEdges = new ArrayList<>();
//		Set<String> seenEdges = new HashSet<>();
//		Set<String> usedPathwayEntityUUIDSet = new HashSet<>();
//		Set<String> usedGenes = new HashSet<>();
//		//List<String> geneList = new ArrayList<>(genes);
//		List<FlatSpecies> geneListFlatSpecies = new ArrayList<>();
//		Set<String> targetGeneUUIDSet = new HashSet<>();
//		Map<String, Set<String>> pathwayGeneMap = new HashMap<>();
//		MappingNode mappingNode = this.mappingNodeService.findByEntityUUID(networkEntityUUID);
//		Set<String> networkRelationTypes = mappingNode.getMappingRelationTypes();
//		for (FlatSpecies fs : genes) {
//			List<String> genePathways = this.pathwayService.findAllForFlatSpeciesEntityUUIDInMapping(fs.getEntityUUID(), networkEntityUUID);
//			for (String pathwayUUID : genePathways) {
//				if (pathwayGeneMap.containsKey(pathwayUUID)) {
//					pathwayGeneMap.get(pathwayUUID).add(fs.getSymbol());
//				} else {
//					Set<String> pathwayGeneSet = new HashSet<>();
//					pathwayGeneSet.add(fs.getSymbol());
//					pathwayGeneMap.put(pathwayUUID, pathwayGeneSet);
//				}
//			}
//		}
//		
//		// Stop using geneList from here on out
//		// use geneListFlatSpecies, which has the actual FlatSpecies that were found for the genes in geneList (and might differ in amount)
//				
//		
//		// now we have a map for all pathways with their respective genes from our geneset
//		if (sbml4jConfig.getNetworkConfigProperties().isUseSharedPathwaySearch()) {
//			log.error("Requested use of shared pathway search, which is not active in this current build. Please open an issue on Github if you need SharedPathway Search");
//			log.info("Requested use of shared pathway search, which is not active. Proceeding with standard search.");
//			/*
//			// find the pathway that contains most genes / get an ordering of them
//			Map<String, Integer> pathwayGeneNumberMap = new HashMap<>();
//			pathwayGeneMap.forEach((key, value)-> {
//				pathwayGeneNumberMap.put(key, value.size());
//			});
//			
//			boolean isFirst = true;
//			
//			int numberOfPathways = pathwayGeneNumberMap.keySet().size();
//			while (numberOfPathways > 0) {
//				Iterator<Map.Entry<String, Integer> > pathwayGeneNumberIterator = pathwayGeneNumberMap.entrySet().iterator();
//				
//				int biggest = 0;
//				String biggestPW = null;
//				while(pathwayGeneNumberIterator.hasNext()) {
//					String pathway = pathwayGeneNumberIterator.next().getKey();
//					boolean pathwayContainsAtLeastOneGeneStillOfInterest = false;
//					for (FlatSpecies gene : genes) {
//						if (pathwayGeneMap.get(pathway).contains(gene.getSymbol())) {
//							pathwayContainsAtLeastOneGeneStillOfInterest = true;
//							break;
//						}
//					}
//					if(!pathwayContainsAtLeastOneGeneStillOfInterest) {
//						pathwayGeneNumberIterator.remove();
//						numberOfPathways --;
//						pathwayGeneMap.remove(pathway);
//						continue;
//					}
//					
//					if (pathwayGeneNumberMap.get(pathway) > biggest) {
//						biggestPW = pathway;
//						biggest = pathwayGeneNumberMap.get(pathway);
//					} else if (pathwayGeneNumberMap.get(pathway) == biggest && 
//							this.pathwayService.findByEntityUUID(pathway).getPathwayIdString().compareToIgnoreCase(
//													this.pathwayService.findByEntityUUID(biggestPW).getPathwayIdString()) < 0) {
//						biggestPW = pathway;
//						biggest = pathwayGeneNumberMap.get(pathway);
//					}
//				}
//				if (biggest == 1) {
//					// we only have one gene in the biggest pathway 
//					// remove it from our check list
//					pathwayGeneNumberMap.remove(biggestPW);
//					numberOfPathways --;
//					for (FlatSpecies gene : genes) {
//						
//					}
//					if (!geneList.containsAll(pathwayGeneMap.get(biggestPW))) {
//						// is that gene already contained in another pathway, i.e it is not in the genes list anymore
//						// we don't need that pathway anymore
//						pathwayGeneMap.remove(biggestPW);
//					}
//				}
//					
//				if (biggest > 1 && biggestPW != null) {
//					usedPathwayEntityUUIDSet.add(biggestPW);
//					boolean hasConnectingGene = false;
//					for (String gene : pathwayGeneMap.get(biggestPW)) {
//						if (!geneList.remove(gene)) {
//							hasConnectingGene = true;
//						} else {
//							usedGenes.add(gene);
//						}
//					}
//					pathwayGeneNumberMap.remove(biggestPW);
//					numberOfPathways --;
//					if (isFirst || hasConnectingGene) {
//						isFirst = false;
//						pathwayGeneMap.remove(biggestPW);
//					}
//					if (geneList.size() == 0) {
//						break;
//					}
//				}
//			}*/
//		}
//		if (!sbml4jConfig.getNetworkConfigProperties().isUseSharedPathwaySearch() || usedGenes.size() < 1) {
//			// we haven't found a pathway connecting at least two genes.
//			/* Strategy:
//			 * 
//			 * Look at the genes again and order by the number of pathways they are in
//			 * consider that the more pathways a gene is part of, the more important it is.
//			 * Take the top 2 genes in that ranking and find a shortest path between them.
//			 * if all the genes have the same amount of pathways
//			 * ideally pick two that are in a cancer pathway
//			 * for now pick the two that are lexically lowest?
//			 * Then 
//			 */
//			log.debug("Continuing after shared pathway search");
//			Iterator<Map.Entry<String, Set<String>> > pathwayGeneMapIterator = pathwayGeneMap.entrySet().iterator();
//			Map<String, Integer> geneNumberMap = new HashMap<>();
//			while (pathwayGeneMapIterator.hasNext()) {
//				String pathwayEntityUUID = pathwayGeneMapIterator.next().getKey();
//				for (String gene : pathwayGeneMap.get(pathwayEntityUUID)) {
//					if(!geneNumberMap.keySet().contains(gene)) {
//						geneNumberMap.put(gene, 1);
//					} else {
//						int currentNum = geneNumberMap.get(gene).intValue();
//						++currentNum;
//						geneNumberMap.put(gene, currentNum);
//					}
//				}
//			}
//			// possible non-deterministic point here: if more than two pathways are the highest or second highest, it is not determined how the ordering works.
//			// TODO might need additional ordering for that case
//			final Map<String, Integer> sortedByCount = geneNumberMap.entrySet()
//	                .stream()
//	                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
//	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//
//			if (sortedByCount.keySet().size() < 2) {
//				// something went wrong, we should have at least two differnt genes in here
//				// TODO do something
//				log.error("Could not find two different genes after sorting.. Abort..");
//				return null;
//			} else {
//				log.debug("Connecting two most common genes...");
//				// now take the top two genes and get a shortest path between them
//				Iterator<Map.Entry<String, Integer>> sortedByCountIterator = sortedByCount.entrySet().iterator();
//				String gene1UUID = null;
//				String gene2UUID = null;
//				String gene1 = null;
//				String gene2 = null;
//				//boolean isGene1 = true;
//				 if (sortedByCountIterator.hasNext()) {
//					 gene1 = sortedByCountIterator.next().getKey();
//				 }
//				 if (sortedByCountIterator.hasNext()) {
//					 gene2 = sortedByCountIterator.next().getKey();
//				 }
//				 if (gene1 == null ||gene2 == null) {
//					 log.error("Unable to identify two most common genes. This is fatal, aborting..");
//					 return null; // TODO actually throw an exception here
//				 }
//				 log.debug("Gene1: " + gene1 + ", Gene2: " +gene2);
//				 
//				 List<String> gene1UUIDs = this.networkService.getFlatSpeciesEntityUUIDOfSymbolInNetwork(networkEntityUUID,gene1);
//				 if (gene1UUIDs != null && !gene1UUIDs.isEmpty()) {
//					usedGenes.add(gene1);
//					geneList.remove(gene1);
//					for (String foundGene1UUID : gene1UUIDs) {
//						List<String> gene2UUIDs = this.networkService.getFlatSpeciesEntityUUIDOfSymbolInNetwork(networkEntityUUID, gene2);
//						if (gene2UUIDs != null && !gene2UUIDs.isEmpty()) {
//							usedGenes.add(gene2);
//							geneList.remove(gene2);
//							for (String foundGene2UUID : gene2UUIDs) {
//								Iterable<ApocPathReturnType> dijkstra = this.apocService.dijkstraWithDefaultWeight(
//										foundGene1UUID, 
//										foundGene2UUID, 
//										this.apocService.getRelationShipOrString(networkRelationTypes, new HashSet<>(), direction), 
//										"weight", 
//										1.0f);
//								this.apocService.extractFlatEdgesFromApocPathReturnType(allFlatEdges, seenEdges, dijkstra);
//								for (FlatEdge edge : allFlatEdges) {
//									usedGenes.add(edge.getInputFlatSpecies().getSymbol());
//									usedGenes.add(edge.getOutputFlatSpecies().getSymbol());
//								}
//							}
//						} // end if (gene2UUIDs != null && !gene2UUIDs.isEmpty()) {
//						else {
//							log.error("Could not find gene2 with symbol " + gene2 + " in network with entityUUID " + networkEntityUUID);
//							return null; // TODO actually throw an exception here
//						}
//					}
//				 } // end if (gene1UUIDs != null && !gene1UUIDs.isEmpty()) {
//				 else {
//					log.error("Could not find gene1 with symbol " + gene1 + " in network with entityUUID " + networkEntityUUID);
//					return null; // TODO actually throw an exception here
//				 }
//			}
//
//		} else {
//			log.info("Connecting genes with pathways: " + usedGenes.toString());
//				// add a check here if we didn't find a pathway connecting at least two genes. 
//			// what to do then?
//				
//			// now pathwayGeneMap contains all the pathways that hold one or more items and those items have not been dealt with
//			// deal with those
//			// they are not directly connected through pathways to the other genes
//			// 1.a for the pathway they are in, look at the genes and see if those are part of our gene set
//			// 1.b if not, we can try to find those genes in the other pathways
//			// 2.  otherwise do a single source shortest path to the already used genes.
//			
//			Iterator<Map.Entry<String, Set<String>> > 
//	        iterator = pathwayGeneMap.entrySet().iterator(); 
//			
//			while (iterator.hasNext()) {
//				Map.Entry<String, Set<String>> entry = iterator.next();
//				boolean connectedPathway = false;
//				for (String gene : entry.getValue()) {
//					if (usedGenes.contains(gene)) {
//						continue;
//					} else {
//						for (String otherPathwayUUID : usedPathwayEntityUUIDSet) {
//							int numConnectingGenes = this.pathwayService.findNumberOfConnectingGenesForTwoPathwaysOfGeneAndGeneSet(entry.getKey(), otherPathwayUUID, gene, usedGenes);
//							if (numConnectingGenes > 0) {
//								// we found connecting genes, this means that the pathway with pathwayUUID contains a gene that is also contained in the pathway with uuid otherPathwayUUID
//								usedGenes.add(gene);
//								geneList.remove(gene);
//								// one could also do:
//								// don't add the whole pathway to the set, but rather
//								// find the shortest path in that pathway only between the current gene and all known genes.
//								usedPathwayEntityUUIDSet.add(entry.getKey());
//								connectedPathway = true;
//								iterator.remove();
//								break;
//							}
//						}
//						if (connectedPathway) {
//							break;
//						}
//					}
//				}
//			}
//			
//			log.debug("Pathways: " + usedPathwayEntityUUIDSet.toString());
//			
//			// for the pathways in usedPathwayEntityUUIDSet gather all nodes and relationships
//			// possibly using:
//			/*
//			 * match (p:PathwayNode) where p.pathwayIdString = "path_hsa05200" with p match (p)-[w:Warehouse]->(sb:SBase) where w.warehouseGraphEdgeType = "CONTAINS" with [sb.entityUUID] as simpleModelUUIDs MATCH (m:MappingNode)-[wm:Warehouse]->(fs:FlatSpecies) where m.entityUUID = "2f5b5686-c877-4043-9164-1047cf839816" and wm.warehouseGraphEdgeType = "CONTAINS" and fs.simpleModelEntityUUID in simpleModelUUIDs return fs
//			 * This however only gets the nodes and not the relationships to the nodes. But the query also getting the relationships does not return any result
//			 */
//			// suppose we have all nodes and relationships of the pathways (something we should be able to deliver in general)
//			// have a list of them or something
//			
//			for (String pathwayUUID : usedPathwayEntityUUIDSet) {
//				// 1. get all sBase entityUUIDs of the pathway nodes
//				List<String> sBaseUUIDList = this.pathwayService.getSBaseUUIDsOfPathwayNodes(pathwayUUID);
//				// 2. find those pathway uuids in the simpleModelUUIDs of the mapping and return their flatEdges
//				Iterable<FlatEdge> pathwayFlatEdges = this.flatEdgeService.getGeneSetFromSBaseUUIDs(networkEntityUUID, sBaseUUIDList);
//				pathwayFlatEdges.forEach(edge -> {
//					if (!allFlatEdges.contains(edge)) {
//						allFlatEdges.add(edge);
//						seenEdges.add(edge.getSymbol());
//					}
//				});
//			}
//		}
//		log.debug("Connecting remaining genes..");
//		
//		// now we have to connect the remaining genes with shortest paths
//		//List<ApocPathReturnType> allPathReturns = new ArrayList<>();
//		if (geneList.size() > 0) {
//			// yes, these genes
//			/* Don't use gds for now
//			 * if(!this.gdsRepository.gdsGraphExists(networkEntityUUID)) {
//				// There is no gds graph for the network yet
//				MappingNode baseNetwork = this.mappingNodeRepository.findByEntityUUID(networkEntityUUID);
//				String relationshipString = this.getRelationShipOrString(baseNetwork.getMappingRelationTypes(), new HashSet<>());
//				//int nodeCount = this.gdsRepository.createGdsGraphForMapping(networkEntityUUID, networkEntityUUID, relationshipString);
//				int nodeCount = this.gdsRepository.createGdsGraphForMappingUsingQueries(String.format("%s",  networkEntityUUID), this.getGdsNodeQuery(networkEntityUUID), this.getGdsRelationshipQuery(networkEntityUUID, relationshipString)); //(this.buildCreateGdsGraphQuery(networkEntityUUID, networkEntityUUID, relationshipString));
//				if (nodeCount < 1) {
//					// creation did not work
//					logger.error("Creation of gds graph for mapping with uuid " + networkEntityUUID + " failed.");
//				}
//			}*/
//			
//			Set<String> targetNodeUUIDs = new HashSet<>();
//			for (String usedGeneSymbol : usedGenes) {
//				targetNodeUUIDs.addAll(this.networkService.getFlatSpeciesEntityUUIDOfSymbolInNetwork(networkEntityUUID, usedGeneSymbol));
//			}
//			for (String gene : geneList) {
//				boolean isIdenticalNode = false;
//				log.debug("Searching connection for gene: " + gene);
//				List<String> geneEntityUUIDs = this.networkService.getFlatSpeciesEntityUUIDOfSymbolInNetwork(networkEntityUUID, gene);
//				if (geneEntityUUIDs != null && !geneEntityUUIDs.isEmpty()) {
//					for (String geneEntityUUID : geneEntityUUIDs) {
//						//Iterable<ApocPathReturnType> bfsPath = this.gdsRepository.runBFSonGdsGraph(networkEntityUUID, geneEntityUUID, targetNodeUUIDs);
//						//this.extractFlatEdgesFromApocPathReturnType(allFlatEdges, seenEdges, bfsPath);
//						Map<String, List<FlatEdge>> targetToNumEdges = new HashMap<>();
//						for (String targetUUID : targetNodeUUIDs) {
//							if (targetUUID.equals(geneEntityUUID)) {
//								// it is the same gene, we don't need to connect it. 
//								log.debug("Gene " + gene + " is identical to gene with uuid " + targetUUID);
//								isIdenticalNode = true;
//								break;
//							}
//							Iterable<ApocPathReturnType> dijkstra = this.apocService.dijkstraWithDefaultWeight(
//									geneEntityUUID, 
//									targetUUID, 
//									this.apocService.getRelationShipOrString(networkRelationTypes, new HashSet<>(), direction),  
//									"weight", 
//									1.0f);
//							List<FlatEdge> targetFlatEdgeList = new ArrayList<>();
//							this.apocService.extractFlatEdgesFromApocPathReturnTypeWithoutSideeffect(targetFlatEdgeList, seenEdges, dijkstra);
//							targetToNumEdges.put(targetUUID, targetFlatEdgeList);
//						}
//						if(isIdenticalNode) {
//							continue;
//						}
//						int maxNum = 10000000;
//						String lowestTargetGeneUUID = null;
//						for (String targetGeneUUID : targetToNumEdges.keySet()) {
//							int targetNum = targetToNumEdges.get(targetGeneUUID).size();
//							if (targetNum > 0 && targetNum < maxNum) {
//								maxNum = targetNum;
//								lowestTargetGeneUUID = targetGeneUUID;
//							}
//						}
//						if (lowestTargetGeneUUID == null) {
//							log.debug("Failed to connect gene: " + gene + "(uuid: " + geneEntityUUID + ")");
//						} else {
//							log.debug("Lowest (" + maxNum + ") has " + lowestTargetGeneUUID);
//							for (FlatEdge edge : targetToNumEdges.get(lowestTargetGeneUUID)) {
//								if (!allFlatEdges.contains(edge)) {
//									allFlatEdges.add(edge);
//									seenEdges.add(edge.getSymbol());
//								}
//							}
//							targetNodeUUIDs.add(geneEntityUUID);
//						}
//					}
//				} else {
//					log.debug("Could not find gene " + gene);
//				}
//				
//				//bfsPath.forEach(allPathReturns::add);
//			}
//		}
//		// now add the context around the target genes
//		for (String geneFlatSpeciesEntityUUID : targetGeneUUIDSet) {
//			// get the sourroundings of the gene
//			Iterable<ApocPathReturnType> geneContextNet = this.apocService.pathExpand(
//					geneFlatSpeciesEntityUUID, 
//					this.apocService.getRelationShipOrString(networkRelationTypes, new HashSet<>(), direction), 
//					this.apocService.getNodeOrString(this.networkService.getNetworkNodeLabels(networkEntityUUID), terminateAt),
//					minSize, 
//					maxSize);
//			this.apocService.extractFlatEdgesFromApocPathReturnType(allFlatEdges, seenEdges, geneContextNet);
//		}
//		return allFlatEdges;	
//	}

/** GDS Graph Queries ******************************************************************************************/
	
	@SuppressWarnings("unused")
	private String getGdsNodeQuery(String mappingEntityUUID) {
		return String.format("MATCH (m:MappingNode)-[w:Warehouse]->(f:FlatSpecies) "
				+ "  WHERE m.entityUUID = \"%s\" "
				+ "    AND w.warehouseGraphEdgeType = \"CONTAINS\" "
				+ "  RETURN id(f) as id", mappingEntityUUID);
	}
	@SuppressWarnings("unused")
	private String getGdsRelationshipQuery(String mappingEntityUUID, String relationshipString) {
		return String.format("MATCH 	(m:MappingNode)-[w:Warehouse]->(f:FlatSpecies)-[r:%s]-"
				+ "			(f2:FlatSpecies)<-[w2:Warehouse]-(m:MappingNode) "
				+ "	 WHERE m.entityUUID = \"%s\" "
				+ "    AND w.warehouseGraphEdgeType = \"CONTAINS\" "
				+ "    AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
				+ "  RETURN id(f) as source, id(f2) as target", relationshipString, mappingEntityUUID);
	}
	@SuppressWarnings("unused")
	private String buildCreateGdsGraphQuery(String graphName, String baseMappingUUID, String relationshipString) {
		String query = String.format("CALL gds.graph.create.cypher"
				+ "("
				+ "'%s', "
				+ "'MATCH (m:MappingNode)-[w:Warehouse]->(f:FlatSpecies) "
				+ "  WHERE m.entityUUID = %s "
				+ "    AND w.warehouseGraphEdgeType = \"CONTAINS\" "
				+ "  RETURN id(f) as id', "
				+ "'MATCH 	(m:MappingNode)-[w:Warehouse]->(f:FlatSpecies)-[r:%s]-"
				+ "			(f2:FlatSpecies)<-[w2:Warehouse]-(m:MappingNode) "
				+ "	 WHERE m.entityUUID = %s "
				+ "    AND w.warehouseGraphEdgeType = \"CONTAINS\" "
				+ "    AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
				+ "  RETURN id(f) as source), id(f2) as target'"
				+ ") "
				+ "YIELD graphName, nodeCount, relationshipCount, createMillis "
				+ "RETURN nodeCount", graphName, baseMappingUUID, relationshipString, baseMappingUUID);
		//logger.debug(query);
		return query;
	}
}
