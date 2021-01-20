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
	// sBaseName To QualSpecies Map
	Map<String, SBMLQualSpecies> sBaseNameToQualSpeciesMap;
	
	// sBaseId To QualSpecies Map
	Map<String, SBMLQualSpecies> sBaseIdToQualSpeciesMap;

	public Map<String, SBMLQualSpecies> getSBaseNameToQualSpeciesMap() {
		return this.sBaseNameToQualSpeciesMap;
	}

	public void setSBaseNameToQualSpeciesMap(Map<String, SBMLQualSpecies> sBaseNameToQualSpeciesMap) {
		this.sBaseNameToQualSpeciesMap = sBaseNameToQualSpeciesMap;
	}
	
	

	public Map<String, SBMLQualSpecies> getSBaseIdToQualSpeciesMap() {
		return sBaseIdToQualSpeciesMap;
	}

	public void setSBaseIdToQualSpeciesMap(Map<String, SBMLQualSpecies> sBaseIdToQualSpeciesMap) {
		this.sBaseIdToQualSpeciesMap = sBaseIdToQualSpeciesMap;
	}
	
	public void addsBasePair(String originalSBaseId, SBMLQualSpecies qualSpecies) {
		if (this.sBaseIdToQualSpeciesMap == null) {
			this.sBaseIdToQualSpeciesMap = new HashMap<>();
		}
		if (!this.sBaseIdToQualSpeciesMap.containsKey(originalSBaseId)) {
			this.sBaseIdToQualSpeciesMap.put(originalSBaseId, qualSpecies);
		}
	}
	
	
	public void addQualSpecies(SBMLQualSpecies qualSpecies) {
		if (this.sBaseNameToQualSpeciesMap == null) {
			this.sBaseNameToQualSpeciesMap = new HashMap<>();
		}
		if(!this.sBaseNameToQualSpeciesMap.containsKey(qualSpecies.getsBaseName())) {
			this.sBaseNameToQualSpeciesMap.put(qualSpecies.getsBaseName(), qualSpecies);
		}
		if (this.sBaseIdToQualSpeciesMap == null) {
			this.sBaseIdToQualSpeciesMap = new HashMap<>();
		}
		if (!this.sBaseIdToQualSpeciesMap.containsKey(qualSpecies.getsBaseId())) {
			this.sBaseIdToQualSpeciesMap.put(qualSpecies.getsBaseId(), qualSpecies);
		}
	}
	
}
