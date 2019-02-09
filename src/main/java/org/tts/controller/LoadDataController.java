package org.tts.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tts.model.SBMLSBaseEntity;
import org.tts.service.FileCheckService;
import org.tts.service.FileStorageService;
import org.tts.service.SBMLPersistenceService;
import org.tts.service.SBMLService;

@RestController
public class LoadDataController {

	FileCheckService fileCheckService;
	FileStorageService fileStorageService;
	SBMLService sbmlService;
	SBMLPersistenceService sbmlPersistenceService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public LoadDataController(FileStorageService fileStorageService, FileCheckService fileCheckService, SBMLService sbmlService, SBMLPersistenceService sbmlPersistenceService) {
		super();
		this.fileStorageService = fileStorageService;
		this.fileCheckService = fileCheckService;
		this.sbmlService = sbmlService;
		this.sbmlPersistenceService = sbmlPersistenceService;
	}
	
	@RequestMapping(value = "/uploadSBML", method=RequestMethod.POST)
	public ResponseEntity<String> uploadSBML(@RequestParam("file") MultipartFile file) {
		// can we access the files name and ContentType?
		if(!fileCheckService.isFileReadable(file)) {
			return new ResponseEntity<String>("Cannot read file", HttpStatus.BAD_REQUEST);
		}
		// is Content Type xml?
		if(!fileCheckService.isContentXML(file)) {
			return new ResponseEntity<String>("File ContentType is not application/xml", HttpStatus.BAD_REQUEST);
		}
		
		
		// Make contents available in File Resource
		/*File f = new File(StringUtils.cleanPath(file.getOriginalFilename()));
		try {
			f = fileStorageService.convertToFile(file, f);
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<String>("File cannot be converted to File Object", HttpStatus.BAD_REQUEST);
		}*/
		// then also store the file
		/*String storedFileName = fileStorageService.storeFile(file);
		logger.info("Stored file " + file.getOriginalFilename() + " as " + storedFileName);*/
		
		// now build our databaseEntities from the model in the file
		// might need to divide this into multiple steps
		Model sbmlModel = null;
		
		try {
			sbmlModel =  sbmlService.extractSBMLModel(file);
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<String>("XMLStreamException while extracting SBMLModel from file", HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<String>("IOException while extracting SBMLModel from file", HttpStatus.BAD_REQUEST);
		}
		if(sbmlModel == null) {
			return new ResponseEntity<String>("Could not extract an sbmlModel", HttpStatus.BAD_REQUEST);
		}
		Map<String, Iterable<SBMLSBaseEntity>> allEntities = sbmlService.extractSBMLEntities(sbmlModel);
		Iterable<SBMLSBaseEntity> persistedEntities = sbmlPersistenceService.saveAll(allEntities);
		
		
		return new ResponseEntity<String>("All good so far", HttpStatus.OK);
	}
	
	
	
	
}
