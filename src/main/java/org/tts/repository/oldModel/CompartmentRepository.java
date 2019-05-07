package org.tts.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.GraphCompartment;

public interface CompartmentRepository extends Neo4jRepository<GraphCompartment, Long> {

	
	GraphCompartment getBySbmlName(String sbmlName);
	
	GraphCompartment getBySbmlIdString(String sbmlIdString);
	
	
}
