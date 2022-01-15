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
package org.sbml4j.service.SimpleSBML;

import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.common.GraphBaseEntity;
import org.sbml4j.model.common.SBMLQualSpecies;
import org.sbml4j.model.common.SBMLQualSpeciesGroup;
import org.sbml4j.model.sbml.ext.sbml4j.BiomodelsQualifier;
import org.sbml4j.model.sbml.ext.sbml4j.ExternalResourceEntity;
import org.sbml4j.repository.common.SBMLQualSpeciesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
	
	@SuppressWarnings("unused")
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
	 * Save the provided {@link SBMLQualSpecies} and all {@link GraphBaseEntity} connected to it to the provided depth
	 * @param species The {@link Iterable} of {@link SBMLQualSpecies} to be persisted
	 * @param depth The number of relationships to traverse to collect {@link GraphBaseEntity} to persist alongside
	 * @return The {@link Iterable} of the persisted {@link SBMLQualSpecies} from the database
	 */
	public Iterable<SBMLQualSpecies> save(Iterable<SBMLQualSpecies> species, int depth) {
		return this.SBMLQualSpeciesRepository.save(species, depth);
	}
	
	/**
	 * Save the provided {@link SBMLQualSpecies}
	 * Uses the CRUD-Repository method
	 * @param species The {@link Iterable} of {@link SBMLQualSpecies} to be persisted
	 * @return The {@link Iterable} of the persisted {@link SBMLQualSpecies} from the database
	 */
	public Iterable<SBMLQualSpecies> saveAll(Iterable<SBMLQualSpecies> species) {
		return this.SBMLQualSpeciesRepository.saveAll(species);
	}
}
