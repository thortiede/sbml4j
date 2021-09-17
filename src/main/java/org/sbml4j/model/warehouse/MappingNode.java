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
package org.sbml4j.model.warehouse;

import java.util.HashSet;
import java.util.Set;

import org.sbml4j.model.common.GraphEnum.NetworkMappingType;

public class MappingNode extends WarehouseGraphNode {

	private NetworkMappingType mappingType;
	
	private String mappingName;

	private Set<String> mappingNodeTypes;
	
	private Set<String> mappingRelationTypes;
	
	private Set<String> mappingNodeSymbols;
	
	private Set<String> mappingRelationSymbols;
	
	public Set<String> getMappingNodeTypes() {
		return mappingNodeTypes;
	}

	public void setMappingNodeTypes(Set<String> mappingNodeTypes) {
		this.mappingNodeTypes = mappingNodeTypes;
	}

	public void addMappingNodeType(String type) {
		this.mappingNodeTypes.add(type);
	}
	
	public Set<String> getMappingRelationTypes() {
		return mappingRelationTypes;
	}

	public void setMappingRelationTypes(Set<String> mappingRelationTypes) {
		this.mappingRelationTypes = mappingRelationTypes;
	}
	
	public void addMappingRelationType(String type) {
		this.mappingRelationTypes.add(type);
	}

	public NetworkMappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(NetworkMappingType mappingType) {
		this.mappingType = mappingType;
	}

	public String getMappingName() {
		return mappingName;
	}

	public void setMappingName(String mappingName) {
		this.mappingName = mappingName;
	}

	public void setMappingNodeSymbols(Set<String> mappingNodeSymbols) {
		this.mappingNodeSymbols = mappingNodeSymbols;
	}
	
	public void addMappingNodeSymbol(String nodeSymbol) {
		this.mappingNodeSymbols.add(nodeSymbol);
	}
	
	public Set<String> getMappingRelationSymbols() {
		return mappingRelationSymbols;
	}

	public void setMappingRelationSymbols(Set<String> mappingRelationSymbols) {
		this.mappingRelationSymbols = mappingRelationSymbols;
	}

	public void addMappingRelationSymbol(String relationSymbol) {
		if (this.mappingRelationSymbols == null) {
			this.mappingRelationSymbols = new HashSet<>();
		}
		this.mappingRelationSymbols.add(relationSymbol);
	}
	
	public Set<String> getMappingNodeSymbols() {
		return this.mappingNodeSymbols;
	}
	
}
