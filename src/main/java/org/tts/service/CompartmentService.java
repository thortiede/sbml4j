package org.tts.service;

import java.util.List;

import org.tts.model.GraphCompartment;

public interface CompartmentService {

	GraphCompartment saveOrUpdate(GraphCompartment newCompartment);
	
	List<GraphCompartment> listAll();
	
	GraphCompartment getById(Long id);
	
	GraphCompartment getBySbmlName(String sbmlName);
	
	GraphCompartment getBySbmlIdString(String sbmlIdString);
	
	
	
}
