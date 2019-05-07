package org.tts.model;

import org.neo4j.ogm.annotation.Relationship;

public abstract class SBMLSimpleSpeciesReference extends SBMLSBaseEntity {

	@Relationship(type="HAS", direction = Relationship.OUTGOING)
	private SBMLSpecies species;
}
