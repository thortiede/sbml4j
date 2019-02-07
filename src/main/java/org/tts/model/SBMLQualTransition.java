package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLQualTransition extends SBMLSBaseEntity {

	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLQualInput> inputs;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLQualOutput> outputs;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLFunctionTerm> functionTerms;
	
}
