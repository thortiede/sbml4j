package org.tts.model.flat.relationship;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.tts.model.flat.FlatEdge;

@RelationshipEntity(type="PHOSPHORYLATION")
public class PhosphorylationFlatEdge extends FlatEdge {
	@Override
	public String getTypeString() {
		return "PHOSPHORYLATION";
	}
}
