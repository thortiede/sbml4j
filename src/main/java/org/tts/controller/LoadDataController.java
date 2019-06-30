package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.common.GraphBaseEntity;
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
import org.tts.service.FileCheckService;
import org.tts.service.FileService;
import org.tts.service.FileStorageService;
import org.tts.service.NodeEdgeListService;
import org.tts.service.OrganismService;
import org.tts.service.ProvenanceGraphService;
import org.tts.service.SBMLPersistenceService;
import org.tts.service.SBMLService;
import org.tts.service.WarehouseGraphService;

@RestController
public class LoadDataController {

	FileCheckService fileCheckService;
	FileStorageService fileStorageService;
	SBMLService sbmlService;
	SBMLPersistenceService sbmlPersistenceService;
	FileService fileService;
	NodeEdgeListService nodeEdgeListService;
	OrganismService organismService;
	ProvenanceGraphService provenanceGraphService;
	WarehouseGraphService warehouseGraphService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public LoadDataController(FileStorageService fileStorageService, 
			FileCheckService fileCheckService, 
			@Qualifier("SBMLSimpleModelServiceImpl") SBMLService sbmlService, 
			SBMLPersistenceService sbmlPersistenceService,
			FileService fileService,
			@Qualifier("simpleNodeEdgeListServiceImpl") NodeEdgeListService nodeEdgeListService,
			OrganismService organismService,
			ProvenanceGraphService provenanceGraphService,
			WarehouseGraphService warehouseGraphService) {
		super();
		this.fileStorageService = fileStorageService;
		this.fileCheckService = fileCheckService;
		this.sbmlService = sbmlService;
		this.sbmlPersistenceService = sbmlPersistenceService;
		this.fileService = fileService;
		this.nodeEdgeListService = nodeEdgeListService;
		this.organismService = organismService;
		this.provenanceGraphService = provenanceGraphService;
		this.warehouseGraphService = warehouseGraphService;
	}
	/** 
	 * All Entity Endpoints
	 */
	
	/**
	 * GET /allEntities
	 * @return all found Nodes that are derived from GraphBaseEntity (all Nodes at this point)
	 */
	@RequestMapping(value="/allEntities", method=RequestMethod.GET)
	public ResponseEntity<List<GraphBaseEntity>> showAllEntites() {
		logger.info("Serving GET allEntities");
		return new ResponseEntity<List<GraphBaseEntity>>(sbmlService.getAllEntities(), HttpStatus.OK);
	}
	
	/**
	 * DELETE /allEntities
	 * Attempts to remove all Nodes and their relationships from the database. Does a check after issuing delete
	 * @return String declaring whether database is free of nodes/relationships
	 */
	
