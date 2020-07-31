package org.tts.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileCheckServiceImpl implements FileCheckService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public boolean isFileReadable(MultipartFile file) {
		try {
			logger.debug("ContentType of file " + file.getOriginalFilename() + " is " + file.getContentType());
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isContentXML(MultipartFile file) {
		if(file.getContentType() != null && (file.getContentType().equals("application/xml")
												|| file.getContentType().equals("application/sbml+xml")
												|| file.getContentType().equals("application/sbml"))) {
			return true;
		} else {
			logger.info("file content type is: " + file.getContentType());
			return false;
		}
	
	}

	@Override
	public boolean isFileValidSBML(File file) {
		// TODO Auto-generated method stub
		return false;
	}


	
	
}
