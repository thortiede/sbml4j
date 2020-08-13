package org.tts.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.config.SBML4jConfig;
import org.tts.model.api.Output.ApocPathReturnType;
import org.tts.model.flat.FlatEdge;
import org.tts.model.flat.FlatSpecies;

@Service
public class GraphMLServiceImpl implements GraphMLService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
					String edgeType = edge.getTypeString();
					//System.out.println(edgeSymbol + ": " + inputSpeciesSymbol + "-[" + edgeType + "]->" + outputSpeciesSymbol);
					
					// get Annotations from InputSpecies
					nodeId = this.buildNodeAnnotations(nodeId, nodeAnnotations, nodeSymbolIdMap, nodesWithAnnotation,
							inputSpeciesSymbol, edge.getInputFlatSpecies().getAnnotation(), edge.getInputFlatSpecies().getAnnotationType());
					
					// get Annotations from OutputSpecies					
					nodeId = this.buildNodeAnnotations(nodeId, nodeAnnotations, nodeSymbolIdMap, nodesWithAnnotation,
							outputSpeciesSymbol, edge.getOutputFlatSpecies().getAnnotation(), edge.getOutputFlatSpecies().getAnnotationType());
					
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
					nodeId = this.buildNodeAnnotations(nodeId, nodeAnnotations, nodeSymbolIdMap, nodesWithAnnotation, species.getSymbol(), species.getAnnotation(), species.getAnnotationType());
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
			logger.error("Failed to write GraphML ByteArrayOutputStream with" + e.getMessage());
			return new ByteArrayOutputStream(); // TODO Evaluate whether returning an empty stream here is correct or desired
		}
	}
	/**
	 * Create a new ByteArrayOutpuStream into which the contents of Apoc.Path calls is being written in GraphML format
	 * Reduces the ApocPathReturnTypes given to a list of unique edges, which then get forwarded to getGraphMLForFlatEdges
	 * @param apocPathReturn Iterable\<ApocPathReturnType\> containing the results of an Apoc.Path reported into ApocPathReturnType.
	 * 						 Both relationships(path) and nodes(path) need to be reported, so that 
	 * 						 the start- and endNode of the edge, inputFlatSpecies and outputFlatSpecies respectively, 
	 * 						 are being populated in the edges.
	 * @param directed boolean parameter to denote whether the edges of the reported network are directed
	 * @return ByteArrayOutputStream with the complete contents of the graphML file.
	 */
	@Override
	public ByteArrayOutputStream getGraphMLForApocPathReturn(Iterable<ApocPathReturnType> apocPathReturn, boolean directed) {
		
		// Turn apoc pathReturn in List of edges.
		List<FlatEdge> flatEdges = new ArrayList<>();
		Set<String> edgeSymbols = new HashSet<>();
		Iterator<ApocPathReturnType> apocIter = apocPathReturn.iterator();
		while (apocIter.hasNext()) {
			ApocPathReturnType current = apocIter.next();
			
			List<FlatEdge> pathEdges = current.getPathEdges();
			for(FlatEdge currentEdge : pathEdges) {
				if(!edgeSymbols.contains(currentEdge.getSymbol())) {
					edgeSymbols.add(currentEdge.getSymbol());
					flatEdges.add(currentEdge);
				}
			}
		}
		return this.getGraphMLForFlatEdges(flatEdges, directed);	
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
			Map<String, String> nodeSymbolIdMap, List<byte[]> nodesWithAnnotation, String speciesSymbol,
			Map<String, Object> nodeAnnotationMap, Map<String, String> nodeAnnotationTypeMap) {
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
						int nodeAnnotationInt = (int) nodeAnnotationMap.get(annotationKey);
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
		
		edgesWithAnnotation.add(String.format("\t\t<edge source=\"%s\" target=\"%s\">\n\t\t\t<data key=\"e_interaction\">%s</data>\n", nodeSymbolIdMap.get(inputSpeciesSymbol), nodeSymbolIdMap.get(outputSpeciesSymbol), (edgeTypeString.equals("STIMULATION") ? "activation" : edgeTypeString.toLowerCase())).getBytes());
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
					int edgeAnnotationInt = (int) edgeAnnotationMap.get(annotationKey);
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
}
