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


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;
import org.tts.model.flat.relationship.CatalystFlatEdge;
import org.tts.model.flat.relationship.ControlFlatEdge;
import org.tts.model.flat.relationship.DephosphorylationFlatEdge;
import org.tts.model.flat.relationship.DissociationFlatEdge;
import org.tts.model.flat.relationship.GlycosylationFlatEdge;
import org.tts.model.flat.relationship.InhibitionFlatEdge;
import org.tts.model.flat.relationship.MethylationFlatEdge;
import org.tts.model.flat.relationship.MolecularInteractionFlatEdge;
import org.tts.model.flat.relationship.NonCovalentBindingFlatEdge;
import org.tts.model.flat.relationship.PathwayFlatEdge;
import org.tts.model.flat.relationship.PhosphorylationFlatEdge;
import org.tts.model.flat.relationship.ProductFlatEdge;
import org.tts.model.flat.relationship.ProteinComplexFormationEdge;
import org.tts.model.flat.relationship.ReactantFlatEdge;
import org.tts.model.flat.relationship.StimulationFlatEdge;
import org.tts.model.flat.relationship.TargetsFlatEdge;
import org.tts.model.flat.relationship.UbiquitinationFlatEdge;
import org.tts.model.flat.relationship.UncertainProcessFlatEdge;
import org.tts.model.flat.relationship.UnknownFromSourceFlatEdge;
import org.tts.repository.flat.FlatEdgeRepository;

/**
 * Handles the creation and persistence of FlatEdge entities
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class FlatEdgeService {

	@Autowired
	FlatEdgeRepository flatEdgeRepository;
	
	/**
	 * Creates a new <a href="#{@link}">{@link FlatEdge}</a> corresponding to the given sboTerm
	 * 
	 * @param sboTermString The sbo-Term string representation of the Edge to be created
	 * @return a subclass of FlatEdge corresponding to the given sboTerm.
	 */
	public FlatEdge createFlatEdge(String sboTermString) {
		FlatEdge newEdge;
		switch (sboTermString) {
			case "SBO:0000180":
				newEdge = new DissociationFlatEdge();
				break;
			case "SBO:0000330":
				newEdge = new DephosphorylationFlatEdge();
				break;
			case "SBO:0000396":
				newEdge = new UncertainProcessFlatEdge();
				break;
			case "SBO:0000177":
				newEdge = new NonCovalentBindingFlatEdge();
				break;
			case "SBO:0000170":
				newEdge = new StimulationFlatEdge();
				break;
			case "SBO:0000217":
				newEdge = new GlycosylationFlatEdge();
				break;
			case "SBO:0000216":
				newEdge = new PhosphorylationFlatEdge();
				break;
			case "SBO:0000169":
				newEdge = new InhibitionFlatEdge();
				break;
			case "SBO:0000224":
				newEdge = new UbiquitinationFlatEdge();
				break;
			case "SBO:0000214":
				newEdge = new MethylationFlatEdge();
				break;
			case "SBO:0000344":
				newEdge = new MolecularInteractionFlatEdge();
				break;
			case "SBO:0000168":
				newEdge = new ControlFlatEdge();
				break;
			case "SBO:0000526":
				newEdge = new ProteinComplexFormationEdge();
				break;
			case "targets":
				newEdge = new TargetsFlatEdge();
				break;
			case "unknownFromSource":
				newEdge = new UnknownFromSourceFlatEdge();
				break;
			case "IS_REACTANT":
				newEdge = new ReactantFlatEdge();
				break;
			case "IS_PRODUCT":
				newEdge = new ProductFlatEdge();
				break;
			case "IS_CATALYST":
				newEdge = new CatalystFlatEdge();
				break;
			case "SHAREDPATHWAY":
				newEdge = new PathwayFlatEdge();
				break;
			default:
				newEdge = new UnknownFromSourceFlatEdge();
				break;
			}
		return newEdge;
	}
	
	/**
	 * Returns the geneSet for nodeUUIDs in network with UUID networkEntityUUID.
	 * Searches for direct connections between pairs of <a href="#{@link}">{@link FlatSpecies}</a> in the List nodeUUIDs
	 * 
	 * @param networkEntityUUID The entityUUID of the network to search in
	 * @param nodeUUIDs List of entityUUIDs of <a href="#{@link}">{@link FlatSpecies}</a> to include
	 * @return Iterable of <a href="#{@link}">{@link FlatEdge}</a> connecting the searched <a href="#{@link}">{@link FlatSpecies}</a>
	 */
	public Iterable<FlatEdge> getGeneSet(String networkEntityUUID, List<String> nodeUUIDs) {
		return this.flatEdgeRepository.getGeneSet(networkEntityUUID.toString(), nodeUUIDs);
	}

	/**
	 * Get an Iterable of <a href="#{@link}">{@link FlatEdge}</a> entities in a Iterable of <a href="#{@link}">{@link MappingNode}</a> using Iterable of <a href="#{@link}">{@link SBMLSBaseEntity}</a> entityUUIDs
	 * @param networkEntityUUID The entityUUID of the <a href="#{@link}">{@link MappingNode}</a>
	 * @param simpleModelUUIDs The entityUUIDs of the <a href="#{@link}">{@link SBMLSBaseEntity}</a> entities to find in the <a href="#{@link}">{@link MappingNode}</a>
	 * @return Iterable of <a href="#{@link}">{@link FlatEdge}</a> entities found
	 */
	public Iterable<FlatEdge> getGeneSetFromSBaseUUIDs(String networkEntityUUID, List<String> simpleModelUUIDs) {
		return this.flatEdgeRepository.getGeneSetFromSBaseUUIDs(networkEntityUUID, simpleModelUUIDs);
	}
	
	/**
	 * Get all <a href="#{@link}">{@link FlatEdge}</a> entities of a network with enitityUUID
 	 * @param entityUUID The entityUUID of a <a href="#{@link}">{@link MappingNode}</a> that contains the requested <a href="#{@link}">{@link FlatEdge}</a> entities
	 * @return Iterable of <a href="#{@link}">{@link FlatEdge}</a> entities
	 */
	public Iterable<FlatEdge> getNetworkFlatEdges(String entityUUID) {
		return this.flatEdgeRepository.getNetworkContentsFromUUID(entityUUID);
	}
	
	/**
	 * Save a collection of <a href="#{@link}">{@link FlatEdge}</a>
	 * @param entities Iterable of <a href="#{@link}">{@link FlatEdge}</a> to persist
	 * @param depth The save depth to use
	 * @return The persisted entities
	 */
	public Iterable<FlatEdge> save(Iterable<FlatEdge> entities, int depth) {
		return this.flatEdgeRepository.save(entities, depth);
	}
	
}
