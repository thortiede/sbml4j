package org.tts.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tts.model.GraphCompartment;
import org.tts.model.GraphSpecies;
import org.tts.repository.SpeciesRepository;

@Service
public class SpeciesServiceImpl implements SpeciesService {

	SpeciesRepository speciesRepository;
	
	@Autowired
	public SpeciesServiceImpl(SpeciesRepository speciesRepository) {
		this.speciesRepository = speciesRepository;
	}

	@Transactional
	public GraphSpecies saveOrUpdate(GraphSpecies newSpecies) {
		GraphSpecies existingSpecies = speciesRepository.getBySbmlIdString(newSpecies.getSbmlIdString());
		if(existingSpecies != null)
		{
			newSpecies.setId(existingSpecies.getId());
			newSpecies.setVersion(existingSpecies.getVersion());
		}
		try {
			return speciesRepository.save(newSpecies, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Tried persisting species " + newSpecies.getName() + " with version " + newSpecies.getVersion());
			e.printStackTrace();
			
			return  null;
		}
	}

	@Override
	public GraphSpecies getBySbmlIdString(String sbmlIdString) {
		return speciesRepository.getBySbmlIdString(sbmlIdString);
	}

	@Override
	public List<GraphSpecies> updateSpeciesList(List<GraphSpecies> listSpecies) {
		for (GraphSpecies newSpecies : listSpecies) {
			GraphSpecies existingSpecies = getBySbmlIdString(newSpecies.getSbmlIdString());
			if(existingSpecies != null) {
				newSpecies.setId(existingSpecies.getId());
				newSpecies.setVersion(existingSpecies.getVersion());
			}
		}
		return listSpecies;
	}

}
