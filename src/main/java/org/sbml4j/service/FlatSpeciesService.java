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
import java.util.List;

import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.repository.flat.FlatSpeciesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for persisting and loading flatSpecies from the database
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */

@Service
public class FlatSpeciesService {
	
	@Autowired
	FlatSpeciesRepository flatSpeciesRepository;
	
	/**
	 * Delete a List of <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param speciesToDelete The List of <a href="#{@link}">{@link FlatSpecies}</a> to delete
	 * @return true if the <a href="#{@link}">{@link FlatSpecies}</a> were deleted, false if an error occured
	 */
	public boolean deleteAll(List<FlatSpecies> speciesToDelete) {
		try {
			this.flatSpeciesRepository.deleteAll(speciesToDelete);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Search for all <a href="#{@link}">{@link FlatSpecies}</a> entities having the given nodeSymbol in the network
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to search in
	 * @param nodeSymbol The symbol of the <a href="#{@link}">{@link FlatSpecies}</a> entity
	 * @return Iterable of <a href="#{@link}">{@link FlatSpecies}</a> having the given nodeSymbol in the symbol property
	 */
	public Iterable<FlatSpecies> findAllNetworkNodesForSymbol(String networkEntityUUID, String nodeSymbol){
		List<String> nodeSymbols = new ArrayList<>();
		nodeSymbols.add(nodeSymbol);
		return this.findAllNetworkNodesForSymbols(networkEntityUUID, nodeSymbols);
	}
	
	/**
	 * Search for all <a href="#{@link}">{@link FlatSpecies}</a> entities having the given nodeSymbols in the network
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to search in
	 * @param nodeSymbols List of symbols of the <a href="#{@link}">{@link FlatSpecies}</a> entity
	 * @return Iterable of <a href="#{@link}">{@link FlatSpecies}</a> having the given nodeSymbol in the symbol property
	 */
	public Iterable<FlatSpecies> findAllNetworkNodesForSymbols(String networkEntityUUID, List<String> nodeSymbols){
		return this.flatSpeciesRepository.getNetworkNodes(networkEntityUUID, nodeSymbols);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link FlatSpecies}</a> entities that are unconnected to any other <a href="#{@link}">{@link FlatSpecies}</a> in the network
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to search in
	 * @return Iterable of <a href="#{@link}">{@link FlatSpecies}</a> found
	 */
	public Iterable<FlatSpecies> findAllUnconnectedSpeciesInNetwork(String networkEntityUUID) {
		return this.flatSpeciesRepository.getUnconnectedNetworkNodes(networkEntityUUID);
	}
	
	/**
	 * Find <a href="#{@link}">{@link FlatSpecies}</a> by its entityUUID
	 * 
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link FlatSpecies}</a> to find
	 * @return The FlatSpecies with entityUUID
	 */
	public FlatSpecies findByEntityUUID(String entityUUID) {
		return this.flatSpeciesRepository.findByEntityUUID(entityUUID);
	}
	
	/**
	 * Find the entityUUID of <a href="#{@link}">{@link FlatSpecies}</a> with symbol geneSymbol
	 * @param networkEntityUUID The UUID of the network to search in
	 * @param geneSymbol The symbol of the <a href="#{@link}">{@link FlatSpecies}</a> to find
	 * @return The UUID of the <a href="#{@link}">{@link FlatSpecies}</a> found
	 */
	public String findEntityUUIDForSymbolInNetwork(String networkEntityUUID, String geneSymbol) {
		return this.flatSpeciesRepository.findEntityUUIDForSymbolInNetwork(networkEntityUUID.toString(), geneSymbol);
	}

	/**
	 * Save a collection of <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param entities Iterable of <a href="#{@link}">{@link FlatSpecies}</a> to persist
	 * @param depth The save depth to use
	 * @return The persisted entities
	 */
	public Iterable<FlatSpecies> save(Iterable<FlatSpecies> entities, int depth) {
		return this.flatSpeciesRepository.save(entities, depth);
	}

	/**
	 * Find all <a href="#{@link}">{@link FlatSpecies}</a> in network of <a href="#{@link}">{@link MappingNode}</a> with given entityUUID
	 * whose property secondaryNames contains the given nodeSymbol String .
	 * 
	 * Requires that there is a Fulltext-index defined on the property secondaryNames of <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a> to search in
	 * @param nodeSymbol The String to find
	 * @return List of <a href="#{@link}">{@link FlatSpecies}</a> having the String nodeSymbol in their secondaryNames String property
	 */
	public List<FlatSpecies> findAllBySecondaryName(String networkEntityUUID, String nodeSymbol) {
		return this.flatSpeciesRepository.findAllBySecondaryName(networkEntityUUID, nodeSymbol);
	}
	
}
