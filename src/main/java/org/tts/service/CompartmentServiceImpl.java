package org.tts.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.GraphCompartment;
import org.tts.repository.CompartmentRepository;

@Service
public class CompartmentServiceImpl implements CompartmentService {

	CompartmentRepository compartmentRepository;
	
	private static final int DEPTH_LIST_ALL = 1;
	private static final int DEPTH_LIST_FIND_BY_ID = 1;
	@Autowired
	public CompartmentServiceImpl(CompartmentRepository compartmentRepository) {
		this.compartmentRepository = compartmentRepository;
	}

	@Override
	public GraphCompartment saveOrUpdate(GraphCompartment newCompartment) {
		return compartmentRepository.save(newCompartment, 1);
	}

	@Override
	public List<GraphCompartment> listAll() {
		List<GraphCompartment> compartments = new ArrayList<GraphCompartment>();
		compartmentRepository.findAll(DEPTH_LIST_ALL).forEach(compartments::add);
		return compartments;
	}

	@Override
	public GraphCompartment getById(Long id) {
		if (id == null) {
			return null; // maybe not return null, but allow for error handling in a way that the controller knows about the id problem
		}
		return compartmentRepository.findById(id, DEPTH_LIST_FIND_BY_ID).orElse(null);
	}

	@Override
	public GraphCompartment getBySbmlName(String sbmlName) {
		return compartmentRepository.getBySbmlName(sbmlName);
	}

	@Override
	public GraphCompartment getBySbmlIdString(String sbmlIdString) {
		return compartmentRepository.getBySbmlIdString(sbmlIdString);
	}

}
