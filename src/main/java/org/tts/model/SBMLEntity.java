package org.tts.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label = "SBML")
public class SBMLEntity extends SBMLSBaseEntity {

	private String sbmlXmlNamespace;
	
	private int sbmlLevel;
	
	private int sbmlVersion;
	
	private String sbmlFileName;
	
	@Properties
	private Map<String, String> extensions  = new HashMap<>();
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private SBMLModelEntity model;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLPackage> sbmlPackages;
	
	
}
