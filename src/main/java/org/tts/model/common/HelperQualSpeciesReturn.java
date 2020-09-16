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

import java.util.HashMap;
import java.util.Map;

public class HelperQualSpeciesReturn {
	Map<String, SBMLQualSpecies> speciesMap;
	
	Map<String, String> sBaseIdMap;

	public Map<String, SBMLQualSpecies> getSpeciesMap() {
		return speciesMap;
	}

	public void setSpeciesMap(Map<String, SBMLQualSpecies> speciesMap) {
		this.speciesMap = speciesMap;
	}
	
	public void addQualSpecies(SBMLQualSpecies qualSpecies) {
		if (this.speciesMap == null) {
			this.speciesMap = new HashMap<>();
		}
		if(!this.speciesMap.containsKey(qualSpecies.getsBaseId())) {
			this.speciesMap.put(qualSpecies.getsBaseId(), qualSpecies);
		}
	}
	

	public Map<String, String> getsBaseIdMap() {
		return sBaseIdMap;
	}

	public void setsBaseIdMap(Map<String, String> sBaseIdMap) {
		this.sBaseIdMap = sBaseIdMap;
	}
	
	public void addsBasePair(String originalSBaseId, String newSBaseId) {
		if (this.sBaseIdMap == null) {
			this.sBaseIdMap = new HashMap<>();
		}
		if (!this.sBaseIdMap.containsKey(originalSBaseId)) {
			this.sBaseIdMap.put(originalSBaseId, newSBaseId);
		}
	}
}
