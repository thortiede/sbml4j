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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.warehouse.PathwayCollectionNode;
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
	 * Find all <a href="#{@link}">{@link PathwayNode}</a> names for a <a href="#{@link}">{@link FlatSpecies}</a> in a <a href="#{@link}">{@link MappingNode}</a>
	 * @param flatSpeciesEntityUUID The entityUUID of the <a href="#{@link}">{@link FlatSpecies}</a>
	 * @param mappingNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @return List of String as Names of pathways
	 */
	public List<String> findAllForFlatSpeciesEntityUUIDInMapping(String flatSpeciesEntityUUID, String mappingNodeEntityUUID) {
		return this.pathwayNodeRepository.findAllForFlatSpeciesEntityUUIDInMapping(flatSpeciesEntityUUID, mappingNodeEntityUUID);
	}
	
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
	
	
	/**
	 * Get a List of UUIDs of pathways associated with a user
	 * @param user The user the pathways are associated with
	 * @param hideCollections Do not include <a href="#{@link}">{@link PathwayCollectionNode}</a> entities (true), or include them (false)
	 * @return List of UUIDs of Pathways associated with the user
	 */
	public List<UUID> getListofPathwayUUIDs(String user, boolean hideCollections) {
		List<UUID> uuids = new ArrayList<>();
		Iterable<PathwayNode> pathways;
		if(hideCollections) {
			pathways = this.pathwayNodeRepository.findNonCollectionPathwaysAttributedToUser(user);
		} else {
			pathways = this.pathwayNodeRepository.findAllPathwaysAttributedToUser(user);
		}
		for (PathwayNode pathwayNode : pathways) {
			uuids.add(UUID.fromString(pathwayNode.getEntityUUID()));
		}
		return uuids;
	}

	/**
	 * Find a <a href="#{@link}">{@link PathwayNode}</a> by its entityUUID
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a> 
	 * @return The found <a href="#{@link}">{@link PathwayNode}</a> 
	 */
	public PathwayNode findByEntityUUID(String entityUUID) {
		return this.pathwayNodeRepository.findByEntityUUID(entityUUID);
	}
	
}
