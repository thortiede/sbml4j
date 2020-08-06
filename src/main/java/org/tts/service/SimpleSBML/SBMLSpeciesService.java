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
