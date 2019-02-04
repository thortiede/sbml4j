package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLPackage extends GraphBaseEntity {

	private boolean required;
	
	private String packageShortLabel;
	
	private String packageNamespaceURI;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private SBMLModelExtension modelExtension;

	
}
