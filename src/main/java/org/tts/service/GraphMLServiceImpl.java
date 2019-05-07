package org.tts.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tts.model.api.Output.NodeEdgeList;
import org.tts.model.api.Output.NodeNodeEdge;

@Service
public class GraphMLServiceImpl implements GraphMLService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public GraphMLServiceImpl() {
		super();
	}

	@Override
	public String getGraphMLString(NodeEdgeList nodeEdgeList) {
		String graphMLString = initGraphMLFileString();
		graphMLString = addGraphToGraphMLString(nodeEdgeList, graphMLString);
		graphMLString = closeGraphMLString(graphMLString);
		return graphMLString;
		
	}
	
	private String addGraphToGraphMLString(NodeEdgeList nodeEdgeList, String graphMLString) {
		
		// add keys for annotations:
		// node Name:
		graphMLString += "\t<key id=\"v_name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>\n";
		// interactionTypes:
		graphMLString += "\t<key id=\"e_interaction\" for=\"edge\" attr.name=\"interaction\" attr.type=\"string\"/>\n";
		
		graphMLString += "\t<graph id=\"G\" edgedefault=\"undirected\">\n";
		
		Map<String, String> idNodeMap = new HashMap<>();
		int nodeId = 0;
		Set<String> uniqueEdges = new HashSet<>();
		for(NodeNodeEdge nne : nodeEdgeList.getNodeNodeEdgeList()) {
			if (!idNodeMap.containsKey(nne.getNode1())) {
			//if(uniqueNodes.add(nne.getNode1())) {
				// node1 was not yet added ->
				String nodeIdString = String.format("n%d", nodeId);
				++nodeId;
				idNodeMap.put(nne.getNode1(), nodeIdString);
				// add to graphML
				//graphMLString += String.format("\t\t<node id=\"%s\">\n\t\t\t<data key=\"v_name\">%s</data>\n\t\t</node>\n", nne.getNode1(), nne.getNode1());
				graphMLString += String.format("\t\t<node id=\"%s\">\n\t\t\t<data key=\"v_name\">%s</data>\n\t\t</node>\n", nodeIdString, nne.getNode1());
			}
			if(!idNodeMap.containsKey(nne.getNode2())) {
			//if(uniqueNodes.add(nne.getNode2())) {
				// node2 was not yet added ->
				String nodeIdString = String.format("n%d", nodeId);
				++nodeId;
				idNodeMap.put(nne.getNode2(), nodeIdString);
				// add to graphML
				//graphMLString += String.format("\t\t<node id=\"%s\">\n\t\t\t<data key=\"v_name\">%s</data>\n\t\t</node>\n", nne.getNode2(), nne.getNode2());
				graphMLString += String.format("\t\t<node id=\"%s\">\n\t\t\t<data key=\"v_name\">%s</data>\n\t\t</node>\n", nodeIdString, nne.getNode2());
			}
			// TODO: If graph is undirected: Be sure that the edges we add are unique also in the way that
			// N1 -e1-> N2 is the same as N2 -e1-> N1 
			uniqueEdges.add(String.format("\t\t<edge source=\"%s\" target=\"%s\"/>\n\t\t\t<data key=\"e_interaction\">%s</data>\n\t\t</edge>\n", idNodeMap.get(nne.getNode1()), idNodeMap.get(nne.getNode2()), nne.getEdge()));
			
			
		}
		for (String edgeString : uniqueEdges) {
			graphMLString += edgeString;
		}
		graphMLString += "\t</graph>\n";
		return graphMLString;
	}

	private String initGraphMLFileString() {
		String graphMLString = "";
		graphMLString += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		graphMLString += "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n";
		graphMLString += "\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
		graphMLString += "\t\txsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n";
		graphMLString += "\t\thttp://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n";
		return graphMLString;
	}
	private String closeGraphMLString(String graphMLString) {
		graphMLString += "</graphml>";
		return graphMLString;
	}
	
}
