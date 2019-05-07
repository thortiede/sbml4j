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
