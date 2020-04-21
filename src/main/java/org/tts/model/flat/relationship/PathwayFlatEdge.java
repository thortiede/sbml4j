package org.tts.model.flat.relationship;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.tts.model.flat.FlatEdge;

@RelationshipEntity(type="SHAREDPATHWAY")
public class PathwayFlatEdge extends FlatEdge {

	@Override
	public String getTypeString() {
		return "SHAREDPATHWAY";
	}
}
