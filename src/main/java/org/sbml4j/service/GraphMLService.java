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

import java.io.ByteArrayOutputStream;

import org.tts.model.api.Output.ApocPathReturnType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;

public interface GraphMLService {

	ByteArrayOutputStream getGraphMLForApocPathReturn(Iterable<ApocPathReturnType> apocPathReturn, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(Iterable<FlatEdge> flatEdges,
			Iterable<FlatSpecies> unconnectedSpecies, boolean directed);

}