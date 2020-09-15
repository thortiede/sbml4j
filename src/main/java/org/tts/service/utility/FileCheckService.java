/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
 */

package org.tts.service.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
												|| file.getContentType().equals("application/sbml"))) {
			return true;
		} else {
			logger.info("file content type is: " + file.getContentType());
			return false;
		}
	}
}
