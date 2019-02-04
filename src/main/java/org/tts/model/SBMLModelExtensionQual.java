package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLModelExtensionQual extends SBMLSBaseEntity implements SBMLModelExtension {

	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	List<SBMLQualSpecies> sbmlQualSpecies;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	List<SBMLQualTransition> sbmlQualTransitions;
}
