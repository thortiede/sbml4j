package org.tts.model.flat.relationship;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.tts.model.flat.FlatEdge;

@RelationshipEntity(type="STIMULATION")
public class StimulationFlatEdge extends FlatEdge {
	@Override
	public String getTypeString() {
		return "STIMULATION";
	}
}
