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
package org.tts.service.SimpleSBML;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.config.SBML4jConfig;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.common.SBMLSpecies;
import org.tts.model.common.SBMLSpeciesGroup;
import org.tts.repository.common.SBMLSpeciesRepository;


/**
 * Service to handle loading and persisting SBMLSpecies
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class SBMLSpeciesService {
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	@Autowired
	SBMLSpeciesRepository sbmlSpeciesRepository;
	
	/**
	 * Get the <a href="#{@link}">{@link SBMLSpecies}</a> that are part of the group with UUID groupEntityUUID
	 * @param groupEntityUUID The UUID of the  <a href="#{@link}">{@link SBMLSpeciesGroup}</a>
	 * @return
	 */
	public Iterable<SBMLSpecies> getSBMLSpeciesOfGroup(String groupEntityUUID) {
		return this.sbmlSpeciesRepository.getSBMLSpeciesOfGroup(groupEntityUUID);
	}
	
	/**
	 * Find a <a href="#{@link}">{@link SBMLSpecies}</a> by its sBaseName (which must be unique)
	 * 
	 * @param sBaseName The sBaseName attribute of the <a href="#{@link}">{@link SBMLSpecies}</a> to find
	 * @return The SBMLSpecies with sBaseName fetched from the database
	 */
	public SBMLSpecies findBysBaseName(String sBaseName) {
		return this.sbmlSpeciesRepository.findBysBaseName(sBaseName);
		
	}

	/**
	 * Find all <a href="#{@link}">{@link SBMLSpecies}</a> that are connected to an <a href="#{@link}">{@link ExternalResourceEntity}</a> via a <a href="#{@link}">{@link BiomodelsQualifier}</a>.
	 * 
	 * @param name The name of the <a href="#{@link}">{@link ExternalResourceEntity}</a>
	 * @param databaseFromUri The database of the <a href="#{@link}">{@link ExternalResourceEntity}</a>
	 * @return Iterable of <a href="#{@link}">{@link SBMLSpecies}</a> found for the connection
	 */
	public Iterable<SBMLSpecies> findByBQConnectionTo(String name, String databaseFromUri) {
		return this.sbmlSpeciesRepository.findByBQConnectionTo(name, databaseFromUri);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link SBMLSpecies}</a> by a given symbol.
	 * The returned list will contain a {@link SBMLSpeciesService} if it has the symbol as sBaseName.
	 * Additionally the name of <a href="#{@link}">{@link ExternalResourceEntity}</a> connected 
	 * via a <a href="#{@link}">{@link BiomodelsQualifier}</a> is searched 
	 * and the connected <a href="#{@link}">{@link SBMLSpecies}</a> is added to the return set.
	 * @param symbol The symbol to search
	 * @return A Map of EntityUUID Strings to <a href="#{@link}">{@link SBMLSpecies}</a> associated with the given symbol
	 */
	public Map<String, SBMLSpecies> findAllBySymbol(String symbol) {
		return this.findAllBySymbol(symbol, false);
	}
	/**
	 * Find all <a href="#{@link}">{@link SBMLSpecies}</a> by a given symbol.
	 * The returned list will contain a {@link SBMLSpeciesService} if it has the symbol as sBaseName.
	 * Additionally the name of <a href="#{@link}">{@link ExternalResourceEntity}</a> connected 
	 * via a <a href="#{@link}">{@link BiomodelsQualifier}</a> is searched 
	 * and the connected <a href="#{@link}">{@link SBMLSpecies}</a> is added to the return set.
	 * @param symbol The symbol to search
	 * @param searchBQConnection Whether to search through all BQ Connections
	 * @return A Map of EntityUUID Strings to <a href="#{@link}">{@link SBMLSpecies}</a> associated with the given symbol
	 */
	public Map<String, SBMLSpecies> findAllBySymbol(String symbol, boolean searchBQConnections) {
		Map<String, SBMLSpecies> allSpecies = new HashMap<>();
		SBMLSpecies geneSpecies = this.findBysBaseName(symbol);
		if (geneSpecies != null) {
			allSpecies.put(geneSpecies.getEntityUUID(), geneSpecies);
		}
		if(searchBQConnections) {
			Iterable<SBMLSpecies> bqSpecies = this.findByBQConnectionTo(symbol, sbml4jConfig.getExternalResourcesProperties().getBiologicalQualifierProperties().getDefaultDatabase());
			Iterator<SBMLSpecies> bqSpeciesIterator = bqSpecies.iterator();
			while(bqSpeciesIterator.hasNext()) {
				SBMLSpecies current = bqSpeciesIterator.next();
				allSpecies.putIfAbsent(current.getEntityUUID(), current);
			}
		}
		return allSpecies;
	}

	/**
	 * Search <a href="#{@link}">{@link ExternalResourceEntity}</a> secondaryNames and return associated <a href="#{@link}">{@link SBMLSpecies}</a> 
	 * @param name The secondaryName to find
	 * @return The associated <a href="#{@link}">{@link SBMLSpecies}</a> as Map of entityUUID to <a href="#{@link}">{@link SBMLSpecies}</a>
	 */
	public Map<String, SBMLSpecies> findByExternalResourceSecondaryName(String name) {
		Map<String, SBMLSpecies> allSpecies = new HashMap<>();
		for (SBMLSpecies species : this.sbmlSpeciesRepository.findByExternalResourceSecondaryName(name) ) {
			allSpecies.putIfAbsent(species.getEntityUUID(), species);
		}
		return allSpecies;
	}
}
