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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.sbml4j.Exception.ConfigException;
import org.sbml4j.config.SBML4jConfig;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class GraphMLServiceImpl implements GraphMLService {
	@Autowired
	FlatEdgeService flatEdgeService;

	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	ConfigService configService;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SBML4jConfig sbml4jConfig;
	
	public GraphMLServiceImpl() {
		super();
	}

	/**
	 * Public EntryPoint Methods to generate GraphML files from Graph Return Types
	 */
	
	/**
	 * Creates a new ByteArrayOutputStream into which the contents of the FlatEdges is being written in GraphML format
	 * @param flatEdges Iterable\<FlatEdge\> containing the edges to be reported. <br>
	 * 					The start- and endNode of the edge, inputFlatSpecies and outputFlatSpecies respectively, 
	 * 					need to be populated in the edges.
	 * @param directed boolean parameter to denote whether the edges of the reported network are directed
	 * @return ByteArrayOutputStream with the complete contents of the graphML file.
	 */
	@Override
	public ByteArrayOutputStream getGraphMLForFlatEdges(Iterable<FlatEdge> flatEdges, boolean directed) {
		return this.getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(flatEdges, null, directed);
	}
	
	/**
	 * Creates a new ByteArrayOutputStream into which the contents of the FlatEdges is being written in GraphML format
	 * @param flatEdges Iterable\<FlatEdge\> containing the edges to be reported. <br>
	 * 					The start- and endNode of the edge, inputFlatSpecies and outputFlatSpecies respectively, 
	 * 					need to be populated in the edges.
	 * @param unconnectedSpecies Iterable<FlatSpecies> containing Nodes that are not part of any Edge and thus are not contained in the flatEdges.
	 * @param directed boolean parameter to denote whether the edges of the reported network are directed
	 * @return ByteArrayOutputStream with the complete contents of the graphML file.
	 */
	@Override
	public ByteArrayOutputStream getGraphMLForFlatEdgesAndUnconnectedFlatSpecies(Iterable<FlatEdge> flatEdges, Iterable<FlatSpecies> unconnectedSpecies, boolean directed) {
		int nodeId = 0;
		//Set<byte[]> uniqueEdges = new HashSet<>();
		// Map that stores nameOfAnnotation to TypeOfAnnotation -> for generating the list of NodeAnnotations at the beginning of the graph
		Map<String, String> nodeAnnotations = new HashMap<>();
		// Map that stores nameOfAnnotation to TypeOfAnnotation -> for generating the list of EdgeAnnotations at the beginning of the graph
		Map<String, String> edgeAnnotations = new HashMap<>();
		
		Map<String, String> nodeSymbolIdMap = new HashMap<>();
		List<byte[]> nodesWithAnnotation = new ArrayList<>();
		List<byte[]> edgesWithAnnotation = new ArrayList<>();
		
		Set<String> seenEdges = new HashSet<>();
		
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			//start document
			this.initGraphML(stream);
			// parse edges and collect graphML parts
			for(FlatEdge edge : flatEdges) {
				String edgeSymbol = edge.getSymbol();
				if(!seenEdges.contains(edgeSymbol)) {
					seenEdges.add(edgeSymbol);
					String inputSpeciesSymbol = edge.getInputFlatSpecies().getSymbol();
					String outputSpeciesSymbol = edge.getOutputFlatSpecies().getSymbol();
					//String edgeType = edge.getTypeString();
					//logger.debug(edgeSymbol + ": " + inputSpeciesSymbol + "-[" + edgeType + "]->" + outputSpeciesSymbol);
					
					// get Annotations from InputSpecies
					nodeId = this.buildNodeAnnotations(nodeId, nodeAnnotations, nodeSymbolIdMap, nodesWithAnnotation,
							edge.getInputFlatSpecies());
					
					// get Annotations from OutputSpecies					
					nodeId = this.buildNodeAnnotations(nodeId, nodeAnnotations, nodeSymbolIdMap, nodesWithAnnotation,
							edge.getOutputFlatSpecies());
					
					this.buildEdgeAnnotations(edgeAnnotations, nodeSymbolIdMap, edgesWithAnnotation, inputSpeciesSymbol, outputSpeciesSymbol, edge.getAnnotation(), edge.getAnnotationType(), edge.getTypeString());
					/*if (!directed) { // this should not be necessary, as the graph is initialized as undirected in the graphML header.
						// just build the same edge again, but with reversed input and output to have undirected network
						this.buildEdgeAnnotations(edgeAnnotations, nodeSymbolIdMap, edgesWithAnnotation, outputSpeciesSymbol, inputSpeciesSymbol, edge.getAnnotation(), edge.getAnnotationType(), edge.getTypeString());
					}*/
				}
			}
			
			// add unconnected FlatSpecies
			if(unconnectedSpecies != null) {
				for (FlatSpecies species : unconnectedSpecies) {
					nodeId = this.buildNodeAnnotations(nodeId, nodeAnnotations, nodeSymbolIdMap, nodesWithAnnotation, species);
				}
			}
			
			// declare node annotations
			this.declareNodeAnnotations(stream, nodeAnnotations);
			// declare edge annotations
			this.declareEdgeAnnotations(stream, edgeAnnotations);
			// open graph
			this.openGraphForByteArrayOutputStream(stream, directed);
			// write nodes with annotation
			for (byte[] nodeElement : nodesWithAnnotation) {
				stream.write(nodeElement);
			}
			// write edges with annotation
			for (byte[] edgeElement : edgesWithAnnotation) {
				stream.write(edgeElement);
			}
			// close graph
			this.closeGraph(stream);
			// close graphML
			this.closeGraphML(stream);
			
			return stream;
		}catch (IOException e) {
			log.error("Failed to write GraphML ByteArrayOutputStream with" + e.getMessage());
			return new ByteArrayOutputStream(); // TODO Evaluate whether returning an empty stream here is correct or desired
		}
	}
	
	/**
	 * Private Helper Methods creating the different elements of the GraphML file for the ByteArrayOutputStream
	 */
	
	
	/**
	 * Write the initial xml tags for the graphML content and open then graphml-tag
	 * @param stream The stream to write to
	 * @return The ByteArrayOutPutStream that was written to.
	 * @throws IOException Thrown when the bytes could not be written to the stream
	 */
	private ByteArrayOutputStream initGraphML(ByteArrayOutputStream stream) throws IOException {
		stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
		stream.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n".getBytes());
		stream.write("\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n".getBytes());
		stream.write("\t\txsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n".getBytes());
		stream.write("\t\thttp://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n".getBytes());
		return stream;
	}


	/**
	 * Open the graph-element of the graphML file
	 * @param stream The stream to which the element should be written
	 * @param directed Boolean value whether this graph has directed edges (true), or undirected edges (false)
	 * @throws IOException When the the desired bytes cannot be written to the Stream stream
	 */
	private void openGraphForByteArrayOutputStream(ByteArrayOutputStream stream, boolean directed) throws IOException {
		// open graph
		if(directed) {
			stream.write("\t<graph id=\"G\" edgedefault=\"directed\">\n".getBytes());
		} else {
			stream.write("\t<graph id=\"G\" edgedefault=\"undirected\">\n".getBytes());
		}
	}
	
	/**
	 * Close the graph-element of the graphML file
	 * @param stream The stream to which the element should be written
	 * @throws IOException When the desired bytes cannot be written to the Stream stream
	 */
	private void closeGraph(ByteArrayOutputStream stream) throws IOException {
		stream.write("\t</graph>\n".getBytes());	
	}
	
	/**
	 * Close the GraphML-Tag of the graphML file
	 * @param stream The stream the closing tag should be written to
	 * @throws IOException When the the desired bytes cannot be written to the Stream stream
	 */
	private void closeGraphML(ByteArrayOutputStream stream) throws IOException {
		stream.write("</graphml>".getBytes());
	}

	/**
	 * Write the node key annotation declarations to the stream
	 * @param byteArrayOutputStream The stream to which the element should be written
	 * @param nodeAnnotations Map containing the names of the key elements mapped to the types of the elements to be defined
	 * @throws IOException When the desired bytes cannot be written to the Stream stream
	 */
	private void declareNodeAnnotations(ByteArrayOutputStream byteArrayOutputStream, Map<String, String> nodeAnnotations) throws IOException {
		for(String nameString : nodeAnnotations.keySet()) {
			String typeString = nodeAnnotations.get(nameString);
			byteArrayOutputStream.write(String.format("\t<key id=\"v_%s\" for=\"node\" attr.name=\"%s\" attr.type=\"%s\"/>\n", nameString, nameString, typeString.toLowerCase()).getBytes());
		}
	}
	
	/**
	 * Write the edge key annotation declarations to the stream
	 * @param byteArrayOutputStream The stream to which the element should be written
	 * @param nodeAnnotations Map containing the names of the key elements mapped to the types of the elements to be defined
	 * @throws IOException When the desired bytes cannot be written to the Stream stream
	 */
	private void declareEdgeAnnotations(ByteArrayOutputStream byteArrayOutputStream, Map<String, String> edgeAnnotations) throws IOException {
		for(String nameString : edgeAnnotations.keySet()) {
			String typeString = edgeAnnotations.get(nameString);
			byteArrayOutputStream.write(String.format("\t<key id=\"e_%s\" for=\"edge\" attr.name=\"%s\" attr.type=\"%s\"/>\n", nameString, nameString, typeString.toLowerCase()).getBytes());
		}
	}
	
	/**
	 * Build the node-elements of the graphML file including the data elements. Also populates the nodeAnnotations Map
	 * which is being used to declare the occurring data annotations
	 * @param nodeId The current counter for the nodeId which this node should be assigned; gets inreased by one if this is a new node
	 * @param nodeAnnotations Map to hold which annotation name occurs and which type it has
	 * @param nodeSymbolIdMap Map to match the node symbol to the assigned id; used to check if node has already been seen
	 * @param nodesWithAnnotation List containing all byte-Arrays created for this node
	 * @param speciesSymbol The unique symbol of the node
	 * @param nodeAnnotationMap Map holding the annotations of the node with symbol speciesSymbol
	 * @param nodeAnnotationTypeMap Map holding the annotation-types of the node with symbol speciesSymbol
	 * @return the new value of nodeId (unchanged or changed depending of whether the node has been seen before or not, respectively)
	 */
	private int buildNodeAnnotations(int nodeId, Map<String, String> nodeAnnotations,
			Map<String, String> nodeSymbolIdMap, List<byte[]> nodesWithAnnotation, FlatSpecies node) {
		
		Map<String, Object> nodeAnnotationMap = node.getAnnotation();
		Map<String, String> nodeAnnotationTypeMap = node.getAnnotationType();
		String speciesSymbol = node.getSymbol();
		if (!nodeSymbolIdMap.containsKey(speciesSymbol)) {
			String nodeIdString = String.format("n%d", nodeId);
			++nodeId;
			nodeSymbolIdMap.put(speciesSymbol, nodeIdString);
			// the node line itself
			nodesWithAnnotation.add(String.format("\t\t<node id=\"%s\">\n", nodeIdString).getBytes());
			// the always present v_name annotation on nodes
			nodesWithAnnotation.add(String.format("\t\t\t<data key=\"v_name\">%s</data>\n", speciesSymbol).getBytes());
			if(!nodeAnnotations.containsKey("name")) {
				nodeAnnotations.put("name", "String");
			}
			// secondary names
			String secondaryNames = node.getSecondaryNames();
			if (secondaryNames != null && !secondaryNames.isBlank()) {
				nodesWithAnnotation.add(String.format("\t\t\t<data key=\"v_secondaryNames\">%s</data>\n", secondaryNames).getBytes());
				nodeAnnotations.put("secondaryNames", "String");
			}
			// are there more annotations on this node?
			
			if (nodeAnnotationMap != null && nodeAnnotationMap.size() != 0) {
				for (String annotationKey : nodeAnnotationMap.keySet()) {
					String annotationType = nodeAnnotationTypeMap.get(annotationKey).toLowerCase();
					switch (annotationType) {
					case "string":
						String nodeAnnotation = (String) nodeAnnotationMap.get(annotationKey);
						nodesWithAnnotation.add(String.format("\t\t\t<data key=\"v_%s\">%s</data>\n", annotationKey, nodeAnnotation).getBytes());
						if (!nodeAnnotations.containsKey(annotationKey)) {
							nodeAnnotations.put(annotationKey, annotationType);
						}
						break;
					case "boolean":
						boolean nodeAnnotationBool = (boolean) nodeAnnotationMap.get(annotationKey);
						nodesWithAnnotation.add(String.format("\t\t\t<data key=\"v_%s\">%s</data>\n", annotationKey, nodeAnnotationBool).getBytes());
						if(!nodeAnnotations.containsKey(annotationKey)) {
							nodeAnnotations.put(annotationKey, annotationType);
						}
						break;
					case "integer":
						long nodeAnnotationInt = (long) nodeAnnotationMap.get(annotationKey);
						nodesWithAnnotation.add(String.format("\t\t\t<data key=\"v_%s\">%d</data>\n", annotationKey, nodeAnnotationInt).getBytes());
						if (!nodeAnnotations.containsKey(annotationKey)) {
							nodeAnnotations.put(annotationKey, annotationType);
						}
						break;
					case "double":
						double nodeAnnotationFloat = (double) nodeAnnotationMap.get(annotationKey);
						nodesWithAnnotation.add(String.format("\t\t\t<data key=\"v_%s\">%f</data>\n", annotationKey, nodeAnnotationFloat).getBytes());
						if (!nodeAnnotations.containsKey(annotationKey)) {
							nodeAnnotations.put(annotationKey, annotationType);
						}
						break;
						
					default:
						String nodeAnnotationDefault = (String) nodeAnnotationMap.get(annotationKey);
						nodesWithAnnotation.add(String.format("\t\t\t<data key=\"v_%s\">%s</data>\n", annotationKey, nodeAnnotationDefault).getBytes());
						if (!nodeAnnotations.containsKey(annotationKey)) {
							nodeAnnotations.put(annotationKey, annotationType);
						}
						break;
					}
				}
			}
			nodesWithAnnotation.add("\t\t</node>\n".getBytes());
		}
		return nodeId;
	}
	
	/**
	 * Build the edge-elements of the graphML file including the data elements. Also populates the edgeAnnotations Map
	 * which is being used to declare the occurring data annotations
	 * @param edgeAnnotations Map to hold which annotation name occurs and which type it has
	 * @param nodeSymbolIdMap Map to match the node symbol to the assigned id; used to check if node has already been seen
	 * @param edgesWithAnnotation List containing all byte-Arrays created for this edge
	 * @param inputSpeciesSymbol The unique symbol of the startNode of this edge
	 * @param outputSpeciesSymbol The unique symbol of the endNode of this edge
	 * @param edgeAnnotationMap Map holding the annotations of the edge with symbol speciesSymbol
	 * @param edgeAnnotationTypeMap Map holding the annotation-types of the edge with symbol speciesSymbol
	 * @param edgeTypeString the interaction type of the edge that is always being created
	 */
	private void buildEdgeAnnotations(Map<String, String> edgeAnnotations,
			Map<String, String> nodeSymbolIdMap, List<byte[]> edgesWithAnnotation, String inputSpeciesSymbol, String outputSpeciesSymbol,
			Map<String, Object> edgeAnnotationMap, Map<String, String> edgeAnnotationTypeMap, String edgeTypeString) {
		
		edgesWithAnnotation.add(String.format("\t\t<edge source=\"%s\" target=\"%s\">\n\t\t\t<data key=\"e_interaction\">%s</data>\n", nodeSymbolIdMap.get(inputSpeciesSymbol), nodeSymbolIdMap.get(outputSpeciesSymbol), edgeTypeString.toLowerCase()).getBytes());
		if(!edgeAnnotations.containsKey("interaction")) {
			edgeAnnotations.put("interaction", "String");
		}
		boolean hideModelUUIDs = sbml4jConfig.getOutputConfigProperties().isHideModelUUIDs();
		// are there annotations on the edge?
		if (edgeAnnotationMap != null && edgeAnnotationMap.size() != 0) {
			for (String annotationKey : edgeAnnotationMap.keySet()) {
				if (hideModelUUIDs && annotationKey.toLowerCase().contains("uuid")) continue;
				String annotationType = edgeAnnotationTypeMap.get(annotationKey).toLowerCase();
				switch (annotationType) {
				case "string":
					String edgeAnnotation = (String) edgeAnnotationMap.get(annotationKey);
					edgesWithAnnotation.add(String.format("\t\t\t<data key=\"e_%s\">%s</data>\n", annotationKey, edgeAnnotation).getBytes());
					if (!edgeAnnotations.containsKey(annotationKey)) {
						edgeAnnotations.put(annotationKey, annotationType);
					}
					break;
				case "boolean":
					boolean edgeAnnotationBool = (boolean) edgeAnnotationMap.get(annotationKey);
					edgesWithAnnotation.add(String.format("\t\t\t<data key=\"e_%s\">%s</data>\n", annotationKey, edgeAnnotationBool).getBytes());
					if(!edgeAnnotations.containsKey(annotationKey)) {
						edgeAnnotations.put(annotationKey, annotationType);
					}
					break;
				case "integer":
					long edgeAnnotationInt = (long) edgeAnnotationMap.get(annotationKey);
					edgesWithAnnotation.add(String.format("\t\t\t<data key=\"e_%s\">%d</data>\n", annotationKey, edgeAnnotationInt).getBytes());
					if (!edgeAnnotations.containsKey(annotationKey)) {
						edgeAnnotations.put(annotationKey, annotationType);
					}
					break;
				case "double":
					double edgeAnnotationFloat = (double) edgeAnnotationMap.get(annotationKey);
					edgesWithAnnotation.add(String.format("\t\t\t<data key=\"e_%s\">%f</data>\n", annotationKey, edgeAnnotationFloat).getBytes());
					if (!edgeAnnotations.containsKey(annotationKey)) {
						edgeAnnotations.put(annotationKey, annotationType);
					}
					break;
					
				default:
					String edgeAnnotationDefault = (String) edgeAnnotationMap.get(annotationKey);
					edgesWithAnnotation.add(String.format("\t\t\t<data key=\"e_%s\">%s</data>\n", annotationKey, edgeAnnotationDefault).getBytes());
					if (!edgeAnnotations.containsKey(annotationKey)) {
						edgeAnnotations.put(annotationKey, annotationType);
					}
					break;
				}
			}
		}

		edgesWithAnnotation.add(("\t\t</edge>\n").getBytes());
	}

	@Override
	public boolean getIsNetworkDirectedFromGraphML(MultipartFile graphMLFile) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(
				graphMLFile.getBytes());
		Document document = builder.parse(input);
		
		Element root = document.getDocumentElement();
		NodeList nl = root.getChildNodes();
		int nchild = nl.getLength();
		for (int i = 0; i != nchild; i++) {
			Node n = nl.item(i);
			String nodeName = n.getNodeName(); // i.e. graph
			if (nodeName.equals("graph")) {
				if (n.hasAttributes()) {			
					NamedNodeMap nnm = n.getAttributes();
					NodeList edgedefaultNodeList = nnm.getNamedItem("edgedefault").getChildNodes();
					if (edgedefaultNodeList.getLength() < 1) {
						log.error("Graph in graphML file " + graphMLFile.getOriginalFilename() + " does not have expected child node at graph attribute 'edgedefault'");
					} else {
						String directedValue = edgedefaultNodeList.item(0).getNodeValue();
						if (directedValue != null && 
								directedValue.equals("directed")) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
		}
		// if we did not return in the loop, we did not find a edgedefault attribute of the graph
		// TODO: Raise special error.
		return false;
	}
	

	@Override
	public String getGraphIdFromGraphML(MultipartFile graphMLFile) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException {
		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(
				graphMLFile.getBytes());
		Document document = builder.parse(input);
		
		Element root = document.getDocumentElement();
		NodeList nl = root.getChildNodes();
		int nchild = nl.getLength();
		for (int i = 0; i != nchild; i++) {
			Node n = nl.item(i);
			String nodeName = n.getNodeName(); // i.e. graph
			if (nodeName.equals("graph")) {
				if (n.hasAttributes()) {			
					NamedNodeMap nnm = n.getAttributes();
					NodeList edgedefaultNodeList = nnm.getNamedItem("id").getChildNodes();
					if (edgedefaultNodeList.getLength() < 1) {
						log.error("Graph in graphML file " + graphMLFile.getOriginalFilename() + " does not have expected child node at graph attribute 'id'");
					} else {
						return edgedefaultNodeList.item(0).getNodeValue();
					}
				}
			}
		}
		// if we did not return in the loop, we did not find an id attribute of the graph
		// TODO: Raise special error.
		return "G";
	}
	
	
	@Override
	public Map<String, FlatSpecies> getFlatSpeciesForGraphML(MultipartFile graphMLFile) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, ConfigException {
		Map<String, FlatSpecies> graphMLSpecies = new HashMap<>();
		/**FileReader reader = new FileReader(graphMLFile.getResource().getFile());
		BufferedReader bf = new BufferedReader(reader);
		String line;
		while ((line = bf.readLine()) != null) {
			line = line.trim();
			// Header Lines (key)
			
			// annotations
			
			// Nodes -> Species
			if (line.startsWith("<graph")) {
				// beginning of the graph
			}
		}
		*/
		if (!this.configService.isSetGraphMLSpeciesSymbolKey()) {
			log.error("Key for matching Species symbol from GraphML keys has not been set. Please set the 'sbml4j.graphml.speciesSymbolKey' config option to a key in the graphml, or provide a symbol via the API parameter 'symbolKey'!");
			throw new ConfigException("Key for matching Species symbol from GraphML keys has not been set. Please set the 'sbml4j.graphml.speciesSymbolKey' config option to a key in the graphml, or provide a symbol via the API parameter 'symbolKey'!");
		}
		String symbolKey = this.configService.getGraphMLSpeciesSymbolKey();
		boolean isSetGraphMLSboTermKey = this.configService.isSetGraphMLSboTermKey();
		
		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(
				graphMLFile.getBytes());
		Document document = builder.parse(input);
		
		Map<String, ImmutablePair<String, String>> nodeAnnotationMap = new HashMap<>();
		
		Element root = document.getDocumentElement();
		NodeList nl = root.getChildNodes();
		int nchild = nl.getLength();
		for (int i = 0; i != nchild; i++) {
			Node n = nl.item(i);
			String nodeName = n.getNodeName(); // i.e. key
			if (nodeName.equals("key")) {
				Map<String,String> annotationKeyMap = new HashMap<>();

				if (n.hasAttributes()) {
					
					NamedNodeMap nnm = n.getAttributes();
					for (int j = 0; j!= nnm.getLength(); j++) {
						Node annotationItem = nnm.item(j);
						String annotationName = annotationItem.getNodeName();
						String annotationValue = annotationItem.hasChildNodes() ? annotationItem.getFirstChild().getNodeValue() : null;
						if (annotationValue == null) {
							log.warn("Annotation key '" + annotationName + "' has no value associated with it! Ignoring annotation for that key");
						}
						annotationKeyMap.put(annotationName, annotationValue);
					}
					// attr.name needs to be the name of the annotation on the FlatSpecies element
					// attr.type denotes the type of the element on the FlatSpecies element
					// id denotes the data-Node-child-node-value_of_key_node to match the value of the data entry to the correct FlatSpecies annotation element
					// the for-value can be used to distinguish between node and edges should they have the same annotation name -> hence, keep two separate lists
					if (annotationKeyMap.containsKey("for")) {
						if (annotationKeyMap.get("for").equals("node")) {
							// this is a node annotation element
							ImmutablePair<String, String> pair = new ImmutablePair<>(annotationKeyMap.get("attr.name"), annotationKeyMap.get("attr.type"));
							nodeAnnotationMap.put(annotationKeyMap.get("id"), pair);
						}
					}
				}
			}
		}
		// now I have all keys for the annotation and can start to create the FlatSpecies
		for (int i = 0; i != nchild; i++) {
			Node n = nl.item(i);
			String nodeName = n.getNodeName(); // i.e. key
			if (nodeName.equals("graph")) {
				// the children of graph are either nodes or edges
				// process the nodes here
				if (!n.hasChildNodes()) {
					log.error("Graph element in the GraphML file does not have children to process. Is the network empty? Not creating network.");
				}
				NodeList graphChildren = n.getChildNodes();
				for (int j = 0; j != graphChildren.getLength(); j ++) {
					Node currentChild = graphChildren.item(j);
					if (currentChild.getNodeName().equals("node")) {
						log.debug("Creating FlatSpecies");
						FlatSpecies nodeFlatSpecies = new FlatSpecies();
						this.graphBaseEntityService.setGraphBaseEntityProperties(nodeFlatSpecies);
						String graphML_ID = currentChild.getAttributes().getNamedItem("id").getNodeValue();
						this.graphBaseEntityService.addAnnotation(nodeFlatSpecies, "graphML_ID", "string", 
								graphML_ID, false);
						NodeList nodeChildren = currentChild.getChildNodes();
						for (int k = 0; k != nodeChildren.getLength(); k++) {
							Node dataNode = nodeChildren.item(k);
							if (dataNode.getNodeName().equals("data")) {
								// this is an annotation
								String speciesAnnotationName = nodeAnnotationMap.get(dataNode.getAttributes().getNamedItem("key").getNodeValue()).getLeft(); // name
								
								String speciesAnnotationType = nodeAnnotationMap.get(dataNode.getAttributes().getNamedItem("key").getNodeValue()).getRight();
								String speciesAnnotationValue = dataNode.getFirstChild().getNodeValue(); // 8644
								if (speciesAnnotationName.equals(symbolKey)) {
									nodeFlatSpecies.setSymbol(speciesAnnotationValue);
								} else if(isSetGraphMLSboTermKey && speciesAnnotationName.equals(this.configService.getGraphMLSboTermKey())) {
									nodeFlatSpecies.setSboTerm(speciesAnnotationValue);
								} else {
									this.graphBaseEntityService.addAnnotation(nodeFlatSpecies, speciesAnnotationName, speciesAnnotationType, speciesAnnotationValue, false);
								}
							}
						}
						graphMLSpecies.put(graphML_ID, nodeFlatSpecies);
						log.debug("Done");
					}
				}
			}
		}
		
		return graphMLSpecies;
	}

	@Override
	public List<FlatEdge> getFlatEdgesForGraphML(MultipartFile graphMLFile, Map<String, FlatSpecies> speciesOfGraphML) throws FileNotFoundException, IOException, ParserConfigurationException, SAXException, ConfigException {
		
		List<FlatEdge> graphMLEdges = new ArrayList<>();
		boolean isSetGraphMLSboTermKey = this.configService.isSetGraphMLSboTermKey();
		DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		ByteArrayInputStream input = new ByteArrayInputStream(
				graphMLFile.getBytes());
		Document document = builder.parse(input);
		
		Map<String, ImmutablePair<String, String>> edgeAnnotationMap = new HashMap<>();
		
		boolean isSetSymbolKey = this.configService.isSetGraphMLRelationShipSymbolKey();
		String symbolKey = this.configService.getGraphMLRelationshipSymbolKey();
		
		Element root = document.getDocumentElement();
		NodeList nl = root.getChildNodes();
		int nchild = nl.getLength();
		for (int i = 0; i != nchild; i++) {
			Node n = nl.item(i);
			String nodeName = n.getNodeName(); // i.e. key
			if (nodeName.equals("key")) {
				Map<String,String> annotationKeyMap = new HashMap<>();

				if (n.hasAttributes()) {
					
					NamedNodeMap nnm = n.getAttributes();
					for (int j = 0; j!= nnm.getLength(); j++) {
						Node annotationItem = nnm.item(j);
						String annotationName = annotationItem.getNodeName();
						String annotationValue = annotationItem.hasChildNodes() ? annotationItem.getFirstChild().getNodeValue() : null;
						if (annotationValue == null) {
							log.warn("Annotation key '" + annotationName + "' has no value associated with it! Ignoring annotation for that key");
						}
						annotationKeyMap.put(annotationName, annotationValue);
					}
					// attr.name needs to be the name of the annotation on the FlatSpecies element
					// attr.type denotes the type of the element on the FlatSpecies element
					// id denotes the data-Node-child-node-value_of_key_node to match the value of the data entry to the correct FlatSpecies annotation element
					// the for-value can be used to distinguish between node and edges should they have the same annotation name -> hence, keep two separate lists
					if (annotationKeyMap.containsKey("for")) {
						if (annotationKeyMap.get("for").equals("edge")) {
							ImmutablePair<String, String> pair = new ImmutablePair<>(annotationKeyMap.get("attr.name"), annotationKeyMap.get("attr.type"));
							edgeAnnotationMap.put(annotationKeyMap.get("id"), pair);
						}
					}
				}
			}
		}
		// now I have all keys for the annotation and can start to create the FlatEdges
		for (int i = 0; i != nchild; i++) {
			Node n = nl.item(i);
			String nodeName = n.getNodeName(); // i.e. key
			if (nodeName.equals("graph")) {
				// the children of graph are either nodes or edges
				// process the edges here
				if (!n.hasChildNodes()) {
					log.error("Graph element in the GraphML file does not have children to process. Is the network empty? Not creating network.");
				}
				NodeList graphChildren = n.getChildNodes();
				for (int j = 0; j != graphChildren.getLength(); j ++) {
				
					Node currentChild = graphChildren.item(j);
					if (currentChild.getNodeName().equals("edge")) {
						log.debug("Creating FlatEdge");
						FlatEdge edge;
						String sourceNodeId = currentChild.getAttributes().getNamedItem("source").getNodeValue();
						String targetNodeId = currentChild.getAttributes().getNamedItem("target").getNodeValue();
						boolean hasSBOTerm = false;
						String sboTerm = null;
						boolean hasSymbol = false;
						String symbol = null;
						Map<String, ImmutablePair<String,String>> annotationValuesMap = new HashMap<>();
						NodeList nodeChildren = currentChild.getChildNodes();
						for (int k = 0; k != nodeChildren.getLength(); k++) {
							Node dataNode = nodeChildren.item(k);
							if (dataNode.getNodeName().equals("data")) {
								// this is an annotation
								String edgeAnnotationName = edgeAnnotationMap.get(dataNode.getAttributes().getNamedItem("key").getNodeValue()).getLeft(); // name
								
								String edgeAnnotationType = edgeAnnotationMap.get(dataNode.getAttributes().getNamedItem("key").getNodeValue()).getRight();
								String edgeAnnotationValue = dataNode.getFirstChild().getNodeValue(); // 8644
								
								if(isSetGraphMLSboTermKey && edgeAnnotationName.equals(this.configService.getGraphMLSboTermKey())) {
									hasSBOTerm = true;
									sboTerm = edgeAnnotationValue;
								} else if (isSetSymbolKey && edgeAnnotationName.equals(symbolKey)){
									hasSymbol = true;
									symbol = edgeAnnotationValue;
								} else {
									annotationValuesMap.put(edgeAnnotationName, new ImmutablePair<>(edgeAnnotationType, edgeAnnotationValue));
								}
							}
						}
						// now create the FlatEdge
						edge = this.flatEdgeService.createFlatEdge(hasSBOTerm ? sboTerm : "unknown");
						this.graphBaseEntityService.setGraphBaseEntityProperties(edge);
						for (String annotationName : annotationValuesMap.keySet()) {
							this.graphBaseEntityService.addAnnotation(edge, annotationName, 
									annotationValuesMap.get(annotationName).getLeft(), annotationValuesMap.get(annotationName).getRight(), true);
						}
						if (hasSymbol) edge.setSymbol(symbol);
						edge.setInputFlatSpecies(speciesOfGraphML.get(sourceNodeId));
						edge.setOutputFlatSpecies(speciesOfGraphML.get(targetNodeId));
						graphMLEdges.add(edge);
						log.debug("Done");
					}
				}
			}
		}
		return graphMLEdges;
	}
}
