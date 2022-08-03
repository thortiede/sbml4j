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
package org.sbml4j.api;

import java.util.ArrayList;
import java.util.List;

import org.sbml4j.model.sbml.simple.SBMLSimpleReaction;
import org.sbml4j.repository.sbml.simple.SBMLSimpleReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
public class TestApi {
	
	@Autowired
	SBMLSimpleReactionRepository sbmlSimpleReactionRepository;
	
	@GetMapping(value = "/reactions", produces = {"application/json"})
	public ResponseEntity<Iterable<SBMLSimpleReaction>> getReactionsTest(@RequestParam(value = "pathwayUUID", required = true) String pathwayUUID) {
		List<String> sboTerms = new ArrayList<String>();
		sboTerms.add("SBO:0000252");
		sboTerms.add("SBO:0000247");
		Iterable<SBMLSimpleReaction> reactions = this.sbmlSimpleReactionRepository.findAllInPathwayWithPartnerSBOTerm(pathwayUUID, sboTerms);
		return new ResponseEntity<Iterable<SBMLSimpleReaction>>(reactions, HttpStatus.OK);
	}
}
