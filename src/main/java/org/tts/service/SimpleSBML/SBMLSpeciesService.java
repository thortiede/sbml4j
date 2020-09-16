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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
	 * Find a SBMLSpecies by its sBaseName (which must be unique)
	 * 
	 * @param sBaseName The sBaseName attribute of the <a href="#{@link}">{@link SBMLSpecies}</a> to find
	 * @return The SBMLSpecies with sBaseName fetched from the database
	 */
	public SBMLSpecies findBysBaseName(String sBaseName) {
		return this.sbmlSpeciesRepository.findBysBaseName(sBaseName);
		
	}

	/**
	 * Find all SBML Species that are connected to an <a href="#{@link}">{@link ExternalResourceEntity}</a> via a <a href="#{@link}">{@link BiomodelsQualifier}</a>.
	 * 
	 * @param name The name of the <a href="#{@link}">{@link ExternalResourceEntity}</a>
	 * @param databaseFromUri The database of the <a href="#{@link}">{@link ExternalResourceEntity}</a>
	 * @return Iterable of <a href="#{@link}">{@link SBMLSpecies}</a> found for the connection
	 */
	public Iterable<SBMLSpecies> findByBQConnectionTo(String name, String databaseFromUri) {
		return this.sbmlSpeciesRepository.findByBQConnectionTo(name, databaseFromUri);
	}
}
