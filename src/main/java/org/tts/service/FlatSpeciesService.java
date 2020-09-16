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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.flat.FlatSpecies;
import org.tts.repository.flat.FlatSpeciesRepository;

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

	public FlatSpecies findBySimpleModelEntityUUID(String baseNetworkUUID, String simpleModelGeneEntityUUID) {
		return this.flatSpeciesRepository.findBySimpleModelEntityUUID(simpleModelGeneEntityUUID, baseNetworkUUID);
	}
	
	/**
	 * Persist a List of <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param flatSpeciesList The List of <a href="#{@link}">{@link FlatSpecies}</a> to persist
	 * @return An Iterable of the persisted <a href="#{@link}">{@link FlatSpecies}</a> entities
	 */
	public Iterable<FlatSpecies> persistListOfFlatSpecies(List<FlatSpecies> flatSpeciesList) {
		return this.flatSpeciesRepository.save(flatSpeciesList, 1);
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
	
}
