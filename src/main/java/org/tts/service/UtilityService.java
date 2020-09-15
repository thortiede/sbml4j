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

import java.util.HashSet;
import java.util.Set;

import org.sbml.jsbml.ontology.Triple;
import org.springframework.stereotype.Service;

/**
 * Service for utility functions regarding SBO Terms
 * 
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class UtilityService {

	/**
	 * Translate an SBO string (i.e. SBO:000170) to a name representation of that term
	 * Uses the method org.sbml.jsbml.SBO.getTerm to translate but has some exceptions for personalised Terms
	 * 
	 * @param sboString The Term String to translate
	 * @return The translated name
	 */
	public String translateSBOString(String sboString) {
		return (	sboString.equals("") 
				|| 	sboString.equals("unknown") 
				|| 	sboString.equals("undefined in source") 
				|| 	sboString.equals("unknownFromSource")
				) 	? "unknownFromSource" 
					: (sboString.toLowerCase().equals("targets") 
							? "targets" 
							: (sboString.toLowerCase().equals("drug") 
									? 	"drug"
									:	(sboString.equals("PRODUCTOF")
											? "PRODUCTOF"
											: (sboString.equals("REACTANTOF")
													? "REACTANTOF"
													: (sboString.equals("CATALYSES")
															? "CATALYSES"
															: org.sbml.jsbml.SBO.getTerm(sboString).getName()
															)
												)
										)
								)
							
						)
				;
	}
	
	/**
	 * Translate a name (or alias) to the corresponding SBO Term (i.e. SBO:000170)
	 * Uses the methods org.sbml.jsbml.SBO.convertAlias2SBO and org.sbml.jsbml.SBO.intToString to translate
	 * Has exception for the alias "targets"
	 * 
	 * @param alias The name (or alias) to translate
	 * @return The translated SBO Term
	 */
	public String translateToSBOString(String alias) {
		if(alias.equals("targets")) return "targets";
		else {
			return org.sbml.jsbml.SBO.intToString(org.sbml.jsbml.SBO.convertAlias2SBO(alias)) != "" ?
				org.sbml.jsbml.SBO.intToString(org.sbml.jsbml.SBO.convertAlias2SBO(alias)) :
					"undefined in source"; // error prone TODO not all not known aliases should lead to "undefined in source"
		}
	}
	
	/**
	 * Get all child SBO terms of a root sbo term element
	 * Uses the methods org.sbml.jsbml.SBO.getTerm and org.sbml.jsbml.SBO.getTriples to find the child terms
	 *  
	 * @param sboRootTerm The SBO term (i.e. SBO:000170) to find the child terms for
	 * @return A <a href="#{@link}">{@link Set}</a> with all SBO Terms of found children
	 */
	public Set<String> getSBOChildren(String sboRootTerm) {
		
		Set<String> childrenSet = new HashSet<>();
		Set<Triple> children = org.sbml.jsbml.SBO.getTriples(null, org.sbml.jsbml.SBO.getTerm("is_a"), org.sbml.jsbml.SBO.getTerm(sboRootTerm));
		for (Triple triple : children) {
			childrenSet.add(triple.getSubject().getId());
		}
		return childrenSet;
	}
	
	/**
	 * Get all child SBO terms and their child elements of a root sbo term element
	 * Uses the methods org.sbml.jsbml.SBO.getTerm and org.sbml.jsbml.SBO.getTriples to find the child terms
	 *  
	 * @param sboRootTerm The SBO term (i.e. SBO:000170) to find the child terms for
	 * @return A <a href="#{@link}">{@link Set}</a> with all SBO Terms of found children
	 */
	public Set<String> getAllSBOChildren(String sboRootTerm) {
		Set<String> directChildren = getSBOChildren(sboRootTerm);
		Set<String> allChildren = new HashSet<>();
		for (String sboChild : directChildren) {
			allChildren.add(sboChild);
			Set<String> childChildren = getSBOChildren(sboChild);
			for(String childChild : childChildren) {
				allChildren.add(childChild);
			}
		}
		return allChildren;
	}
	
}
