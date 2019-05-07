package org.tts.controller.graph_old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;


import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.qual.QualConstants;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tts.model.old.FileInfoObject;

@RestController
public class FileController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String QUAL_NS = QualConstants.namespaceURI;
	
	public FileController()
	{
		
	}
	
	
	@RequestMapping(value="/fileinfo", method = RequestMethod.GET)
	public ResponseEntity<FileInfoObject> getFileInfo(@RequestParam(value="name") String filepath) {
		
		FileInfoObject fileInfo = getFileInfoObject(filepath);
		
		if(fileInfo != null) {
			return new ResponseEntity<FileInfoObject>(fileInfo, HttpStatus.OK);
		} else {
			return new ResponseEntity<FileInfoObject>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

	@RequestMapping(value = "/folderinfo", method = RequestMethod.GET)
	public ResponseEntity<List<FileInfoObject>> getFolderInfo(@RequestParam(value="name") String folderName) {
		
		return new ResponseEntity<List<FileInfoObject>>(getListFileObjects(folderName), HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/folderinfosplit",  method = RequestMethod.GET)
	public ResponseEntity<Map<String, List<FileInfoObject>>> getFolderInfoSplit(@RequestParam(value="name") String folderName) {
		List<FileInfoObject> allFIO = getListFileObjects(folderName);
		List<FileInfoObject> emptyFIOs = new ArrayList<>();
		List<FileInfoObject> filledFIOs = new ArrayList<>();
		for (FileInfoObject fio : allFIO) {
			if(fio.isQualitativeModel() && (fio.getNumQualSpecies() == 0 || fio.getNumTransitions() == 0)) 
			{
				emptyFIOs.add(fio);
			} else if (!fio.isQualitativeModel() && (fio.getNumSpecies() == 0 || fio.getNumReactions() == 0))
			{
				emptyFIOs.add(fio);
			} else {
				filledFIOs.add(fio);
			}
		}
		// separated the sets, build the map
		Map<String, List<FileInfoObject>> infoSplit = new HashMap<>();
		infoSplit.put("Empty Models", emptyFIOs);
		infoSplit.put("regular Models", filledFIOs);
		
		return new ResponseEntity<Map<String, List<FileInfoObject>>>(infoSplit, HttpStatus.OK);
		
	}
	

	private List<FileInfoObject> getListFileObjects(String folderName) 
	{
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		List<FileInfoObject> folderInfo = new ArrayList<>();
		for (File file : listOfFiles) {
			if(file.getName().contains(".xml")) {
				folderInfo.add(getFileInfoObject(file.getAbsolutePath()));
			} else {
				logger.info("Unknown File: " + file.getName());
			}
		}
		return folderInfo;
	}
	
	
	private FileInfoObject getFileInfoObject(String filepath) {
		FileInfoObject fileInfo = new FileInfoObject (filepath);
		try {
			SBMLDocument doc = SBMLReader.read(new File(filepath));
			Model model = doc.getModel();
			fileInfo.setModelName(model.getName());
			fileInfo.setNumSpecies(model.getListOfSpecies().size());
			fileInfo.setNumReactions(model.getListOfReactions().size());
			Map<String, SBasePlugin> extensionPackages = model.getExtensionPackages();
			
			
			
			
			Set<String> keys = extensionPackages.keySet();
			for (String key : keys) 
			{
				logger.info("Extension " + key + ": " + extensionPackages.get(key));
			}
			
			// is it a qualitative model
			if(extensionPackages.containsKey("qual")) {
				logger.info("Qualitative Model");
				fileInfo.setQualitativeModel(true);
				QualModelPlugin qualModelPlugin = (QualModelPlugin) model.getExtension(QUAL_NS);
				
				fileInfo.setNumQualSpecies(qualModelPlugin.getNumQualitativeSpecies());
				fileInfo.setNumTransitions(qualModelPlugin.getNumTransitions());
				
				if(qualModelPlugin.getListOfTransitions().size() > 0) {
					if(qualModelPlugin.getTransition(0).getAnnotation() != null) {
						fileInfo.setNumTransitionAnnotationCVTerms(qualModelPlugin.getTransition(0).getAnnotation().getCVTermCount());
					}
					if(qualModelPlugin.getTransition(0).getCVTerms() != null) {
						fileInfo.setNumTransitonCVTerms(qualModelPlugin.getTransition(0).getCVTermCount());
					}
				}
				
				
			} else {	
				logger.info("Non-qualitative Model");
				fileInfo.setQualitativeModel(false);
				fileInfo.setNumQualSpecies(0);
				fileInfo.setNumTransitions(0);
			}
			return fileInfo;
			
			
			
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
}
