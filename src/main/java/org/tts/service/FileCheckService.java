package org.tts.service;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

public interface FileCheckService {

	public boolean isFileReadable(MultipartFile file);
	
	public boolean isContentXML(MultipartFile file);
	
	public boolean isFileValidSBML(File file);
}
