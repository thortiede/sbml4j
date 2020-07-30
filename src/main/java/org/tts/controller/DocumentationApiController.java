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


@Controller
public class DocumentationApiController implements DocumentationApi {

	@Autowired
	private Environment environment;
	
	@Autowired
	private GraphBaseEntityService graphBaseEntityService;
	
	
	public ResponseEntity<Object> getBaseDocumentation() {
		return new ResponseEntity<>("index", HttpStatus.OK);
	}
	
	
	public ResponseEntity<String> getVersion() {
		return new ResponseEntity<String>("0.0.26-SNAPSHOT", HttpStatus.OK);
	}
	
	public ResponseEntity<List<String>> getProfile() {
		List<String> activeProfiles =  Arrays.asList(environment.getActiveProfiles());
		return new ResponseEntity<>(activeProfiles, HttpStatus.OK);
	}
	
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
