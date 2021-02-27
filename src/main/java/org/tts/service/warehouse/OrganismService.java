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
package org.tts.service.warehouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.Organism;
import org.tts.model.warehouse.WarehouseGraphNode;
import org.tts.repository.common.OrganismRepository;
import org.tts.service.GraphBaseEntityService;

/**
 * Service for handling creation and retrieval of OrgansimNode
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class OrganismService {
	
	@Autowired
	OrganismRepository organismRepository;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	/**
	 * Create a new <a href="#{@link}">{@link Organism}</a> with three letter code orgCode
	 * This will return the existing organsim with orgCode if it already exists and create
	 * a new one otherwise.
	 * @param orgCode The three letter organism Code
	 * @return The <a href="#{@link}">{@link Organism}</a> entity with three letter code orgCode
	 */
	public Organism createOrganism(String orgCode) {
		if(organismExists(orgCode)) { // might want to check the orgCode more thoroughly here
			return getOrgansimByOrgCode(orgCode);
		} else {
			Organism newOrganism = new Organism();
			this.graphBaseEntityService.setGraphBaseEntityProperties(newOrganism);
			newOrganism.setOrgCode(orgCode);
			// TODO: set more attributes here, maybe query webservice
			return this.organismRepository.save(newOrganism);
		}
	}
	/**
	 * Create a new <a href="#{@link}">{@link Organism}</a> with three letter code orgCode
	 * Does not check for existing OrganismNodes, always creates one
	 * @param orgCode The three letter organism Code
	 * @return The <a href="#{@link}">{@link Organism}</a> entity with three letter code orgCode. Entity is not persisted to database
	 */
	public Organism prepareOrganism(String orgCode) {
		Organism newOrganism = new Organism();
		this.graphBaseEntityService.setGraphBaseEntityProperties(newOrganism);
		newOrganism.setOrgCode(orgCode);
		return newOrganism;
	}
	/**
	 * Retrieve an <a href="#{@link}">{@link Organism}</a> from the database
	 * 
	 * @param orgCode The three letter organism code of the <a href="#{@link}">{@link Organism}</a> to get
	 * @return The <a href="#{@link}">{@link Organism}</a> with orgCode
	 */
	public Organism getOrgansimByOrgCode(String orgCode) {
		return organismRepository.findByOrgCode(orgCode);
	}
	
	/**
	 * check if an Organism with orgCode exists in the database
	 * 
	 * @param orgCode The three-letter Organism-code to find
	 * @return true if Organism with orgCode exists in database, false otherwise
	 */
	public boolean organismExists(String orgCode) {
		if(getOrgansimByOrgCode(orgCode) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Find the <a href="#{@link}">{@link Organism}</a> that is attached to a <a href="#{@link}">{@link WarehouseGraphNode}</a> via the FOR relationship
	 * 
	 * @param entityUUID The entityUUID of the <a href="#{@link}">{@link w:WarehouseGraphNode}</a>
	 * @return The <a href="#{@link}">{@link Organism}</a> found using the query
	 */
	public Organism findOrganismForWarehouseGraphNode(String entityUUID) {
		return this.organismRepository.findOrganismForWarehouseGraphNode(entityUUID);
	}
}
