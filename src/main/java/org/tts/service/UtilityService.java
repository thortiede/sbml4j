package org.tts.service;

import org.springframework.stereotype.Service;

@Service
public class UtilityService {

	
	public String translateSBOString(String sboString) {
		return sboString == "" ? "undefined in source" : org.sbml.jsbml.SBO.getTerm(sboString).getName();
	}
	
}
