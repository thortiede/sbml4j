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

import java.io.ByteArrayOutputStream;

import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;

public interface GraphMLService {

	ByteArrayOutputStream getGraphMLForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(Iterable<FlatEdge> flatEdges,
			Iterable<FlatSpecies> unconnectedSpecies, boolean directed);

}