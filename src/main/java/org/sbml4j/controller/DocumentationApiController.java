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
package org.sbml4j.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.sbml4j.api.DocumentationApi;
import org.sbml4j.model.base.GraphBaseEntity;
import org.sbml4j.service.ConfigService;
import org.sbml4j.service.base.GraphBaseEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.view.RedirectView;

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
	
	@Autowired
	private ConfigService configService;
	
	Logger log = LoggerFactory.getLogger(NetworksApiController.class);
	
	/**
	 * get a static representation of the API Description
	 * 
	 * @return static HTML page containing api endpoint descriptions
	 */
	public RedirectView getBaseDocumentation() {
		String redirectUrl = this.configService.getApi_documentation_url();
		log.info("Serving api documentation. Redirecting to " +redirectUrl);
		return new RedirectView(redirectUrl);
	}
	
	/**
	 * get the version of SBML4j
	 * Needs to be updated manually. For development purposes only.
	 * 
	 * @return String containing the current version of SBML4j
	 */
	public ResponseEntity<String> getVersion() {
		return new ResponseEntity<String>("1.2.2", HttpStatus.OK);
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
	
	/**
	 * Check the assigned memory
	 * 
	 * This method will check and print the initial heapMemory and max heapMemory available
	 * The results are printed to log (INFO level) and are returned as String
	 * 
	 * @return String containing the memory settings
	 */
	public ResponseEntity<String> getMemorySetup() {
		int mb = 1024 * 1024;
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		long xmx = memoryBean.getHeapMemoryUsage().getMax() / mb;
		long xms = memoryBean.getHeapMemoryUsage().getInit() / mb;
		log.info("Initial Memory (xms): " +xms +"mb");
		log.info("Max Memory (xmx): "+ xmx + "mb");
		return new ResponseEntity<String>("Initial Memory (xms): " + xms + "mb / Max Memory (xmx): " + xmx + "mb", HttpStatus.OK);
	}
	
}
