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
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.warehouse.PathwayNode;
import org.tts.repository.warehouse.PathwayNodeRepository;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
public class PathwayService {

	@Autowired
	PathwayNodeRepository pathwayNodeRepository;
	/**
	 * Get all <a href="#{@link}">{@link MetabolicPathwayReturnType}</a> for reactions in a pathway
	 * 
	 * @param pathwayUUID The UUID of the pathway to search for Reactions
	 * @param nodeSBOTerms The SBOTerms of the nodes that should be included
	 * @return Iterable of <a href="#{@link}">{@link MetabolicPathwayReturnType}</a> containing the reactions of the pathway
	 */
	public Iterable<MetabolicPathwayReturnType> getAllMetabolicPathwayReturnTypes(UUID pathwayUUID, List<String> nodeSBOTerms) {
		return this.pathwayNodeRepository.getAllMetabolicPathwayReturnTypes(pathwayUUID.toString(), nodeSBOTerms);
	}
	
	/**
	 * Get all <a href="#{@link}">{@link NonMetabolicPathwayReturnType}</a> for transitions in a pathway
	 * 
	 * @param pathwayUUID The UUID of the pathway to search for Reactions
	 * @param transitionSBOTerms The SBOTErms of the transitions that should be included
	 * @param nodeSBOTerms The SBOTerms of the nodes that should be included
	 * @return Iterable of <a href="#{@link}">{@link NonMetabolicPathwayReturnType}</a> containing the reactions of the pathway
	 */
	public Iterable<NonMetabolicPathwayReturnType> getAllNonMetabolicPathwayReturnTypes(UUID pathwayUUID, List<String> transitionSBOTerms, List<String> nodeSBOTerms) {
		return this.pathwayNodeRepository.getFlatTransitionsForPathwayUsingSpecies(pathwayUUID.toString(), transitionSBOTerms,nodeSBOTerms);
	}
	
	/**
	 * Get a List of <a href="#{@link}">{@link PathwayNode}</a> that are connected to an <a href="#{@link}">{@link SBMLSBaseEntity}</a>
	 * @param sBaseEntityUUID The UUID of the <a href="#{@link}">{@link SBMLSBaseEntity}</a>
	 * @return List of <a href="#{@link}">{@link PathwayNode}</a> that are connected to the <a href="#{@link}">{@link SBMLSBaseEntity}</a>
	 */
	public List<PathwayNode> getPathwayNodesOfSBase(UUID sBaseEntityUUID) {
		return this.pathwayNodeRepository.getPathwayNodesOfSBase(sBaseEntityUUID.toString());
	}
}
