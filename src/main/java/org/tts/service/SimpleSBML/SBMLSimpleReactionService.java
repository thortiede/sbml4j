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
import org.tts.model.api.Output.MetabolicPathwayReturnType;
import org.tts.repository.simpleModel.SBMLSimpleReactionRepository;

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
	 * @return <a href="#{@link}">{@link MetabolicPathwayReturnType}</a> Collection with the found <a href="#{@link}">{@link SBMLSimpleReactions}</a>
	 */
	public Iterable<MetabolicPathwayReturnType> findAllReactionsForSpecies(String speciesEntityUUID) {
		return this.sbmlSimpleReactionRepository.findAllReactionsForSpecies(speciesEntityUUID);
	}
	
	/**
	 * Find all <a href="#{@link}">{@link SBMLSpecies}</a> that are part of the <a href="#{@link}">{@link SBMLSimpleReaction}</a> with given entityUUID
	 * @param reactionEntityUUID The entityUUID of the <a href="#{@link}">{@link SBMLSimpleReaction}</a>
	 * @return <a href="#{@link}">{@link MetabolicPathwayReturnType}</a> Collection with the found <a href="#{@link}">{@link SBMLSpecies}</a>
	 */
	public Iterable<MetabolicPathwayReturnType> findAllSpeciesForReaction(String reactionEntityUUID) {
		return this.sbmlSimpleReactionRepository.findAllSpeciesForReaction(reactionEntityUUID);
	}
	
}
