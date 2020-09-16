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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.GraphBaseEntity;
import org.tts.model.provenance.ProvenanceGraphEntityNode;

public class DatabaseNode extends WarehouseGraphNode {

	private String source;
	private String sourceVersion;
	private Map<String, String> matchingAttributes;
		
	private Map<String, String> entityKeyMap;
	
	private Map<String, GraphBaseEntity> nameEntityMap;
	
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


	public Map<String, String> getEntityKeyMap() {
		return entityKeyMap;
	}


	public void setEntityKeyMap(Map<String, String> entityKeyMap) {
		this.entityKeyMap = entityKeyMap;
	}


	public boolean contains(String entityName) {
		if(nameEntityMap != null) {
			if(nameEntityMap.containsKey(entityName)) {
				return true;
			}
		}
		return false;
	}


	public Map<String, GraphBaseEntity> getNameEntityMap() {
		if (nameEntityMap != null ) {
			return nameEntityMap;
		} else {
			return new HashMap<>();
		}
	}


	public void add(String name, GraphBaseEntity entity) {
		if (entities == null) {
			entities = new ArrayList<>();
		}
		if(!entities.contains(entity)) {
			//entities.add(entity);
			entityKeyMap.put(name, entity.getEntityUUID());
			nameEntityMap.put(name, entity);
		}
		
	}


	public Map<String, String> getMatchingAttributes() {
		return matchingAttributes;
	}


	public void setMatchingAttributes(Map<String, String> matchingAttributes) {
		this.matchingAttributes = matchingAttributes;
	}
}
