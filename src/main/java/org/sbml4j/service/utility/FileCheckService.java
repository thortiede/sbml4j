/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.service.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Service to check uploaded files
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Service
public class FileCheckService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Check if the given <a href="#{@link}">{@link MultipartFile}</a> is readable
	 * 
	 * Attemps to read the originalFilename and the ContentType of the <a href="#{@link}">{@link MultipartFile}</a>
	 * 
	 * @param file The <a href="#{@link}">{@link MultipartFile}</a> to check
	 * @return true if originalFilename and ContentType are accessible, false otherwise
	 */
	public boolean isFileReadable(MultipartFile file) {
		try {
			logger.debug("ContentType of file " + file.getOriginalFilename() + " is " + file.getContentType());
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	/**
	 * Check whether the ContentType of <a href="#{@link}">{@link MultipartFile}</a> file is xml or sbml
	 * 
	 * @param file The <a href="#{@link}">{@link MultipartFile}</a> to check
	 * @return true if the ContentType is either application/xml, application/sbml+xml or application/sbml, false otherwise
	 */
	public boolean isContentXML(MultipartFile file) {
		if(file.getContentType() != null && (file.getContentType().equals("application/xml")
												|| file.getContentType().equals("application/sbml+xml")
												|| file.getContentType().equals("application/sbml")
												|| file.getContentType().equals("text/xml"))) {
			return true;
		} else {
			logger.info("file content type is: " + file.getContentType());
			return false;
		}
	}
	
	
	public String getMD5Sum(MultipartFile file) throws IOException {
		return DigestUtils
			      .md5Hex(file.getBytes()).toUpperCase();
	}
}
