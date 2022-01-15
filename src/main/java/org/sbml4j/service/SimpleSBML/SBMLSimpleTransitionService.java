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

import java.util.List;

import org.sbml4j.model.common.GraphBaseEntity;
import org.sbml4j.model.queryResult.NonMetabolicPathwayReturnType;
import org.sbml4j.model.simple.SBMLSimpleTransition;
import org.sbml4j.repository.simpleModel.SBMLSimpleTransitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class SBMLSimpleTransitionService {

	@Autowired
	SBMLSimpleTransitionRepository sbmlSimpleTransitionRepository;
	
	/**
	 * Find all <a href="#{@link}">{@link SBMLSimpleTransitions}</a> a <a href="#{@link}">{@link SBMLSpecies}</a> is part of
	 * @param speciesEntityUUID The entityUUID of a the <a href="#{@link}">{@link SBMLSpecies}</a> to find the transitions for
	 * @return<a href="#{@link}">{@link NonMetabolicPathwayReturnType}</a> containing the Transitions
	 */
	public Iterable<NonMetabolicPathwayReturnType> getTransitionsForSpecies(String speciesEntityUUID) {
		return this.sbmlSimpleTransitionRepository.getTransitionsForSpecies(speciesEntityUUID);
	}
	
	/**
	 * Finds all <a href="#{@link}">{@link SBMLSimpleTransition}</a> entities that are connected to a <a href="#{@link}">{@link PathwayNode}</a>
	 * with a "CONTAINS" - <a href="#{@link}">{@link WarehouseGraphEdge}</a> with the input and output <a href="#{@link}">{@link SBMLQualSpecies}</a>
	 * @param pathwayEntityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return Iterable of <a href="#{@link}">{@link SBMLSimpleTransition}</a> that were found for the given pathway
	 */
	public Iterable<SBMLSimpleTransition> findAllTransitionsInPathway(String pathwayEntityUUID) {
		return this.sbmlSimpleTransitionRepository.findAllInPathway(pathwayEntityUUID);
	}
	
	/**
	 * Finds all <a href="#{@link}">{@link SBMLSimpleTransition}</a> entities that are connected to a <a href="#{@link}">{@link PathwayNode}</a>
	 * with a "CONTAINS" - <a href="#{@link}">{@link WarehouseGraphEdge}</a> with the input and output <a href="#{@link}">{@link SBMLQualSpecies}</a>
	 * @param pathwayEntityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return Iterable of <a href="#{@link}">{@link SBMLSimpleTransition}</a> that were found for the given pathway
	 */
	@Deprecated
	public Iterable<SBMLSimpleTransition> findMatchingTransitionsInPathway(
												String pathwayEntityUUID, 
												List<String> transitionSBOTerms, 
												List<String> nodeSBOTerms) {
		return this.sbmlSimpleTransitionRepository.findMatchingInPathway(pathwayEntityUUID, transitionSBOTerms, nodeSBOTerms);
	}
	
	/**
	 * Finds all <a href="#{@link}">{@link SBMLSimpleTransition}</a> entities that are connected to a <a href="#{@link}">{@link PathwayNode}</a>
	 * with a "CONTAINS" - <a href="#{@link}">{@link WarehouseGraphEdge}</a> with the input and output <a href="#{@link}">{@link SBMLQualSpecies}</a>
	 * @param pathwayEntityUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a>
	 * @return Iterable of <a href="#{@link}">{@link SBMLSimpleTransition}</a> that were found for the given pathway
	 */
	public Iterable<SBMLSimpleTransition> findMatchingTransitionPathsInPathway(
												String pathwayEntityUUID, 
												List<String> transitionSBOTerms, 
												List<String> nodeSBOTerms) {
		return this.sbmlSimpleTransitionRepository.findMatchingPathsInPathway(pathwayEntityUUID, transitionSBOTerms, nodeSBOTerms);
	}
	
	/**
	 * Save the provided {@link SBMLSimpleTransition} and all {@link GraphBaseEntity} connected to it to the provided depth
	 * @param species The {@link Iterable} of {@link SBMLQualSpecies} to be persisted
	 * @param depth The number of relationships to traverse to collect {@link GraphBaseEntity} to persist alongside
	 * @return The {@link Iterable} of the persisted {@link SBMLSimpleTransition} from the database
	 */
	public SBMLSimpleTransition save(SBMLSimpleTransition transition, int depth) {
		return this.sbmlSimpleTransitionRepository.save(transition, depth);
	}
}
