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
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.model.common.Organism;
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
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	/**
	 * Create a new <a href="#{@link}">{@link PathwayNode}</a>
	 * @param idString The idString to set
	 * @param nameString The nameString to set
	 * @param org The organism this <a href="#{@link}">{@link PathwayNode}</a> is for
	 * @return The newly created and persisted <a href="#{@link}">{@link PathwayNode}</a>
	 */
	public PathwayNode createPathwayNode(String idString, String nameString, Organism org) {
		PathwayNode newPathwayNode = new PathwayNode();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newPathwayNode);
		newPathwayNode.setOrganism(org);
		newPathwayNode.setPathwayIdString(idString);
		newPathwayNode.setPathwayNameString(nameString);
		return this.pathwayNodeRepository.save(newPathwayNode);
	}
	
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
	 * Find all distinct Species SBO Terms included in a <a href="#{@link}">{@link PathwayNode}</a>
	 * @param pathwayNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return Iterable of Strings containing the SBO Terms
	 */
	public Iterable<String> getAllDistinctSpeciesSboTermsOfPathway(String pathwayNodeEntityUUID) {
		return this.pathwayNodeRepository.getAllDistinctSpeciesSboTermsOfPathway(pathwayNodeEntityUUID);
	}

	/**
	 * Find all distinct Relation SBO Terms included in a <a href="#{@link}">{@link PathwayNode}</a>
	 * @param pathwayNodeEntityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return Iterable of Strings containing the SBO Terms
	 */
	public Iterable<String> getAllDistinctTransitionSboTermsOfPathway(String pathwayNodeEntityUUID) {
		return this.pathwayNodeRepository.getAllDistinctTransitionSboTermsOfPathway(pathwayNodeEntityUUID);
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
	
	/**
	 * Find the number of connecting nodes that share a pathway connection with two input nodes
	 * @param pathway1UUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a> of node 1
	 * @param pathway2UUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a> of node 2
	 * @param geneOfPathway1 The symbol of the node 1
	 * @param genesOfPathway2 The symbol of the node 2
	 * @return number of connection nodes
	 */
	public int findNumberOfConnectingGenesForTwoPathwaysOfGeneAndGeneSet(String pathway1UUID, String pathway2UUID,
			String geneOfPathway1, Set<String> genesOfPathway2) {
		return this.pathwayNodeRepository.findNumberOfConnectingGenesForTwoPathwaysOfGeneAndGeneSet(pathway1UUID, pathway2UUID, geneOfPathway1, genesOfPathway2);
	}
	
	/**
	 * Get a List of entityUUIDs of <a href="#{@link}">{@link SBMLSBaseEntity}</a> that are in a pathway
	 * @param pathwayUUID The entiyUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return List of Strings of entityUUIDs
	 */
	List<String> getSBaseUUIDsOfPathwayNodes(String pathwayUUID) {
		return this.pathwayNodeRepository.getSBaseUUIDsOfPathwayNodes(pathwayUUID);
	}
}
