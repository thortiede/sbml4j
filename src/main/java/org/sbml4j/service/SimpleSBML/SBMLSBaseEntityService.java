package org.sbml4j.service.SimpleSBML;

import org.sbml4j.model.common.GraphBaseEntity;
import org.sbml4j.model.sbml.SBMLSBaseEntity;
import org.sbml4j.repository.common.SBMLSBaseEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SBMLSBaseEntityService {

	@Autowired
	SBMLSBaseEntityRepository sbmlSBaseEntityRepository;
	
	/**
	 * Persist the provided {@link SBMLSBaseEntity} and all connected {@link GraphBaseEntity} up to the provided depth
	 * @param sbase The {@link SBMLSBaseEntity} to persist
	 * @param depth The number of relationships to traverse to look for {@link GraphBaseEntity} to persist
	 * @return The persisted {@link SBMLSBaseEntity} from the database
	 */
	public SBMLSBaseEntity save(SBMLSBaseEntity sbase, int depth) {
		return this.sbmlSBaseEntityRepository.save(sbase, depth);
	}
	
	
}
