/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
 */

package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.tts.api.SbmlApi;
import org.tts.model.api.PathwayInventoryItem;
import org.tts.model.common.GraphEnum.FileNodeType;
import org.tts.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.tts.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.common.Organism;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.provenance.ProvenanceGraphActivityNode;
import org.tts.model.provenance.ProvenanceGraphAgentNode;
import org.tts.model.warehouse.DatabaseNode;
import org.tts.model.warehouse.FileNode;
import org.tts.model.warehouse.PathwayNode;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.SBMLService;
import org.tts.service.WarehouseGraphService;
import org.tts.service.utility.FileCheckService;
import org.tts.service.warehouse.OrganismService;

/**
 * Controller for the SbmlApi handling SBML file uploads
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class SbmlApiController implements SbmlApi {

	@Autowired
	FileCheckService fileCheckService;
	
	@Autowired
	@Qualifier("SBMLSimpleModelServiceImpl") SBMLService sbmlService;
	
	@Autowired
	OrganismService organismService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	/**
	 * POST /sbml
	 * Endpoint to upload a sbml file, extract the model and persist the contents in the simple model representation
	 * 
	 * @param file The file holding the sbml model to be persisted
	 * @return all Entities as they were persisted (or connected if already present) as a result of persisting the model
	 */
	@Override
	public ResponseEntity<Map<String, PathwayInventoryItem>> uploadSBML(@Valid MultipartFile[] files, String user,
			@NotNull @Valid String organism, @NotNull @Valid String source, @NotNull @Valid String version) {
		
		Map<String, PathwayInventoryItem> fileNameToPathwayInventoryMap = new HashMap<>();
		
		for (MultipartFile file : files) {
			log.info("Processing file " + file.getOriginalFilename());
	
			List<ProvenanceEntity> returnList = new ArrayList<>();
			ProvenanceEntity defaultReturnEntity = new ProvenanceEntity();
			
			// can we access the files name and ContentType?
			
			if(!fileCheckService.isFileReadable(file)) {
				defaultReturnEntity.setEntityUUID("Filename or content-type not accessible");
				returnList.add(defaultReturnEntity);
				return ResponseEntity.badRequest().header("reason", "Filename or content-type not accessible").build();
				//return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
			}
			
			logger.info("Serving POST /sbml for File " + file.getOriginalFilename());
			
			// is Content Type xml?
			if(!fileCheckService.isContentXML(file)) {
				return ResponseEntity.badRequest().header("reason", "File ContentType is not application/xml").build();
			}

			/*
			 * Create Provenance Nodes
			 */
			// Create a ProvenanceGraph.Agent Node with it (using the timestamp to make it unique?) - or find Agent in DB and use it
			Map<String, Object> agentNodeProperties = new HashMap<>();
			agentNodeProperties.put("graphagentname", user);
			agentNodeProperties.put("graphagenttype", ProvenanceGraphAgentType.User);
			ProvenanceGraphAgentNode userAgentNode = this.provenanceGraphService.createProvenanceGraphAgentNode(agentNodeProperties);
			
			// Then we need an Activity Node (uploadSBML)
			//Instant createDate = Instant.now();
			Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
			activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.persistFile);
			activityNodeProvenanceProperties.put("graphactivityname", file.getOriginalFilename()); 
			//activityNodeProvenanceProperties.put("createdate", createDate);
			
			ProvenanceGraphActivityNode persistGraphActivityNode = this.provenanceGraphService.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
			
			// Associate Activity with User Agent
			this.provenanceGraphService.connect(persistGraphActivityNode, userAgentNode, ProvenanceGraphEdgeType.wasAssociatedWith);
			
			// Organism
			Organism org = null;
			if(organism != null) {
				if(this.organismService.organismExists(organism)) {
					org = this.organismService.getOrgansimByOrgCode(organism);
					this.provenanceGraphService.connect(persistGraphActivityNode, org, ProvenanceGraphEdgeType.used);
				} else {
					if(organism.length() != 3) {
						return ResponseEntity.badRequest().header("reason", "Organism needs to be threeLetter Organsim Code (i.e. hsa). You priovided: " + organism).build();
					} else {
						org = this.organismService.createOrganism(organism);
						this.provenanceGraphService.connect(org, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
					}
				}
			} else { // organism is null
				return ResponseEntity.badRequest().header("reason", "Need to provide organsim in three Letter Code").build();
				// one could potentially read the organism from the file, but for now lets have the uploader provide it!
			}
					
			// DatabaseNode has source, version info
			DatabaseNode database = this.warehouseGraphService.getDatabaseNode(source, version, org);
			if(database == null) {
				// should be only the first time
				logger.info("Creating DatabaseNode for source: " + source + " with version " + version + " and organism " + org.getOrgCode());
				database = this.warehouseGraphService.createDatabaseNode(source, version, org);
				this.provenanceGraphService.connect(database, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			} else {
				this.provenanceGraphService.connect(persistGraphActivityNode, database, ProvenanceGraphEdgeType.used);
			}
			
			FileNode sbmlFileNode = null;
			// Does node with this filename already exist?
			// TODO It should be able to have all these for each version of the database, so this needs to be encoded in there somewhere
			if(!this.warehouseGraphService.fileNodeExists(FileNodeType.SBML, org, file.getOriginalFilename())) {
				// does not exist -> create
				try {
					sbmlFileNode = this.warehouseGraphService.createFileNode(FileNodeType.SBML, org, file.getOriginalFilename(), file.getBytes());
					this.provenanceGraphService.connect(sbmlFileNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				} catch (IOException e) {
					return ResponseEntity.badRequest().header("reason", "Cannot extract content of file " + file.getOriginalFilename()).build();
				}
				
			} else {
				sbmlFileNode = this.warehouseGraphService.getFileNode(FileNodeType.SBML, org, file.getOriginalFilename());
				this.provenanceGraphService.connect(persistGraphActivityNode, sbmlFileNode, ProvenanceGraphEdgeType.used);
			}
			
			// FileNode needs to connect to DatabaseNode
			this.provenanceGraphService.connect(sbmlFileNode, database, ProvenanceGraphEdgeType.wasDerivedFrom);
			
			// now handle the model itself
			Model sbmlModel = null;
			try {
				sbmlModel =  sbmlService.extractSBMLModel(file);
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return ResponseEntity.badRequest().header("reason", "XMLStreamException while extracting SBMLModel from file").build();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return ResponseEntity.badRequest().header("reason", "IOException while extracting SBMLModel from file").build();
			}
			if(sbmlModel == null) {
				return ResponseEntity.badRequest().header("reason", "Could not extract an sbmlModel").build();
			}
			// create a Pathway Node for the model
			// TODO Likewise with the fileNode, the pathway Node and all Entities within it need to be able to be present for a combination of Org AND Database!!
			PathwayNode pathwayNode = this.warehouseGraphService.createPathwayNode(sbmlModel.getId(), sbmlModel.getName(), org);
			
			List<ProvenanceEntity> resultSet = sbmlService.buildAndPersist(sbmlModel, sbmlFileNode, persistGraphActivityNode);
			
			for (ProvenanceEntity entity : resultSet) {
				this.warehouseGraphService.connect(pathwayNode, entity, WarehouseGraphEdgeType.CONTAINS);
			}
			
			logger.info("Persisted " + resultSet.size() + " entities for file " + file.getOriginalFilename());
						
			this.provenanceGraphService.connect(pathwayNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			this.provenanceGraphService.connect(pathwayNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
			this.provenanceGraphService.connect(sbmlFileNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
			this.provenanceGraphService.connect(database, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
			
			this.provenanceGraphService.connect(pathwayNode, sbmlFileNode, ProvenanceGraphEdgeType.wasDerivedFrom);
			
			fileNameToPathwayInventoryMap.put(file.getOriginalFilename(), this.warehouseGraphService.getPathwayInventoryItem(user, pathwayNode));
			

		}
		return new ResponseEntity<>(fileNameToPathwayInventoryMap, HttpStatus.CREATED);
	}
}
