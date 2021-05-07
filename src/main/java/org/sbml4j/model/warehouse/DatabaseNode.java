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
package org.tts.model.warehouse;

import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.provenance.ProvenanceGraphEntityNode;

public class DatabaseNode extends WarehouseGraphNode {

	private String source;
	private String sourceVersion;
	private Map<String, String> matchingAttributes;

	
	@Relationship(type="HAS_ENTITY")
	private List<ProvenanceGraphEntityNode> entities;
	
	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	public String getSourceVersion() {
		return sourceVersion;
	}


	public void setSourceVersion(String sourceVersion) {
		this.sourceVersion = sourceVersion;
	}



	public Map<String, String> getMatchingAttributes() {
		return matchingAttributes;
	}


	public void setMatchingAttributes(Map<String, String> matchingAttributes) {
		this.matchingAttributes = matchingAttributes;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((matchingAttributes == null) ? 0 : matchingAttributes.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((sourceVersion == null) ? 0 : sourceVersion.hashCode());
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
		DatabaseNode other = (DatabaseNode) obj;
		if (matchingAttributes == null) {
			if (other.matchingAttributes != null)
				return false;
		} else if (!matchingAttributes.equals(other.matchingAttributes))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (sourceVersion == null) {
			if (other.sourceVersion != null)
				return false;
		} else if (!sourceVersion.equals(other.sourceVersion))
			return false;
		return true;
	}
	
	
	
}
