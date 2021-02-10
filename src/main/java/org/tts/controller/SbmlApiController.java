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
import org.tts.service.PathwayService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.SBMLService;
import org.tts.service.WarehouseGraphService;
import org.tts.service.utility.FileCheckService;
import org.tts.service.warehouse.DatabaseNodeService;
import org.tts.service.warehouse.FileNodeService;
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
	DatabaseNodeService databaseNodeService;
	
	@Autowired
	FileCheckService fileCheckService;
	
	@Autowired
	FileNodeService fileNodeService;
	
	@Autowired
	OrganismService organismService;

	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	@Qualifier("SBMLSimpleModelServiceImpl") SBMLService sbmlService;
	
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
	public ResponseEntity<List<PathwayInventoryItem>> uploadSBML(@NotNull @Valid String organism,
			@NotNull @Valid String source, @NotNull @Valid String version, String user,
			@Valid List<MultipartFile> files) {
		logger.debug("Serving POST /sbml..");
		List<PathwayInventoryItem> fileNameToPathwayInventoryList = new ArrayList<>();
		
		for (MultipartFile file : files) {
			logger.debug("Processing file " + file.getOriginalFilename());
	
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
			DatabaseNode database = this.databaseNodeService.getDatabaseNode(source, version, org.getOrgCode());
			if(database == null) {
				// should be only the first time
				logger.info("Creating DatabaseNode for source: " + source + " with version " + version + " and organism " + org.getOrgCode());
				database = this.databaseNodeService.createDatabaseNode(source, version, org);
				this.provenanceGraphService.connect(database, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			} else {
				this.provenanceGraphService.connect(persistGraphActivityNode, database, ProvenanceGraphEdgeType.used);
			}
			
			FileNode sbmlFileNode = null;
			// Does node with this filename already exist?
			// TODO It should be able to have all these for each version of the database, so this needs to be encoded in there somewhere
			if(!this.fileNodeService.fileNodeExists(FileNodeType.SBML, org, file.getOriginalFilename())) {
				// does not exist -> create
				sbmlFileNode = this.fileNodeService.createFileNode(FileNodeType.SBML, org, file.getOriginalFilename());
				this.provenanceGraphService.connect(sbmlFileNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			} else {
				sbmlFileNode = this.fileNodeService.getFileNode(FileNodeType.SBML, org, file.getOriginalFilename());
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
			PathwayNode pathwayNode = this.pathwayService.createPathwayNode(sbmlModel.getId(), sbmlModel.getName(), org);
			
			List<ProvenanceEntity> resultSet;
			try {
				resultSet = sbmlService.buildAndPersist(sbmlModel, sbmlFileNode, persistGraphActivityNode);

				for (ProvenanceEntity entity : resultSet) {
					this.warehouseGraphService.connect(pathwayNode, entity, WarehouseGraphEdgeType.CONTAINS);
				}
				
				logger.info("Persisted " + resultSet.size() + " entities for file " + file.getOriginalFilename());			
				this.provenanceGraphService.connect(pathwayNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				this.provenanceGraphService.connect(pathwayNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
				this.provenanceGraphService.connect(sbmlFileNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
				this.provenanceGraphService.connect(database, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
				
				this.provenanceGraphService.connect(pathwayNode, sbmlFileNode, ProvenanceGraphEdgeType.wasDerivedFrom);
				
				fileNameToPathwayInventoryList.add(this.pathwayService.getPathwayInventoryItem(user, pathwayNode));
			} catch (Exception e) {
				// TODO Roll back transaction..
				e.printStackTrace();
				return ResponseEntity.badRequest().header("reason", "Error persisting the contents of the model. Database in inconsistent state!").build();
			}
		}
		return new ResponseEntity<>(fileNameToPathwayInventoryList, HttpStatus.CREATED);
	}
}
