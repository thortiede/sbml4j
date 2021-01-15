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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Species;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
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

	public SBMLSpecies findExistingSpeciesToModelSpecies(Species species) {
		
		List<String> cvTermUris = new ArrayList<>();
		// 1. check bqb_is against external resources 
		for (CVTerm cvTerm : species.getCVTerms()) {
			//createExternalResources(cvTerm, qualSpecies);
			if(cvTerm.getBiologicalQualifierType().equals(Qualifier.BQB_IS) || cvTerm.getBiologicalQualifierType().equals(Qualifier.BQB_HAS_VERSION)) {
				for (String resource : cvTerm.getResources()) {
					cvTermUris.add(resource);
				}
			}
		}
		List<SBMLSpecies> foundSBMLSpecies = new ArrayList<>();
		for (SBMLSpecies existingSpecies :
			this.sbmlSpeciesRepository.findByBQConnectionWithUriList(cvTermUris)) {
			// check compartment
			if(!existingSpecies.getCompartment().getsBaseId().equals(species.getCompartment())) { // this again will not work across different models..
				continue;
			}
			// check sbo term
			if (!(existingSpecies.getsBaseSboTerm() != null && existingSpecies.getsBaseSboTerm().equals(species.getSBOTermID()))) {
				continue;
			}
			// check more if needed
			// check sbasename
			//if (!(existingSpecies.getsBaseId() != null && existingSpecies.getsBaseId().equals(species.getId())))
			// check sbaseid
			
			
			// if we reach here, we found an existing species that has the same properties in regards to what we check as the new species and we can reuse it.
			foundSBMLSpecies.add(existingSpecies);
		}
		
		if (foundSBMLSpecies.size() < 1) {
			logger.debug("Found no matching SBMLSpecies for new entity with sbaseId: " + species.getId());
			return null;
		} else if (foundSBMLSpecies.size() > 1) {
			logger.warn("Found more than one matching species for new entity with sbaseId:" + species.getId());
			for(SBMLSpecies s : foundSBMLSpecies) {
				logger.warn("\tSBMLSpecies found with uuid:" + s.getEntityUUID());
			}
		}
		return foundSBMLSpecies.get(0); // this might not be desired in the future. we might want to be able to work with all found matches
	}
}
