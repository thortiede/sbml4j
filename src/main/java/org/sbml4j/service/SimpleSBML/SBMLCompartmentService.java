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
import org.sbml4j.model.sbml.SBMLCompartment;
import org.sbml4j.repository.sbml.SBMLCompartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SBMLCompartmentService {

	@Autowired
	SBMLCompartmentRepository sbmlCompartmentRepository;
	
	/**
	 * Retrieve all {@link SBMLCompartment}s from the database matching the sBaseId provided
	 * Also retrieves all connected {@link GraphBaseEntity} connected to it up to the depth provided
	 * @param sBaseId The sbaseId to find
	 * @param depth The number of relationships to traverse to collect connected {@link GraphBaseEntity}
	 * @return {@link List} of {@link SBMLCompartment} found
	 */
	public List<SBMLCompartment> findBysBaseId(String sBaseId, int depth) {
		return this.sbmlCompartmentRepository.findBysBaseId(sBaseId, depth);
	}
	
	/**
	 * Persist the provided {@link SBMLCompartment} and all connected entities up to the provided depth
	 * @param compartment The {@link SBMLCompartment} to persist
	 * @param depth The number of relationships to traverse to collected {@link GraphBaseEntity} to be peristed alongside
 	 * @return The persisted {@link SBMLCompartment}
	 */
	public SBMLCompartment save(SBMLCompartment compartment, int depth) {
		return this.sbmlCompartmentRepository.save(compartment, depth);
	}
	
	
}
