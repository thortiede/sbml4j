package org.tts.model.full;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.SBMLSBaseEntity;
import org.tts.model.common.SBMLSpecies;

public abstract class SBMLSimpleSpeciesReference extends SBMLSBaseEntity {

	@Relationship(type="HAS", direction = Relationship.OUTGOING)
	private SBMLSpecies species;
}
