package org.tts.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.tts.model.common.GraphBaseEntity;
import org.tts.service.GraphBaseEntityService;


@Controller
public class DocumentationController {

	@Autowired
	private Environment environment;
	
	@Autowired
	private GraphBaseEntityService graphBaseEntityService;
	
	@GetMapping("/help")
	public String getBaseDocumentation() {
		return "index";
	}
	
	@GetMapping("/version")
	public ResponseEntity<String> getVersion() {
		return new ResponseEntity<String>("0.0.26-SNAPSHOT", HttpStatus.OK);
	}
	
	@GetMapping("/profile")
	public ResponseEntity<String[]> getProfile() {
		String[] activeProfiles = environment.getActiveProfiles();
		return new ResponseEntity<String[]>(activeProfiles, HttpStatus.OK);
	}
	
	@GetMapping("/dbStatus")
	public ResponseEntity<String> getDbStatus() {
		try {
			GraphBaseEntity newEntity = new GraphBaseEntity();
			newEntity.setEntityUUID(UUID.randomUUID().toString());
			GraphBaseEntity testEntity =  this.graphBaseEntityService.persistEntity(newEntity);
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
