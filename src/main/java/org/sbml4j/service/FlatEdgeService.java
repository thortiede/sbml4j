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
package org.sbml4j.service;


import java.util.List;

import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.model.flat.relationship.CatalystFlatEdge;
import org.sbml4j.model.flat.relationship.ControlFlatEdge;
import org.sbml4j.model.flat.relationship.DephosphorylationFlatEdge;
import org.sbml4j.model.flat.relationship.DissociationFlatEdge;
import org.sbml4j.model.flat.relationship.GlycosylationFlatEdge;
import org.sbml4j.model.flat.relationship.InhibitionFlatEdge;
import org.sbml4j.model.flat.relationship.MethylationFlatEdge;
import org.sbml4j.model.flat.relationship.MolecularInteractionFlatEdge;
import org.sbml4j.model.flat.relationship.NonCovalentBindingFlatEdge;
import org.sbml4j.model.flat.relationship.PathwayFlatEdge;
import org.sbml4j.model.flat.relationship.PhosphorylationFlatEdge;
import org.sbml4j.model.flat.relationship.ProductFlatEdge;
import org.sbml4j.model.flat.relationship.ProteinComplexFormationEdge;
import org.sbml4j.model.flat.relationship.ReactantFlatEdge;
import org.sbml4j.model.flat.relationship.StimulationFlatEdge;
import org.sbml4j.model.flat.relationship.TargetsFlatEdge;
import org.sbml4j.model.flat.relationship.UbiquitinationFlatEdge;
import org.sbml4j.model.flat.relationship.UncertainProcessFlatEdge;
import org.sbml4j.model.flat.relationship.UnknownFromSourceFlatEdge;
import org.sbml4j.repository.flat.FlatEdgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	public List<FlatEdge> getNetworkFlatEdges(String entityUUID) {
		List<FlatEdge> flatEdges = this.flatEdgeRepository.getNetworkContentsFromUUID(entityUUID);
		List<FlatEdge> selfRelations = this.flatEdgeRepository.getSelfRelationsFromUUID(entityUUID);
		selfRelations.forEach(flatEdges::add);
		
		return flatEdges;
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
