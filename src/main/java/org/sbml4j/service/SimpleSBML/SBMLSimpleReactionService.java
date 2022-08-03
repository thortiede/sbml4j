/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
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

import org.sbml4j.model.base.GraphBaseEntity;
import org.sbml4j.model.sbml.SBMLSpecies;
import org.sbml4j.model.sbml.simple.SBMLSimpleReaction;
import org.sbml4j.repository.sbml.simple.SBMLSimpleReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class SBMLSimpleReactionService {

	@Autowired
	SBMLSimpleReactionRepository sbmlSimpleReactionRepository;
	
	/**
	 * Find all <a href="#{@link}">{@link SBMLSimpleReaction}</a> a <a href="#{@link}">{@link SBMLSpecies}</a> is part of
	 * @param speciesEntityUUID The entityUUID of the <a href="#{@link}">{@link SBMLSpecies}</a> 
	 * @return <a href="#{@link}">{@link SBMLSimpleReaction}</a> Collection with the found <a href="#{@link}">{@link SBMLSimpleReactions}</a>
	 */
	public Iterable<SBMLSimpleReaction> findAllReactionsForSpecies(String speciesEntityUUID) {
		return this.sbmlSimpleReactionRepository.findAllReactionsForSpecies(speciesEntityUUID);
	}
	
	
	public Iterable<SBMLSpecies> findAllReactionPartnersOfType(String reactionEntityUUID, String partnerType) {
		return this.sbmlSimpleReactionRepository.findAllReactionPartnersOfType(reactionEntityUUID, partnerType);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link SBMLSimpleReaction}</a> entities in Pathway with UUID provided in pathwayUUID and reaction partners or catalyst that match 
	 * the provided SBO terms
	 * @param pathwayUUID The entityUUID of the <a href="#{@link}">{@link PathwayNode}</a> to which the reaction must belong
	 * @param partnerSBOTerm The SBOTerms of the allowed reaction partners (e.g., SBO:000247)
	 * @return
	 */
	public Iterable<SBMLSimpleReaction> findAllForPathwayWithPartnerSBOTerm(String pathwayUUID, List<String> partnerSBOTerm) {
		return this.sbmlSimpleReactionRepository.findAllInPathwayWithPartnerSBOTerm(pathwayUUID, partnerSBOTerm);
	}
	
	/**
	 * Save an {@link SBMLSimpleReaction} and all {@link GraphBaseEntity} connected to it up to the provided depth
	 * @param reaction The {@link SBMLSimpleReaction} to persist
	 * @param depth The number of relationships to traverse to find {@link GraphBaseEntity} to persist along this {@link SBMLSimpleReaction}
	 * @return The {@link SBMLSimpleReaction} as persisted in the database.
	 */
	public SBMLSimpleReaction save(SBMLSimpleReaction reaction, int depth) {
		return this.sbmlSimpleReactionRepository.save(reaction, depth);
	}

	
	/**
	 * Save a collection of {@link SBMLSimpleReaction} entities and all {@link GraphBaseEntity} connected to it up to the provided depth
	 * @param reactions The collection of {@link SBMLSimpleReaction} to persist
	 * @param depth The number of relationships to traverse to find {@link GraphBaseEntity} to persist along this {@link SBMLSimpleReaction}
	 * @return The Iterable of {@link SBMLSimpleReaction} entities as persisted in the database.
	 */
	public Iterable<SBMLSimpleReaction> save(List<SBMLSimpleReaction> reactions, int depth) {
		return this.sbmlSimpleReactionRepository.save(reactions, depth);
	}

	
	/**
	 * Save a collection of {@link SBMLSimpleReaction} entities
	 * @param reactions The List of {@link SBMLSimpleReaction} to persist
	 * @return Iterable of the persisted {@link SBMLSimpleReaction} entities
	 */
	public Iterable<SBMLSimpleReaction> saveAll(List<SBMLSimpleReaction> reactions) {
		return this.sbmlSimpleReactionRepository.saveAll(reactions);
	}
	
}
