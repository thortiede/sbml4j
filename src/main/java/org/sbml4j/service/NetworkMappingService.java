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
package org.sbml4j.service;

import org.sbml4j.Exception.NetworkAlreadyExistsException;
import org.sbml4j.Exception.NetworkMappingError;
import org.sbml4j.model.base.GraphEnum.NetworkMappingType;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.model.warehouse.PathwayNode;

public interface NetworkMappingService {
	
	/**
	 * Create a <a href="#{@link}">{@link MappingNode}</a> from a <a href="#{@link}">{@link PathwayNode}</a> by mapping entities
	 * @param pathway The <a href="#{@link}">{@link PathwayNode}</a> to be mapped
	 * @param type The <a href="#{@link}">{@link NetworkMappingType}</a> that should be applied in the mappingProcess
	 * @param activityNode The <a href="#{@link}">{@link ProvenanceGraphActivityNode}</a> this mapping is generated from
	 * @param agentNode The The <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a> that this mapping was attributed to
	 * @param newMappingName A string representing the name of the Mapping to be created (Needs to be checked for uniqueness before passing it in this method)
	 * @return The created <a href="#{@link}">{@link MappingNode}</a> that contains the new Mapping
	 * @throws NetworkAlreadyExistsException If a <a href="#{@link}">{@link MappingNode}</a> with the same name already exists for the <a href="#{@link}">{@link ProvenanceGraphAgentNode}</a>
	 * @throws NetworkMappingError If an error occurs during creation of the entities of the mapping
	 * @throws Exception if an unexpected error occurs
	 */
	public MappingNode createMappingFromPathway(PathwayNode pathway, 
												NetworkMappingType type,
												ProvenanceGraphActivityNode activityNode,
												ProvenanceGraphAgentNode agentNode,
												String newMappingName) throws NetworkAlreadyExistsException, Exception;
	
}
