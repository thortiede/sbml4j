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
package org.tts.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.api.Output.ApocPathReturnType;
import org.tts.model.flat.FlatEdge;
import org.tts.repository.apoc.ApocRepository;

/**
 * Service for handling APOC plugin query requests
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class ApocService {

	@Autowired
	ApocRepository apocRepository;
	
	/**
	 * Extract FlatEdge entities from an Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a>
	 * Adds new <a href="#{@link}">{@link FlatEdge}</a> entities to allEdges and their symbol to seenEdges.
	 * @param allEdges existing List of <a href="#{@link}">{@link FlatEdge}</a> entities to add the extracted <a href="#{@link}">{@link FlatEdge}</a> entities to
	 * @param seenEdges A Set of edge symbols that should not be added to keep the allEdges List unique
	 * @param multiNodeApocPath The Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a> to extract the <a href="#{@link}">{@link FlatEdge}</a> entities from
	 */
	public void extractFlatEdgesFromApocPathReturnType(List<FlatEdge> allEdges, Set<String> seenEdges,
			Iterable<ApocPathReturnType> multiNodeApocPath) {
		Iterator<ApocPathReturnType> iter = multiNodeApocPath.iterator();
		while (iter.hasNext()) {
			ApocPathReturnType current = iter.next();
			for(FlatEdge edge : current.getPathEdges()) {
				if(!seenEdges.contains(edge.getSymbol())) {
					allEdges.add(edge);
					seenEdges.add(edge.getSymbol());
				}
			}
		}
	}

	/**
	 * Extract FlatEdge entities from an Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a>
	 * Keeps the param seenEdges unmodified and only adds new <a href="#{@link}">{@link FlatEdge}</a> entities to allEdges .
	 * 
	 * @param allEdges existing List of <a href="#{@link}">{@link FlatEdge}</a> entities to add the extracted <a href="#{@link}">{@link FlatEdge}</a> entities to
	 * @param seenEdges A Set of edge symbols that should not be added to keep the allEdges List unique
	 * @param multiNodeApocPath The Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a> to extract the <a href="#{@link}">{@link FlatEdge}</a> entities from
	 */
	public void extractFlatEdgesFromApocPathReturnTypeWithoutSideeffect(List<FlatEdge> allEdges, Set<String> seenEdges,
			Iterable<ApocPathReturnType> multiNodeApocPath) {
		Set<String> modifiedSeenEdges = new HashSet<>(seenEdges);
		Iterator<ApocPathReturnType> iter = multiNodeApocPath.iterator();
		while (iter.hasNext()) {
			ApocPathReturnType current = iter.next();
			for(FlatEdge edge : current.getPathEdges()) {
				if(!modifiedSeenEdges.contains(edge.getSymbol())) {
					allEdges.add(edge);
					modifiedSeenEdges.add(edge.getSymbol());
				}
			}
		}
	}
	
	
	public String getNodeOrString(Iterable<String> nodeTypes, boolean terminateAtDrug) {
		StringBuilder sb = new StringBuilder();
		for (String nodeType : nodeTypes) {
			if (nodeType.equals("drug") && terminateAtDrug) {
				sb.append("/");
				sb.append("Drug");
				sb.append("|");
			}	
		}
		sb.append("+FlatSpecies");
		return sb.toString();
	}
	

	public String getRelationShipOrString(Iterable<String> relationshipTypes, Set<String> exclude, String direction) {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (String relationType : relationshipTypes) {
			if (!isFirst) {
				sb.append("|");
			}
			if (!exclude.contains(relationType)) {
				if(direction.equals("upstream")) {
					sb.append("<");
				}
				isFirst = false;
				sb.append(relationType);
				if(direction.equals("downstream")) {
					sb.append(">");
				}	
			}
		}
		return sb.toString();
	}
	
	/**
	 * Run the DijkstraWithDefaultWeight algorithm from the APOC plugin
	 * @param startNodeEntityUUID The entityUUID of the startNode of the algorithm
	 * @param endNodeEntityUUID The entityUUID of the endNode of the algorithm
	 * @param relationTypesApocString apocRelationshipString the relationshipString of the algorithm
	 * @param propertyName The propertyName on the relationships to use as weight
	 * @param defaultWeight The default weight to use of the property is not present
	 * @return Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a>
	 */
	Iterable<ApocPathReturnType> dijkstraWithDefaultWeight(String startNodeEntityUUID, String endNodeEntityUUID, 
			String relationTypesApocString, String propertyName, float defaultWeight) {
		return this.apocRepository.apocDijkstraWithDefaultWeight(startNodeEntityUUID, endNodeEntityUUID, relationTypesApocString, propertyName, defaultWeight);
	}
	
	/**
	 * Run the path.expand algorithm from the APOC plugin
	 * @param startNodeEntityUUID entityUUID of the startNode for path.expand
	 * @param apocRelationshipString the relationshipString for path.expand
	 * @param apocNodeString the nodeString for path.expand 
	 * @param minDepth the minDepth for path.expand
	 * @param maxDepth the maxDepth for path.expand
	 * @return Iterable of <a href="#{@link}">{@link ApocPathReturnType}</a>
	 */
	Iterable<ApocPathReturnType> pathExpand(String startNodeEntityUUID, String apocRelationshipString, String apocNodeString, int minDepth, int maxDepth) {
		return this.apocRepository.runApocPathExpandFor(startNodeEntityUUID, apocRelationshipString, apocNodeString, minDepth, maxDepth);
	}
	
	
}
