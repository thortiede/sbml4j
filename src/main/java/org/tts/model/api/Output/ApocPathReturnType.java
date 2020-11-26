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
package org.tts.model.api.Output;

import java.util.List;

import org.springframework.data.neo4j.annotation.QueryResult;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;

@QueryResult
public class ApocPathReturnType {

	private List<FlatSpecies> pathNodes;
	
	private List<FlatEdge> pathEdges;

	public List<FlatSpecies> getPathNodes() {
		return pathNodes;
	}

	public void setPathNodes(List<FlatSpecies> pathNodes) {
		this.pathNodes = pathNodes;
	}

	public List<FlatEdge> getPathEdges() {
		return pathEdges;
	}

	public void setPathEdges(List<FlatEdge> pathEdges) {
		this.pathEdges = pathEdges;
	}
}
