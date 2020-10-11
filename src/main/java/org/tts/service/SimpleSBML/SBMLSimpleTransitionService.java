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
import org.tts.model.api.Output.NonMetabolicPathwayReturnType;
import org.tts.repository.simpleModel.SBMLSimpleTransitionRepository;

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
	
}
