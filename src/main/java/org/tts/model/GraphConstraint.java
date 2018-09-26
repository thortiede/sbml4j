package org.tts.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;
import org.sbml.jsbml.Constraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class GraphConstraint {

	@Id @GeneratedValue
	private Long id = null;

	@Version
	private Long version;
	
	@JsonIgnore
	@Relationship(type="hasConstraint", direction = Relationship.INCOMING)
	GraphModel model;
	
	public GraphConstraint() {}
	
	// Constructor to create a constraint object that can be persisted in the graph database
	// from a jsbml-constraint instance
	// KGML-Models do not appear to have constraints at the moment. Postponing implementation.
	public GraphConstraint(Constraint constraint) {
		
	}
	
}
