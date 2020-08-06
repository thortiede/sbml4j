/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
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
	
	public Iterable<FlatSpecies> persistListOfFlatSpecies(List<FlatSpecies> flatSpeciesList) {
		return this.flatSpeciesRepository.save(flatSpeciesList, 1);
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
	 * Save a collection of <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param entities Iterable of <a href="#{@link}">{@link FlatSpecies}</a> to persist
	 * @param depth The save depth to use
	 * @return The persisted entities
	 */
	public Iterable<FlatSpecies> save(Iterable<FlatSpecies> entities, int depth) {
		return this.flatSpeciesRepository.save(entities, depth);
	}
	
}
