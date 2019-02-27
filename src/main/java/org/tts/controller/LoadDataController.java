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
	 * Endpoint for simple model
	 * @param file
	 * @return
	 * 
	 */
	@RequestMapping(value="uploadSBMLSimple", method=RequestMethod.POST)
	public ResponseEntity<List<GraphBaseEntity>> uploadSBMLSimple(@RequestParam("file") MultipartFile file) {
		logger.info("Serving POST uploadSBMLSimple for File " + file.getOriginalFilename());
		List<GraphBaseEntity> returnList = new ArrayList<>();
		GraphBaseEntity defaultReturnEntity = new GraphBaseEntity();
		Model jsbmlModel = null;
		try {
			jsbmlModel = sbmlService.extractSBMLModel(file);
			// the old way, do not use this anymore
			List<GraphBaseEntity> persistedEntities = sbmlService.buildAndPersist(jsbmlModel, file.getOriginalFilename());
			logger.info("Finished Persisting for POST uploadSBMLSimple for File " + file.getOriginalFilename());
			return new ResponseEntity<List<GraphBaseEntity>>(persistedEntities, HttpStatus.OK);
		} catch (XMLStreamException | IOException e) {
			defaultReturnEntity.setEntityUUID("Problem extracting the model");
			returnList.add(defaultReturnEntity);
			e.printStackTrace();
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			defaultReturnEntity.setEntityUUID("Problem occured: " + e.getMessage());
			returnList.add(defaultReturnEntity);
			e.printStackTrace();
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@RequestMapping(value="/simple/transitionList", method=RequestMethod.GET)
	public ResponseEntity<Resource> getTransitionList() {
		
		logger.info("Requesting transitionList");
		NodeEdgeList fullList = nodeEdgeListService.getFullNet();
		SifFile sifFile = fileService.getSifFromNodeEdgeList(fullList);
		
        //Resource resource = fileStorageService.loadFileAsResource(fileName);
        Resource resource = fileStorageService.getSifAsResource(sifFile);
        logger.info("Fetched sifResource");
        // Try to determine file's content type
        String contentType = null;
      
        // only generic contentType as we are not dealing with an actual file serverside,
        // but the file shall only be created at the client side.
        contentType = "application/octet-stream";
        String filename = "unnamed";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
	}
	
	@RequestMapping(value="/allEntities", method=RequestMethod.GET)
	public ResponseEntity<List<GraphBaseEntity>> showAllEntites() {
		logger.info("Serving GET allEntities");
		return new ResponseEntity<List<GraphBaseEntity>>(sbmlService.getAllEntities(), HttpStatus.OK);
	}
	
	@RequestMapping(value="/allEntities", method=RequestMethod.DELETE)
	public ResponseEntity<String> deleteAllEntites() {
		logger.info("Serving DELETE allEntities");
		if(this.sbmlService.clearDatabase()) {
			return new ResponseEntity<String>("Database is clear!", HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Database is not clear!", HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@RequestMapping(value = "/uploadSBML", method=RequestMethod.POST)
	public ResponseEntity<List<GraphBaseEntity>> uploadSBML(@RequestParam("file") MultipartFile file) {
		// can we access the files name and ContentType?
		List<GraphBaseEntity> returnList = new ArrayList<>();
		GraphBaseEntity defaultReturnEntity = new GraphBaseEntity();
		if(!fileCheckService.isFileReadable(file)) {
			defaultReturnEntity.setEntityUUID("Cannot read file");
			returnList.add(defaultReturnEntity);
			return new ResponseEntity<List<GraphBaseEntity>>(returnList, HttpStatus.BAD_REQUEST);
		}
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
		
		//SBMLDocumentEntity sbmlDocumentEntity = sbmlService.getSBMLDocument(sbmlModel, file.getOriginalFilename());
		
		
		
		return new ResponseEntity<List<GraphBaseEntity>>(resultSet, HttpStatus.OK);
	}
	
}
