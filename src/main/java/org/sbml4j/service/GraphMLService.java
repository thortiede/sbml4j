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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

public interface GraphMLService {

	ByteArrayOutputStream getGraphMLForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(Iterable<FlatEdge> flatEdges,
			Iterable<FlatSpecies> unconnectedSpecies, boolean directed);
	
	List<FlatSpecies> getFlatSpeciesForGraphML(MultipartFile graphMLFileStream) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException;
	
	List<FlatEdge> getFlatEdgesForGraphML(MultipartFile graphMLFileStream, List<FlatSpecies> speciesOfGraphML) throws FileNotFoundException, IOException;
	

}