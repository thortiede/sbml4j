/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.tts.model.common;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.sbml.jsbml.CVTerm;

import com.fasterxml.jackson.annotation.JsonIgnore;

@RelationshipEntity(type = "BQ")
public class BiomodelsQualifier extends ContentGraphEdge {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endNode == null) ? 0 : endNode.getEntityUUID().hashCode());
		result = prime * result + ((qualifier == null) ? 0 : qualifier.hashCode());
		result = prime * result + ((startNode == null) ? 0 : startNode.getEntityUUID().hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BiomodelsQualifier other = (BiomodelsQualifier) obj;
		if (endNode == null) {
			if (other.endNode != null)
				return false;
		} else if (!endNode.getEntityUUID().equals(other.endNode.getEntityUUID()))
			return false;
		if (qualifier != other.qualifier)
			return false;
		if (startNode == null) {
			if (other.startNode != null)
				return false;
		} else if (!startNode.getEntityUUID().equals(other.startNode.getEntityUUID()))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	private CVTerm.Type type;
	
	private CVTerm.Qualifier qualifier;

	@StartNode
	private SBMLSBaseEntity startNode;
	
	@EndNode
	private ExternalResourceEntity endNode;
	
	@JsonIgnore
	public SBMLSBaseEntity getStartNode() {
		return startNode;
	}

	public void setStartNode(SBMLSBaseEntity startNode) {
		this.startNode = startNode;
	}

	public ExternalResourceEntity getEndNode() {
		return endNode;
	}

	public void setEndNode(ExternalResourceEntity endNode) {
		this.endNode = endNode;
	}

	public CVTerm.Type getType() {
		return type;
	}

	public void setType(CVTerm.Type type) {
		this.type = type;
	}

	public CVTerm.Qualifier getQualifier() {
		return qualifier;
	}

	public void setQualifier(CVTerm.Qualifier qualifier) {
		this.qualifier = qualifier;
	}
}
