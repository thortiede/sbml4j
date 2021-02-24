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
package org.tts.service.networks;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.service.GraphMLService;

/**
 * This service class handles requests from controllers to get Resources containing networks
 * It depends on resource-creating services (like graphMLService) and network-extracting services
 * like networkService.
 */
@Service
public class NetworkResourceService {

	@Autowired 
	GraphMLService graphMLService;
	
	@Autowired
	NetworkService networkService;
	
	Logger log = LoggerFactory.getLogger(NetworkResourceService.class);
	
	/**
	 * Delegates the extraction of network nodes and relationships and the subsequently
	 * the generation of a graphML-Resource containing those nodes and relationships.
	 * 
	 * @param uuid The uuid of the network to extract
	 * @param geneSet The set of node symbols (here genes-symbols) that should be extracted with their directly connecting relationships
	 * @param directed Option whether the resulting graphML-Graph should be directed (true) or undirected (false)
	 * @return ByteArrayResource containing the GraphML representation of the network
	 */
	public Resource getNetwork(String uuid, List<String> geneSet, boolean directed) {
		
		// 1. Translate the symbols in geneSet into simplemodel entityUUIDs of network with UUID uuid
		List<String> flatSpeciesUUIDs = new ArrayList<>();
		for (String geneSymbol : geneSet) {
			List<String> speciesUUIDs = this.networkService.getFlatSpeciesEntityUUIDOfSymbolInNetwork(uuid, geneSymbol);
			if(speciesUUIDs != null && !speciesUUIDs.isEmpty()) {
				flatSpeciesUUIDs.addAll(speciesUUIDs);
			} else {
				log.warn("Could not find FlatSpecies for symbol " + geneSymbol + " in network (" + uuid + ")");
			}
		}
		
		Iterable<FlatSpecies> geneSetFlatSpecies = this.networkService.getNetworkNodes(flatSpeciesUUIDs);
		Iterable<FlatEdge> geneSetFlatEdges = this.networkService.getGeneSet(uuid, flatSpeciesUUIDs);
		ByteArrayOutputStream graphMLStream = getByteArrayOutputStreamOfNetworkContents(directed, geneSetFlatSpecies,
				geneSetFlatEdges);
		return new ByteArrayResource(graphMLStream.toByteArray(), "geneset.graphml");
	}

	/**
	 * Delegates the extraction of network nodes and relationships and the subsequently
	 * the generation of a graphML-Resource containing those nodes and relationships.
	 * 
	 * @param uuid The uuid of the network to extract
	 * @param geneSet The set of node symbols (here genes-symbols) that should be extracted with their directly connecting relationships
	 * @return ByteArrayResource containing the GraphML representation of the network
	 */
	public Resource getNetwork(String uuid, boolean directed) {
		Iterable<FlatSpecies> networkSpecies = this.networkService.getNetworkNodes(uuid);
		Iterable<FlatEdge> networkEdges = this.networkService.getNetworkRelations(uuid);
		ByteArrayOutputStream graphMLStream = getByteArrayOutputStreamOfNetworkContents(directed, networkSpecies,
				networkEdges);
		return new ByteArrayResource(graphMLStream.toByteArray(), "network_" + uuid + ".graphml");
	}
	
	/**
	 * Creates a ByteArrayOutputResource for a set of <a href="#{@link}">{@link FlatEdge}</a> entities 
	 * @param flatEdges The <a href="#{@link}">{@link FlatEdge}</a> entities  to build the resource from
	 * @param directed Whether the resulting graphML Graph should be directed
	 * @param filename The filename for the Resource
	 * @return ByteArrayResource containing the graphML graph
	 */
	public Resource getNetworkForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed, String filename) {
		ByteArrayOutputStream graphMLStream = getByteArrayOutputStreamOfNetworkContents(directed, null,
				flatEdges);
		return new ByteArrayResource(graphMLStream.toByteArray(), filename);
	}
	
	/**
	 * Create a ByteArrayOutputStream from FlatEdge and FlatSpecies entities in graphML format
	 * @param directed whether the Graph should be directed (true) or not (false)
	 * @param flatSpecies Iterable of <a href="#{@link}">{@link FlatSpecies}</a> entities 
	 * @param flatEdges Iterable of <a href="#{@link}">{@link FlatEdge}</a> entities 
	 * @return ByteArrayOutputStream with GraphML content
	 */
	private ByteArrayOutputStream getByteArrayOutputStreamOfNetworkContents(boolean directed,
			Iterable<FlatSpecies> flatSpecies, Iterable<FlatEdge> flatEdges) {
		Set<String> seenSymbols = new HashSet<>();
		Iterator<FlatEdge> edgeIter = flatEdges.iterator();
		List<FlatSpecies> unconnectedSpecies = new ArrayList<>();
		while(edgeIter.hasNext()) {
			FlatEdge current = edgeIter.next();
			seenSymbols.add(current.getInputFlatSpecies().getSymbol());
			seenSymbols.add(current.getOutputFlatSpecies().getSymbol());
		}
		if (flatSpecies != null) {
			Iterator<FlatSpecies> speciesIter = flatSpecies.iterator();
			while (speciesIter.hasNext()) {
				FlatSpecies current = speciesIter.next();
				if(!seenSymbols.contains(current.getSymbol())) {
					unconnectedSpecies.add(current);
				}
			}
		}
		ByteArrayOutputStream graphMLStream;
		if(unconnectedSpecies.size() == 0) {
			graphMLStream = this.graphMLService.getGraphMLForFlatEdges(flatEdges, directed);
			
		} else {
			for (FlatSpecies species : unconnectedSpecies) {
				log.debug("Found unconnected species in geneset: " + species.getSymbol() + ": " + species.getEntityUUID());
			}
			graphMLStream = this.graphMLService.getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(flatEdges, unconnectedSpecies, directed);
			
		}
		return graphMLStream;
	}
	
}
