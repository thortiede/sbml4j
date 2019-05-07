package org.tts.service.oldModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.old.GraphCompartment;
import org.tts.repository.oldModel.CompartmentRepository;

@Service
public class CompartmentServiceImpl implements CompartmentService {

	CompartmentRepository compartmentRepository;
	
	private static final int DEPTH_LIST_ALL = 1;
	private static final int DEPTH_LIST_FIND_BY_ID = 1;
	@Autowired
	public CompartmentServiceImpl(CompartmentRepository compartmentRepository) {
		this.compartmentRepository = compartmentRepository;
	}

	@Transactional
	public GraphCompartment saveOrUpdate(GraphCompartment newCompartment, int depth) {
		return compartmentRepository.save(newCompartment, depth);
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

	@Override
	public List<GraphCompartment> updateCompartmentList(List<GraphCompartment> listCompartment) {
		for (GraphCompartment newCompartment : listCompartment) {
			GraphCompartment existingCompartment = getBySbmlIdString(newCompartment.getSbmlIdString());
			if(existingCompartment != null) {
				newCompartment.setId(existingCompartment.getId());
				newCompartment.setVersion(existingCompartment.getVersion());
			}
		}
		return listCompartment;
	}

}
