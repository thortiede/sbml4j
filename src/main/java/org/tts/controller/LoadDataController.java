package org.tts.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.GraphBaseEntity;
import org.tts.model.NodeEdgeList;
import org.tts.model.SBMLSBaseEntity;
import org.tts.model.SifFile;
import org.tts.service.FileCheckService;
import org.tts.service.FileService;
import org.tts.service.FileStorageService;
import org.tts.service.NodeEdgeListService;
import org.tts.service.SBMLPersistenceService;
import org.tts.service.SBMLService;

@RestController
public class LoadDataController {

	FileCheckService fileCheckService;
	FileStorageService fileStorageService;
	SBMLService sbmlService;
	SBMLPersistenceService sbmlPersistenceService;
	FileService fileService;
	NodeEdgeListService nodeEdgeListService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public LoadDataController(FileStorageService fileStorageService, 
			FileCheckService fileCheckService, 
			@Qualifier("SBMLSimpleModelServiceImpl") SBMLService sbmlService, 
			SBMLPersistenceService sbmlPersistenceService,
			FileService fileService,
			@Qualifier("simpleNodeEdgeListServiceImpl") NodeEdgeListService nodeEdgeListService) {
		super();
		this.fileStorageService = fileStorageService;
		this.fileCheckService = fileCheckService;
		this.sbmlService = sbmlService;
		this.sbmlPersistenceService = sbmlPersistenceService;
		this.fileService = fileService;
		this.nodeEdgeListService = nodeEdgeListService;
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
	 * @return all Entities as they were persisted (or connected if already present) as a result persisting the model
	 */
	@RequestMapping(value = "/sbml", method=RequestMethod.POST)
	public ResponseEntity<List<GraphBaseEntity>> uploadSBML(@RequestParam("file") MultipartFile file) {
		// can we access the files name and ContentType?
		List<GraphBaseEntity> returnList = new ArrayList<>();
		GraphBaseEntity defaultReturnEntity = new GraphBaseEntity();
		if(!fileCheckService.isFileReadable(file)) {
			defaultReturnEntity.setEntityUUID("Cannot read file");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		logger.info("Serving POST uploadSBML for File " + file.getOriginalFilename());
		// is Content Type xml?
		if(!fileCheckService.isContentXML(file)) {
			defaultReturnEntity.setEntityUUID("File ContentType is not application/xml");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		Model sbmlModel = null;
		try {
			sbmlModel =  sbmlService.extractSBMLModel(file);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			defaultReturnEntity.setEntityUUID("XMLStreamException while extracting SBMLModel from file");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			defaultReturnEntity.setEntityUUID("IOException while extracting SBMLModel from file");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		if(sbmlModel == null) {
			defaultReturnEntity.setEntityUUID("Could not extract an sbmlModel");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		List<GraphBaseEntity> resultSet = sbmlService.buildAndPersist(sbmlModel, file.getOriginalFilename());
		return new ResponseEntity<List<GraphBaseEntity>>(resultSet, HttpStatus.OK);
	}
	
}
