package org.tts.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label="SBase")
public class SBMLSBaseEntity extends GraphBaseEntity {

	private String sbaseId;
	
	private String sBaseName;
	
	private String sBaseMetaId;
	
	private String sBaseSboTerm;
	
	private String sBaseNotes;
	
	@Properties
	private Map<String, String> sBaseAnnotation = new HashMap<>();
	
	@Relationship(type="IN_MODEL", direction = Relationship.OUTGOING)
	private List<SBMLModelEntity> models = new ArrayList<>();
	
}
