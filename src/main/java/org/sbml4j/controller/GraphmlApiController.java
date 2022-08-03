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
package org.sbml4j.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;

import org.sbml4j.Exception.ConfigException;
import org.sbml4j.api.GraphmlApi;
import org.sbml4j.model.api.network.NetworkInventoryItem;
import org.sbml4j.model.base.GraphEnum.FileNodeType;
import org.sbml4j.model.base.GraphEnum.NetworkMappingType;
import org.sbml4j.model.base.GraphEnum.Operation;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphAgentType;
import org.sbml4j.model.base.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.flat.FlatEdge;
import org.sbml4j.model.flat.FlatSpecies;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.warehouse.FileNode;
import org.sbml4j.model.warehouse.MappingNode;
import org.sbml4j.model.warehouse.Organism;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.FlatEdgeService;
import org.sbml4j.service.FlatSpeciesService;
import org.sbml4j.service.GraphMLService;
import org.sbml4j.service.ProvenanceGraphService;
import org.sbml4j.service.WarehouseGraphService;
import org.sbml4j.service.networks.NetworkService;
import org.sbml4j.service.utility.FileCheckService;
import org.sbml4j.service.warehouse.FileNodeService;
import org.sbml4j.service.warehouse.MappingNodeService;
import org.sbml4j.service.warehouse.OrganismService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
	@Autowired
	FlatEdgeService flatEdgeService;
	@Autowired
	FlatSpeciesService flatSpeciesService;
	@Autowired
	NetworkService networkService;
	
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
		List<NetworkInventoryItem> inventoryItems = new ArrayList<>(); 
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
			MappingNode parentMappingNode = null;
			String parentUUIDString = null;
			if (parentUUID != null) {
				hasParent = true;
				parentUUIDString = parentUUID.toString();
				paramsMap.put("UUID", parentUUIDString);
				parentMappingNode = this.mappingNodeService.findByEntityUUID(parentUUIDString);
				String oldMappingName = parentMappingNode.getMappingName();
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
				org = this.organismService.findOrganismForWarehouseGraphNode(parentUUIDString);
				if (org != null) {
					hasOrganism = true;
				}
				
			} else {
				// no parent network
				hasParent = false;
				if (networkname == null) {
					createdNetworkname = (prefixName ? "GraphML_network_" : "") + originalFilename +(suffixName ? "_GraphML_network" : "");
				} else {
					createdNetworkname = (prefixName ? "GraphML_network_" : "") + networkname +(suffixName ? "_GraphML_network" : "");
				}
				
			}
			if (!hasOrganism) {
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
			if (hasParent) this.provenanceGraphService.connect(persistGraphActivityNode, parentMappingNode, ProvenanceGraphEdgeType.used);
			
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
				Map<String, FlatSpecies> networkSpecies = this.graphMLService.getFlatSpeciesForGraphML(file);
				List<FlatEdge> networkEdges = this.graphMLService.getFlatEdgesForGraphML(file, networkSpecies);
				
				log.debug("Created Network components");
				this.flatSpeciesService.save(networkSpecies.values(), 0);
				this.flatEdgeService.save(networkEdges, 1);
				this.networkService.connectContains(graphMLMappingNode, networkEdges, networkSpecies.values(), true);
				// connect network to parent (if it exists)
				if (hasParent) {
					this.provenanceGraphService.connect(graphMLMappingNode, this.mappingNodeService.findByEntityUUID(parentUUIDString), ProvenanceGraphEdgeType.wasDerivedFrom);
				}
				this.networkService.updateMappingNodeMetadata(graphMLMappingNode);
				
				this.provenanceGraphService.connect(graphMLMappingNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
				this.provenanceGraphService.connect(graphMLMappingNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				inventoryItems.add(this.networkService.getNetworkInventoryItem(graphMLMappingNode.getEntityUUID()));
				
			} catch (IOException e) {
				countError++;
				log.error("IOException while persisting GraphML: ", e);
				e.printStackTrace();
				errorFileNames.append(file.getOriginalFilename());
				errorFileNames.append(", ");
			} catch (ParserConfigurationException e) {
				countError++;
				log.error("ParserConfigurationException while persisting GraphML: ", e);
				e.printStackTrace();
				errorFileNames.append(file.getOriginalFilename());
				errorFileNames.append(", ");
			} catch (SAXException e) {
				countError++;
				log.error("SAXException while persisting GraphML: ", e);
				e.printStackTrace();
				errorFileNames.append(file.getOriginalFilename());
				errorFileNames.append(", ");
			} catch (ConfigException e) {
				countError++;
				log.error("ConfigException while persisting GraphML: ", e);
				e.printStackTrace();
				errorFileNames.append(file.getOriginalFilename());
				errorFileNames.append(", ");
			}
		}
		if (countError > 0) {
			if (countTotal > countError) {
				//ResponseEntity mixed = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).header("reason", "Could not persist Model in file(s)" + errorFileNames.toString()).build();
				ResponseEntity<List<NetworkInventoryItem>> m = new ResponseEntity<>(inventoryItems, HttpStatus.UNPROCESSABLE_ENTITY);
				m.getHeaders().add("reason", "Could not create networks from file(s): " + errorFileNames.toString());
				return m;
			} else {
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).header("reason", "Could not create networks from file(s): " + errorFileNames.toString()).build();
			}
		} else {
			return new ResponseEntity<>(inventoryItems, HttpStatus.CREATED);
		}
	}

}
