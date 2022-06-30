package org.sbml4j.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;

import org.sbml4j.api.GraphmlApi;
import org.sbml4j.model.api.network.NetworkInventoryItem;
import org.sbml4j.model.base.GraphEnum.FileNodeType;
import org.sbml4j.model.base.GraphEnum.NetworkMappingType;
import org.sbml4j.model.base.GraphEnum.Operation;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphAgentType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.base.GraphEnum.WarehouseGraphEdgeType;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.warehouse.FileNode;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.model.warehouse.Organism;
import org.sbml4j.model.warehouse.WarehouseGraphNode;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.GraphMLService;
import org.sbml4j.service.ProvenanceGraphService;
import org.sbml4j.service.WarehouseGraphService;
import org.sbml4j.service.utility.FileCheckService;
import org.sbml4j.service.warehouse.FileNodeService;
import org.sbml4j.service.warehouse.MappingNodeService;
import org.sbml4j.service.warehouse.OrganismService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

@Controller
public class GraphmlApiController implements GraphmlApi {
	@Autowired
	ConfigService configService;
	@Autowired
	FileCheckService fileCheckService;
	@Autowired
	FileNodeService fileNodeService;
	@Autowired
	GraphMLService graphMLService;
	@Autowired
	OrganismService organismService;
	@Autowired
	MappingNodeService mappingNodeService;
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public ResponseEntity<List<NetworkInventoryItem>> uploadGraphML(String user, @Valid UUID parentUUID,
			@Valid String networkname, @Valid Boolean prefixName, @Valid Boolean suffixName,
			@Valid List<MultipartFile> files) {
		
		Operation op = Operation.POST;
		String endpoint = "/graphml";
		log.info("Serving " + op.getOperation() + " " + endpoint + (user != null ? " for user " + user : ""));

		if (user == null || user.isBlank() ||user.isEmpty()) {
			user = configService.getPublicUser();
		}
		if (user == null) {
			return ResponseEntity.badRequest().header("reason", "No user provided and no public user configured. This is fatal. Aborting. Please provide user in header or configure public user.").build();
		}
		
		/*
		 * Create Agent for user if not existing
		 */
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", user);
		agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
		ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService.createProvenanceGraphAgentNode(agentNodeProperties);
		
		
		// // provenance
		Map<String, Map<String, Object>> provenanceAnnotation = new HashMap<>();

		Map<String, Object> paramsMap = new HashMap<>();
		
		/*
		 * Was a parent network provided?
		 */
		
		int countTotal = 0;
		int countError = 0;
		
		StringBuilder errorFileNames = new StringBuilder();
		int filenum = 0;
		int filestotal = files.size();
		
		// New data for provenance tracking
		// get all filenames
		StringBuilder allFileNamesSB = new StringBuilder();
		
		for (MultipartFile file :files) {
			allFileNamesSB.append(file.getOriginalFilename());
			allFileNamesSB.append(',');
		}
		String allFileNames = allFileNamesSB.substring(0, allFileNamesSB.lastIndexOf(","));
		
		for (MultipartFile file : files) {
			filenum++;
			countTotal++;
			String originalFilename = file.getOriginalFilename();
			log.debug("Processing file " + originalFilename + " (" + filenum + "/" + filestotal + ")");
			
			if(!this.fileCheckService.isFileReadable(file)) {
				return ResponseEntity.badRequest().header("reason", "Filename or content-type not accessible").build();
			}
			
			// is Content Type xml?
			//if(!this.fileCheckService.isContentXML(file)) {
			//	return ResponseEntity.badRequest().header("reason", "File ContentType is not application/xml").build();
			//}
			
			String fileMD5Sum;
			try {
				fileMD5Sum = this.fileCheckService.getMD5Sum(file);
			} catch (IOException e1) {
				e1.printStackTrace();
				return ResponseEntity.badRequest().header("reason", "IOException while calculating MD5 Sum of file " + originalFilename).build();
			}
			// prepare the organism
			boolean hasOrganism = false;
			Organism org = null;
			
			// 3. Determine the name of the network
			String createdNetworkname = null;
			boolean hasParent = false;
			String parentUUIDString = null;
			if (parentUUID != null) {
				hasParent = true;
				parentUUIDString = parentUUID.toString();
				paramsMap.put("UUID", parentUUIDString);
				MappingNode mappingForUUID = this.mappingNodeService.findByEntityUUID(parentUUIDString);
				String oldMappingName = mappingForUUID.getMappingName();
				if (prefixName && networkname != null) {
					createdNetworkname = (networkname.endsWith("_") ? networkname : networkname + "_") + 
							oldMappingName;
				} else if (suffixName && networkname != null) { 
					createdNetworkname = oldMappingName + (networkname.startsWith("_") ? networkname : "_" + networkname);
				} else if (networkname == null && (prefixName || suffixName)) {
					createdNetworkname = (prefixName ? "GraphML_derived_from_" : "") + oldMappingName +(suffixName ? "_derived_GraphML" : "");
				} else {
					createdNetworkname = networkname;
				}
				// Organism
				Iterable<ProvenanceEntity> organisms = this.warehouseGraphService.findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType.FOR, parentUUIDString);
				for (ProvenanceEntity organism : organisms) {
					if (hasOrganism) {
						log.warn("Multiple Organisms found for MappingNode with UUID " + parentUUIDString);
					} else if (Organism.class.isInstance(organism)) {
						org = (Organism) organism;
						hasOrganism = true;
					}
				}
				if (!hasOrganism) {
					log.warn("Could not locate Organism for MappingNode with UUID " + parentUUIDString);
					org = this.organismService.createOrganism("GraphML");
				}
				
			} else {
				// no parent network
				hasParent = false;
				if (networkname == null) {
					createdNetworkname = (prefixName ? "GraphML_network_" : "") + originalFilename +(suffixName ? "_GraphML_network" : "");
				} else {
					createdNetworkname = (prefixName ? "GraphML_network_" : "") + networkname +(suffixName ? "_GraphML_network" : "");
				}
				
				// need new Organism for this GraphML File
				org = this.organismService.createOrganism("GraphML");
				
			}
			// have createdNetworkname
			// have boolean if parent exists
			
			// have Organism in org
			
			// create FileNode
			FileNode graphMLFileNode = null;
			
			if(!this.fileNodeService.fileNodeExists(FileNodeType.GRAPHML, org, originalFilename, fileMD5Sum)) {
				// does not exist -> create
				graphMLFileNode = this.fileNodeService.createFileNode(FileNodeType.GRAPHML, org, originalFilename, fileMD5Sum);
			} else {
				graphMLFileNode = this.fileNodeService.getFileNode(FileNodeType.GRAPHML, org, originalFilename, fileMD5Sum);
				// TODO: Is it a problem, if we have multiple Networks attached to this FileNode?
			}
			
			// create activity
			Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
			activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.persistFile);
			activityNodeProvenanceProperties.put("graphactivityname", originalFilename); 
			
			
			ProvenanceGraphActivityNode persistGraphActivityNode = this.provenanceGraphService.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
			// 
			this.provenanceGraphService.connect(graphMLFileNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);

			
			// complete Provenance
			paramsMap.put("user", user);
			paramsMap.put("files", allFileNames);
			paramsMap.put("networkname", networkname);
			if (prefixName != null) paramsMap.put("prefixName", prefixName);
			if (prefixName != null) paramsMap.put("suffixName", suffixName);
			provenanceAnnotation.put("params",  paramsMap);	
			
			Map<String, Object> endpointMap = new HashMap<>();
			endpointMap.put("operation", op.getOperation());
			endpointMap.put("endpoint", endpoint);
			
			provenanceAnnotation.put("endpoint",  endpointMap);
			
			this.provenanceGraphService.addProvenanceAnnotationMap(persistGraphActivityNode, provenanceAnnotation);
			
			
			
			
			// need MappingNode
			MappingNode graphMLMappingNode = this.mappingNodeService.createMappingNode(graphMLFileNode, 
					(hasParent ? this.mappingNodeService.findByEntityUUID(parentUUIDString).getMappingType() : NetworkMappingType.PATHWAYMAPPING), 
					createdNetworkname);
			
			// create Network from GraphML
			try {
				List<FlatSpecies> networkSpecies = this.graphMLService.getFlatSpeciesForGraphML(file);
				//List<FlatEdge> networkEdges = this.graphMLService.getFlatEdgesForGraphML(file, networkSpecies);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// connect network to parent (if it exists)
			
			
			
		}
		
		
		
		
		
		return ResponseEntity.badRequest().build();
	}

}
