package org.sbml4j.repository.common;

import java.util.List;

import org.sbml4j.model.sbml.SBMLCompartment;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SBMLCompartmentRepository extends Neo4jRepository<SBMLCompartment, Long>{

	List<SBMLCompartment> findBysBaseId(String sBaseId, int depth);
	
}
