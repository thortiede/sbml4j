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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.config.SBML4jConfig;
import org.tts.model.common.BiomodelsQualifier;
import org.tts.model.common.ExternalResourceEntity;
import org.tts.model.common.SBMLQualSpecies;
import org.tts.model.common.SBMLQualSpeciesGroup;
import org.tts.repository.common.SBMLQualSpeciesRepository;


/**
 * Service to handle loading and persisting SBMLQualSpecies
 * @author Thorsten Tiede
 *
 * @since 0.2
 */
@Service
public class SBMLQualSpeciesService {
	
	@Autowired
	SBML4jConfig sbml4jConfig;
	
	@Autowired
	SBMLQualSpeciesRepository SBMLQualSpeciesRepository;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * Get the <a href="#{@link}">{@link SBMLQualSpecies}</a> that are part of the group with UUID groupEntityUUID
	 * @param groupEntityUUID The UUID of the  <a href="#{@link}">{@link SBMLQualSpeciesGroup}</a>
	 * @return
	 */
	public Iterable<SBMLQualSpecies> getSBMLQualSpeciesOfGroup(String groupEntityUUID) {
		return this.SBMLQualSpeciesRepository.getSBMLQualSpeciesOfGroup(groupEntityUUID);
	}
	
	/**
	 * Find a <a href="#{@link}">{@link SBMLQualSpecies}</a> by its sBaseName (which must be unique)
	 * 
	 * @param sBaseName The sBaseName attribute of the <a href="#{@link}">{@link SBMLQualSpecies}</a> to find
	 * @return The SBMLQualSpecies with sBaseName fetched from the database
	 */
	public List<SBMLQualSpecies> findBysBaseName(String sBaseName) {
		return this.SBMLQualSpeciesRepository.findBysBaseName(sBaseName);
		
	}

	/**
	 * Find all <a href="#{@link}">{@link SBMLQualSpecies}</a> that are connected to an <a href="#{@link}">{@link ExternalResourceEntity}</a> via a <a href="#{@link}">{@link BiomodelsQualifier}</a>.
	 * 
	 * @param name The name of the <a href="#{@link}">{@link ExternalResourceEntity}</a>
	 * @param databaseFromUri The database of the <a href="#{@link}">{@link ExternalResourceEntity}</a>
	 * @return Iterable of <a href="#{@link}">{@link SBMLQualSpecies}</a> found for the connection
	 */
	public Iterable<SBMLQualSpecies> findByBQConnectionTo(String name, String databaseFromUri) {
		return this.SBMLQualSpeciesRepository.findByBQConnectionTo(name, databaseFromUri);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link SBMLQualSpecies}</a> by a given symbol.
	 * The returned list will contain a {@link SBMLQualSpeciesService} if it has the symbol as sBaseName.
	 * Additionally the name of <a href="#{@link}">{@link ExternalResourceEntity}</a> connected 
	 * via a <a href="#{@link}">{@link BiomodelsQualifier}</a> is searched 
	 * and the connected <a href="#{@link}">{@link SBMLQualSpecies}</a> is added to the return set.
	 * @param symbol The symbol to search
	 * @return A Map of EntityUUID Strings to <a href="#{@link}">{@link SBMLQualSpecies}</a> associated with the given symbol
	 */
	/*public Map<String, SBMLQualSpecies> findAllBySymbol(String symbol) {
		return this.findAllBySymbol(symbol, false);
	}*/
	/**
	 * Find all <a href="#{@link}">{@link SBMLQualSpecies}</a> by a given symbol.
	 * The returned list will contain a {@link SBMLQualSpeciesService} if it has the symbol as sBaseName.
	 * Additionally the name of <a href="#{@link}">{@link ExternalResourceEntity}</a> connected 
	 * via a <a href="#{@link}">{@link BiomodelsQualifier}</a> is searched 
	 * and the connected <a href="#{@link}">{@link SBMLQualSpecies}</a> is added to the return set.
	 * @param symbol The symbol to search
	 * @param searchBQConnection Whether to search through all BQ Connections
	 * @return A Map of EntityUUID Strings to <a href="#{@link}">{@link SBMLQualSpecies}</a> associated with the given symbol
	 */
	/*public Map<String, SBMLQualSpecies> findAllBySymbol(String symbol, boolean searchBQConnections) {
		Map<String, SBMLQualSpecies> allSpecies = new HashMap<>();
		for (SBMLQualSpecies geneSpecies : this.findBysBaseName(symbol)) {
			if (geneSpecies != null) {
				allSpecies.put(geneSpecies.getEntityUUID(), geneSpecies);
			}
			if(searchBQConnections) {
				Iterable<SBMLQualSpecies> bqSpecies = this.findByBQConnectionTo(symbol, sbml4jConfig.getExternalResourcesProperties().getBiologicalQualifierProperties().getDefaultDatabase());
				Iterator<SBMLQualSpecies> bqSpeciesIterator = bqSpecies.iterator();
				while(bqSpeciesIterator.hasNext()) {
					SBMLQualSpecies current = bqSpeciesIterator.next();
					allSpecies.putIfAbsent(current.getEntityUUID(), current);
				}
			}
		}
		return allSpecies;
	}*/

	/**
	 * Search <a href="#{@link}">{@link ExternalResourceEntity}</a> secondaryNames and return associated <a href="#{@link}">{@link SBMLQualSpecies}</a> 
	 * @param name The secondaryName to find
	 * @return The associated <a href="#{@link}">{@link SBMLQualSpecies}</a> as Map of entityUUID to <a href="#{@link}">{@link SBMLQualSpecies}</a>
	 */
	/*public Map<String, SBMLQualSpecies> findByExternalResourceSecondaryName(String name) {
		Map<String, SBMLQualSpecies> allSpecies = new HashMap<>();
		for (SBMLQualSpecies species : this.SBMLQualSpeciesRepository.findByExternalResourceSecondaryName(name) ) {
			allSpecies.putIfAbsent(species.getEntityUUID(), species);
		}
		return allSpecies;
	}
	*/
	/*
	public SBMLQualSpecies findExistingSpeciesToModelSpecies(Species species) {
		
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
		List<SBMLQualSpecies> foundSBMLQualSpecies = new ArrayList<>();
		for (SBMLQualSpecies existingSpecies :
			this.SBMLQualSpeciesRepository.findByBQConnectionWithUriList(cvTermUris)) {
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
			foundSBMLQualSpecies.add(existingSpecies);
		}
		
		if (foundSBMLQualSpecies.size() < 1) {
			logger.debug("Found no matching SBMLQualSpecies for new entity with sbaseId: " + species.getId());
			return null;
		} else if (foundSBMLQualSpecies.size() > 1) {
			logger.warn("Found more than one matching species for new entity with sbaseId:" + species.getId());
			for(SBMLQualSpecies s : foundSBMLQualSpecies) {
				logger.warn("\tSBMLQualSpecies found with uuid:" + s.getEntityUUID());
			}
		}
		return foundSBMLQualSpecies.get(0); // this might not be desired in the future. we might want to be able to work with all found matches
	}*/
}