	@RequestMapping(value="/allEntities", method=RequestMethod.DELETE)
	public ResponseEntity<String> deleteAllEntites() {
		logger.info("Serving DELETE allEntities");
		if(this.sbmlService.clearDatabase()) {
			return new ResponseEntity<String>("Database is clear!", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Database is not clear!", HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	/**
	 * POST /sbml
	 * Endpoint to upload a sbml file, extract the model and persist the contents in the simple model representation
	 * @param file The file holding the sbml model to be persisted
	 * @return all Entities as they were persisted (or connected if already present) as a result of persisting the model
	 */
	@RequestMapping(value = "/sbml", method=RequestMethod.POST)
	public ResponseEntity<List<ProvenanceEntity>> uploadSBML(@RequestParam("file") MultipartFile file,
															@RequestParam("organism") String organism,
															@RequestParam("matchingAttribute") String matchingAttribute,
															@RequestParam("source") String source,
															@RequestParam("version") String sourceVersion,
															@RequestHeader("user") String username) {
		

		/*
		 * Basic checks of the file
		 */
		List<ProvenanceEntity> returnList = new ArrayList<>();
		ProvenanceEntity defaultReturnEntity = new ProvenanceEntity();
		
		// can we access the files name and ContentType?
		
		if(!fileCheckService.isFileReadable(file)) {
			defaultReturnEntity.setEntityUUID("Filename or content-type not accessible");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		
		logger.info("Serving POST /sbml for File " + file.getOriginalFilename());
		
		// is Content Type xml?
		if(!fileCheckService.isContentXML(file)) {
			defaultReturnEntity.setEntityUUID("File ContentType is not application/xml");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		
		/*
		 * Create Provenance Nodes
		 */
		
		
		// Potentially read out header information to get the user that uploads this file
		// Create a ProvenanceGraph.Agent Node with it (using the timestamp to make it unique?) - or find Agent in DB and use it
		// String username = "Thor"; // TODO: Grab this from the header
		Map<String, Object> agentNodeProperties = new HashMap<>();
		agentNodeProperties.put("graphagentname", username);
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
					defaultReturnEntity.setEntityUUID("Organism needs to be threeLetter Organsim Code (i.e. hsa). You priovided: " + organism);
					returnList.add(defaultReturnEntity);
					return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
				} else {
					org = this.organismService.createOrganism(organism);
					this.provenanceGraphService.connect(org, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
				}
			}
		} else { // organism is null
			defaultReturnEntity.setEntityUUID("Need to provide organsim in three Letter Code");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
			// one could potentially read the organism from the file, but for now lets have the uploader provide it!
		}
		// now we can link all created Nodes based on GraphBaseEntity to this organism
		
		
		// MatchingAttributes
		/**
		 * So far, I only use one matching attribute.
		 * I want to have the possibility open to have individual matchingAttributes per Entity in the model
		 * So far I do not use it, but want to have it covered for future features
		 */
		Map<String, String> matchingAttributes = new HashMap<>();
		if (matchingAttribute != null) {
			matchingAttributes.put("Default", matchingAttribute);
		} else {
			matchingAttributes.put("Default", "N/A");
		}
				
		
		// KnowledgeGraphNode, not sure if needed actually, the database node could just be the knowledgegraphnode
		// DatabaseNode has source, version info, connects to KnowledgeGraphNode
		
		DatabaseNode database = this.warehouseGraphService.getDatabaseNode(source, sourceVersion, org, matchingAttributes);
		
		if(database == null) {
			// should be only the first time
			logger.info("Creating DatabaseNode for source: " + source + " with version " + sourceVersion + " and organism " + org.getOrgCode() + " using " + matchingAttributes.get("Default"));
			database = this.warehouseGraphService.createDatabaseNode(source, sourceVersion, org, matchingAttributes);
			this.provenanceGraphService.connect(database, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
		} else {
			this.provenanceGraphService.connect(persistGraphActivityNode, database, ProvenanceGraphEdgeType.used);
		}
		
		
	
		
		FileNode sbmlFileNode = null;
		// Does node with this filename already exist?
		if(!this.warehouseGraphService.fileNodeExists(FileNodeType.SBML, org, file.getOriginalFilename())) {
			// does not exist -> create
			try {
				sbmlFileNode = this.warehouseGraphService.createFileNode(FileNodeType.SBML, org, file.getOriginalFilename(), file.getBytes());
				this.provenanceGraphService.connect(sbmlFileNode, persistGraphActivityNode, ProvenanceGraphEdgeType.wasGeneratedBy);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				defaultReturnEntity.setEntityUUID("Cannot extract content of file " + file.getOriginalFilename());
				returnList.add(defaultReturnEntity);
				return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
			}
			
		} else {
			sbmlFileNode = this.warehouseGraphService.getFileNode(FileNodeType.SBML, org, file.getOriginalFilename());
			this.provenanceGraphService.connect(persistGraphActivityNode, sbmlFileNode, ProvenanceGraphEdgeType.used);
		}
		// FileNode needs to connect to DatabaseNode
		// what if already connected?
		this.warehouseGraphService.connect(database, sbmlFileNode, WarehouseGraphEdgeType.CONTAINS);
		
		
		List<String> newEntityLabels = new ArrayList<>();
		
		
		Model sbmlModel = null;
		try {
			sbmlModel =  sbmlService.extractSBMLModel(file);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			defaultReturnEntity.setEntityUUID("XMLStreamException while extracting SBMLModel from file");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			defaultReturnEntity.setEntityUUID("IOException while extracting SBMLModel from file");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		if(sbmlModel == null) {
			defaultReturnEntity.setEntityUUID("Could not extract an sbmlModel");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<ProvenanceEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		// create a Pathway Node for the model
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
		
		this.warehouseGraphService.connect(sbmlFileNode, pathwayNode, WarehouseGraphEdgeType.CONTAINS);
		
		
		return new ResponseEntity<List<ProvenanceEntity>>(resultSet, HttpStatus.OK);
	}
	
}
