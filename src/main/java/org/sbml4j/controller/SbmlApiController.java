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
package org.sbml4j.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml4j.Exception.ModelPersistenceException;
import org.sbml4j.api.SbmlApi;
import org.sbml4j.model.api.PathwayInventoryItem;
import org.sbml4j.model.common.Organism;
import org.sbml4j.model.common.GraphEnum.FileNodeType;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphActivityType;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphAgentType;
import org.sbml4j.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.provenance.ProvenanceGraphActivityNode;
import org.sbml4j.model.provenance.ProvenanceGraphAgentNode;
import org.sbml4j.model.warehouse.DatabaseNode;
import org.sbml4j.model.warehouse.FileNode;
import org.sbml4j.model.warehouse.PathwayNode;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.GraphBaseEntityService;
import org.sbml4j.service.PathwayService;
import org.sbml4j.service.ProvenanceGraphService;
import org.sbml4j.service.SBMLSimpleModelService;
import org.sbml4j.service.UtilityService;
import org.sbml4j.service.WarehouseGraphService;
import org.sbml4j.service.utility.FileCheckService;
import org.sbml4j.service.warehouse.DatabaseNodeService;
import org.sbml4j.service.warehouse.FileNodeService;
import org.sbml4j.service.warehouse.OrganismService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for the SbmlApi handling SBML file uploads
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class SbmlApiController implements SbmlApi {

	@Autowired
	ConfigService configService;
	
	@Autowired
	DatabaseNodeService databaseNodeService;
	
	@Autowired
	FileCheckService fileCheckService;
	
	@Autowired
	FileNodeService fileNodeService;
	
	@Autowired
	GraphBaseEntityService graphBaseEntityService;
	
	@Autowired
	OrganismService organismService;

	@Autowired
	PathwayService pathwayService;
	
	@Autowired
	ProvenanceGraphService provenanceGraphService;
	
	@Autowired
	SBMLSimpleModelService sbmlsimpleModelService;
	
	@Autowired
	WarehouseGraphService warehouseGraphService;
	
	@Autowired
	UtilityService utilityService;
	
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
		
		logger.debug("Serving POST /sbml " + (user != null ? " for user " + user : ""));
		
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
		
		
		// Organism
		Organism org = null;
		boolean orgExisted = false;
		if(organism != null) {
			if(this.organismService.organismExists(organism)) {
				org = this.organismService.getOrgansimByOrgCode(organism);
				orgExisted = true;
				//this.provenanceGraphService.connect(persistGraphActivityNode, org, ProvenanceGraphEdgeType.used);
			} else {
				if(organism.length() != 3) {
					return ResponseEntity.badRequest().header("reason", "Organism needs to be threeLetter Organsim Code (i.e. hsa). You priovided: " + organism).build();
				} else {
					org = this.organismService.createOrganism(organism);
					orgExisted=false;
					//this.provenanceGraphService.connect(org, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
			}
		} else { // organism is null
			return ResponseEntity.badRequest().header("reason", "Need to provide organsim in three Letter Code").build();
			// one could potentially read the organism from the file, but for now lets have the uploader provide it!
		}
		
		// Database
		// DatabaseNode has source, version info
		boolean databaseExisted = false;
		DatabaseNode database = this.databaseNodeService.getDatabaseNode(source, version, org.getOrgCode());
		
		if(database == null) {
			// should be only the first time
			logger.info("Creating DatabaseNode for source: " + source + " with version " + version + " and organism " + org.getOrgCode());
			database = this.databaseNodeService.createDatabaseNode(source, version, org);
			databaseExisted = false;
		} else {
			databaseExisted = true;
		}
		
		
		List<PathwayInventoryItem> pathwayInventoryList = new ArrayList<>();
		int countTotal = 0;
		int countError = 0;
		
		StringBuilder errorFileNames = new StringBuilder();
		int filenum = 0;
		int filestotal = files.size();
		for (MultipartFile file : files) {
			filenum++;
			countTotal++;
			String originalFilename = file.getOriginalFilename();
			logger.debug("Processing file " + originalFilename + " (" + filenum + "/" + filestotal + ")");
			//Instant beginOfFile = Instant.now();
			List<ProvenanceEntity> returnList = new ArrayList<>();
			ProvenanceEntity defaultReturnEntity = new ProvenanceEntity();
			
			// can we access the files name and ContentType?
			
			if(!fileCheckService.isFileReadable(file)) {
				defaultReturnEntity.setEntityUUID("Filename or content-type not accessible");
				returnList.add(defaultReturnEntity);
				return ResponseEntity.badRequest().header("reason", "Filename or content-type not accessible").build();
				//return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
			}
			
			logger.info("Serving POST /sbml for File " + originalFilename + " (" + filenum + "/" + filestotal + ")");
			
			// is Content Type xml?
			if(!fileCheckService.isContentXML(file)) {
				return ResponseEntity.badRequest().header("reason", "File ContentType is not application/xml").build();
			}

			// Then we need an Activity Node (uploadSBML)
			//Instant createDate = Instant.now();
			Map<String, Object> activityNodeProvenanceProperties = new HashMap<>();
			activityNodeProvenanceProperties.put("graphactivitytype", ProvenanceGraphActivityType.persistFile);
			activityNodeProvenanceProperties.put("graphactivityname", originalFilename); 
			//activityNodeProvenanceProperties.put("createdate", createDate);
			
			ProvenanceGraphActivityNode persistGraphActivityNode = this.provenanceGraphService.createProvenanceGraphActivityNode(activityNodeProvenanceProperties);
			
			// Associate Activity with User Agent
			this.provenanceGraphService.connect(persistGraphActivityNode, userAgentNode, ProvenanceGraphEdgeType.wasAssociatedWith);
			// Associate Organism with ActivityNode
			if (orgExisted) {
				this.provenanceGraphService.connect(persistGraphActivityNode, org, ProvenanceGraphEdgeType.used);
			} else {
				this.provenanceGraphService.connect(org, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				orgExisted = true;
			}
			// Associate Database with ActivityNode
			if (databaseExisted) {
				this.provenanceGraphService.connect(persistGraphActivityNode, database, ProvenanceGraphEdgeType.used);
			} else {
				this.provenanceGraphService.connect(database, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				databaseExisted = true;
			}
			
			// File Node
			FileNode sbmlFileNode = null;
			// Does node with this filename already exist?
			// TODO It should be able to have all these for each version of the database, so this needs to be encoded in there somewhere
			if(!this.fileNodeService.fileNodeExists(FileNodeType.SBML, org, originalFilename)) {
				// does not exist -> create
				sbmlFileNode = this.fileNodeService.createFileNode(FileNodeType.SBML, org, originalFilename);
				this.provenanceGraphService.connect(sbmlFileNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			} else {
				sbmlFileNode = this.fileNodeService.getFileNode(FileNodeType.SBML, org, originalFilename);
				this.provenanceGraphService.connect(persistGraphActivityNode, sbmlFileNode, ProvenanceGraphEdgeType.used);
			}
			
			// FileNode needs to connect to DatabaseNode
			this.provenanceGraphService.connect(sbmlFileNode, database, ProvenanceGraphEdgeType.wasDerivedFrom);
			//Instant createMetadataFinished = Instant.now();
			// now handle the model itself
			Model sbmlModel = null;
			try {
				sbmlModel =  sbmlsimpleModelService.extractSBMLModel(file);
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
			//Instant modelExtractionFinished = Instant.now();
			// create a Pathway Node for the model
			// TODO Likewise with the fileNode, the pathway Node and all Entities within it need to be able to be present for a combination of Org AND Database!!
			PathwayNode pathwayNode = this.pathwayService.createPathwayNode(sbmlModel.getId(), sbmlModel.getName(), org);
			//Instant createPathwayNodeFinished = Instant.now();
			//Map<String, ProvenanceEntity> resultSet;
			try {
				this.sbmlsimpleModelService.buildAndPersist(sbmlModel, sbmlFileNode, persistGraphActivityNode, pathwayNode, database);

				//for (ProvenanceEntity entity : resultSet.values()) {
				//	this.warehouseGraphService.connect(pathwayNode, entity, WarehouseGraphEdgeType.CONTAINS);
				//}
				//Instant modelPersistingFinished = Instant.now();
				
				this.provenanceGraphService.connect(pathwayNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				this.provenanceGraphService.connect(pathwayNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
				this.provenanceGraphService.connect(sbmlFileNode, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
				this.provenanceGraphService.connect(database, userAgentNode, ProvenanceGraphEdgeType.wasAttributedTo);
				
				this.provenanceGraphService.connect(pathwayNode, sbmlFileNode, ProvenanceGraphEdgeType.wasDerivedFrom);
				pathwayInventoryList.add(this.pathwayService.getPathwayInventoryItem(user, pathwayNode));
				//Instant postMetadataFinished = Instant.now();
				
				//StringBuilder sb = new StringBuilder();
				//sb.append("Execution times: ");
				
				// createPreMetadata
				/*this.utilityService.appendDurationString(sb, Duration.between(beginOfFile, postMetadataFinished), "total");
				this.utilityService.appendDurationString(sb, Duration.between(beginOfFile, createMetadataFinished), "preMetadata");
				this.utilityService.appendDurationString(sb, Duration.between(createMetadataFinished, modelExtractionFinished), "modelExtract");
				this.utilityService.appendDurationString(sb, Duration.between(modelExtractionFinished, createPathwayNodeFinished), "createPWNode");
				this.utilityService.appendDurationString(sb, Duration.between(createPathwayNodeFinished, modelPersistingFinished), "modelPersist");
				this.utilityService.appendDurationString(sb, Duration.between(modelPersistingFinished, postMetadataFinished), "postMetadata");
						
				logger.info(sb.toString());*/
				
			} catch (ModelPersistenceException e) {
				if (e.getMessage().startsWith("SBMLSpecies")) {
					this.graphBaseEntityService.deleteEntity(pathwayNode);
					logger.error("Error during model persistence. Model " + originalFilename + " has not been loaded.");
					logger.error("The error message is: " + e.getMessage());
					e.printStackTrace();
					logger.error("Continuing with the next model..");
				} else {
					logger.error("Error during model persistence. Model " + originalFilename + " has not been loaded correctly. Unfortunately at this moment we cannot clean up after you. There might be dangling entities in the database. This feature will be implemented in a future release, I promise.");
					logger.error("The error message is: " + e.getMessage());
					e.printStackTrace();
					logger.error("Continuing with the next model..");
					countError++;
					errorFileNames.append(originalFilename);
					errorFileNames.append(", ");
				}
			} catch (Exception e) {
				// TODO Roll back transaction..
				logger.error("Error during model persistence. Model " + originalFilename + " has not been loaded correctly. Unfortunately at this moment we cannot clean up after you. There might be dangling entities in the database. This feature will be implemented in a future release, I promise.");
				logger.error("The error message is: " + e.getMessage());
				e.printStackTrace();
				logger.error("Continuing with the next model..");
				countError++;
				errorFileNames.append(originalFilename);
				errorFileNames.append(", ");
				//return ResponseEntity.badRequest().header("reason", "Error persisting the contents of the model. Database in inconsistent state!").build();
			} finally {
				logger.info("Finished processing file " + originalFilename);
			}
		}
		if (countError > 0) {
			if (countTotal > countError) {
				//ResponseEntity mixed = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).header("reason", "Could not persist Model in file(s)" + errorFileNames.toString()).build();
				ResponseEntity<List<PathwayInventoryItem>> m = new ResponseEntity<>(pathwayInventoryList, HttpStatus.UNPROCESSABLE_ENTITY);
				m.getHeaders().add("reason", "Could not persist Model in file(s)" + errorFileNames.toString());
				return m;
			} else {
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).header("reason", "Could not persist Model in file(s): " + errorFileNames.toString()).build();
			}
		} else {
			return new ResponseEntity<>(pathwayInventoryList, HttpStatus.CREATED);
		}
	}


	
}
