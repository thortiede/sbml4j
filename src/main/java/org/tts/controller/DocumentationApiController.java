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
package org.tts.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.tts.api.DocumentationApi;
import org.tts.model.common.GraphBaseEntity;
import org.tts.service.GraphBaseEntityService;

/**
 * Controller class for handling Requests to the DocumentationApi
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@Controller
public class DocumentationApiController implements DocumentationApi {

	@Autowired
	private Environment environment;
	
	@Autowired
	private GraphBaseEntityService graphBaseEntityService;
	
	/**
	 * get a static representation of the API Description
	 * 
	 * @return static HTML page containing api endpoint descriptions
	 */
	public ResponseEntity<Object> getBaseDocumentation() {
		return new ResponseEntity<>("index", HttpStatus.OK);
	}
	
	/**
	 * get the version of SBML4j
	 * Needs to be updated manually. For development purposes only.
	 * 
	 * @return String containing the current version of SBML4j
	 */
	public ResponseEntity<String> getVersion() {
		return new ResponseEntity<String>("0.1.8", HttpStatus.OK);
	}
	
	/**
	 * get the list of active profiles
	 * 
	 * @return List of profile name strings
	 */
	public ResponseEntity<List<String>> getProfile() {
		List<String> activeProfiles =  Arrays.asList(environment.getActiveProfiles());
		return new ResponseEntity<>(activeProfiles, HttpStatus.OK);
	}
	
	/**
	 * Check the status of the connected database.
	 * 
	 * This method will attempt to create a simple entity and delete it again.
	 * If successful, it returns a positive String. Otherwise tries to give a reason for malfunction.
	 * 
	 * @return String containing the status of the database
	 */
	public ResponseEntity<String> getDbStatus() {
		try {
			GraphBaseEntity newEntity = new GraphBaseEntity();
			newEntity.setEntityUUID(UUID.randomUUID().toString());
			GraphBaseEntity testEntity =  this.graphBaseEntityService.persistEntity(newEntity, 0);
			if (testEntity != null) {
				GraphBaseEntity controlTestEntity = this.graphBaseEntityService.findByEntityUUID(testEntity.getEntityUUID());
				if (controlTestEntity != null) {
					if (this.graphBaseEntityService.deleteEntity(controlTestEntity) == true) {
						return new ResponseEntity<String>("Database operational", HttpStatus.OK);
					}
				}
			}
			return new ResponseEntity<String>("Database not operational", HttpStatus.OK);
		} catch (org.neo4j.driver.exceptions.ServiceUnavailableException e) {
			return new ResponseEntity<String>("Database not operational (Service unavailable)", HttpStatus.OK);
		} catch (org.neo4j.ogm.exception.ConnectionException e) {
			return new ResponseEntity<String>("Database not operational (Cannot open session for transaction)", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>("Database not operational ( " + e.getMessage() + ")", HttpStatus.OK);
		}
	}
	
}
