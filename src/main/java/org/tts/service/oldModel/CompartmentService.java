package org.tts.service.oldModel;

import java.util.List;

import org.tts.model.old.GraphCompartment;

public interface CompartmentService {

	GraphCompartment saveOrUpdate(GraphCompartment newCompartment, int depth);
	
	List<GraphCompartment> listAll();
	
	GraphCompartment getById(Long id);
	
	GraphCompartment getBySbmlName(String sbmlName);
	
	GraphCompartment getBySbmlIdString(String sbmlIdString);

	List<GraphCompartment> updateCompartmentList(List<GraphCompartment> listCompartment);
	
	
	
}
