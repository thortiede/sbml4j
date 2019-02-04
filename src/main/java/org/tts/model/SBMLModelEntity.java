package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLModelEntity extends SBMLSBaseEntity {

	private String substanceUnits;

	private String timeUnits;
	
	private String volumeUnits;
	
	private String areaUnits;
	
	private String lengthUnits;
	
	private String extentUnits;
	
	private String conversionFactor;
	
	@Relationship(type = "CONTAINS", direction = Relationship.INCOMING)
	private SBMLEntity enclosingSBMLEntity;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLFunctionDefinition> sbmlFunctionDefinitions;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLUnitDefintion> sbmlUnitDefinitions;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLCompartment> sbmlCompartments;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLSpecies> sbmlSpecies;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLParameter> sbmlParameters;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLInitialAssignment> sbmlInitialAssigments;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLRule> sbmlRules;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLConstraint> sbmlConstraints;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLReaction> sbmlReactions;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLEvent> sbmlEvents;

}
