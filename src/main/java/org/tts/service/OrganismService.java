package org.tts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tts.model.common.Organism;
import org.tts.repository.common.OrganismRepository;

@Service
public class OrganismService {
	
	
	OrganismRepository organismRepository;
	SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityService;
	
	@Autowired
	public OrganismService(OrganismRepository organismRepository,
			SBMLSimpleModelUtilityServiceImpl sbmlSimpleModelUtilityService) {
		super();
		this.organismRepository = organismRepository;
		this.sbmlSimpleModelUtilityService = sbmlSimpleModelUtilityService;
	}

	public boolean organismExists(String orgCode) {
		if(getOrgansimByOrgCode(orgCode) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public Organism getOrgansimByOrgCode(String orgCode) {
		return organismRepository.findByOrgCode(orgCode);
	}

	public Organism createOrganism(String orgCode) {
		if(organismExists(orgCode)) { // might want to check the orgCode more thoroughly here
			return getOrgansimByOrgCode(orgCode);
		} else {
			Organism newOrganism = new Organism();
			this.sbmlSimpleModelUtilityService.setGraphBaseEntityProperties(newOrganism);
			newOrganism.setOrgCode(orgCode);
			// TODO: set more attributes here, maybe query webservice
			return this.organismRepository.save(newOrganism);
		}
	}
	
	
}
