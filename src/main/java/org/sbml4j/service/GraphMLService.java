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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.sbml4j.Exception.ConfigException;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

public interface GraphMLService {

	ByteArrayOutputStream getGraphMLForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed);

	ByteArrayOutputStream getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(Iterable<FlatEdge> flatEdges,
			Iterable<FlatSpecies> unconnectedSpecies, boolean directed);
	
	Map<String, FlatSpecies> getFlatSpeciesForGraphML(MultipartFile graphMLFileStream) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, ConfigException;
	
	List<FlatEdge> getFlatEdgesForGraphML(MultipartFile graphMLFileStream, Map<String, FlatSpecies> speciesOfGraphML) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, ConfigException ;

	String getGraphIdFromGraphML(MultipartFile graphMLFile)
			throws FileNotFoundException, IOException, ParserConfigurationException, SAXException;

	boolean getIsNetworkDirectedFromGraphML(MultipartFile graphMLFile)
			throws FileNotFoundException, IOException, ParserConfigurationException, SAXException;
	

}