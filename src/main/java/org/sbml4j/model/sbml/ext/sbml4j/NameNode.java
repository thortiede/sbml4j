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
package org.sbml4j.model.sbml.ext.sbml4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;
import org.sbml4j.model.provenance.ProvenanceGraphEntityNode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class NameNode extends ProvenanceGraphEntityNode {

	@Index(unique=true)
	private String name;
	
	@Relationship(type = "KNOWNAS", direction = Relationship.INCOMING)
	private List<ExternalResourceEntity> resources;

	@Transient
	private Set<String> resourceUUIDs;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public List<ExternalResourceEntity> getResources() {
		return resources;
	}

	public void setResources(List<ExternalResourceEntity> resources) {
		this.resources = resources;
		this.resourceUUIDs = new HashSet<>();
		for (ExternalResourceEntity e : this.resources) {
			resourceUUIDs.add(e.getEntityUUID());
		}
	}
	
	public void addResource(ExternalResourceEntity resource) {
		if (this.resources == null) {
			this.resources = new ArrayList<>();
		}
		if (this.resourceUUIDs == null) {
			this.resourceUUIDs = new HashSet<>();
			for (ExternalResourceEntity e : this.resources) {
				resourceUUIDs.add(e.getEntityUUID());
			}
		}
		if (this.resourceUUIDs.add(resource.getEntityUUID())) {
			this.resources.add(resource);
		}
	}

}
