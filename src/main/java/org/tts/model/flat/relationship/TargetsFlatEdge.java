package org.tts.model.flat.relationship;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.tts.model.flat.FlatEdge;

@RelationshipEntity(type="TARGETS")
public class TargetsFlatEdge extends FlatEdge {
	@Override
	public String getTypeString() {
		return "TARGETS";
	}
}
