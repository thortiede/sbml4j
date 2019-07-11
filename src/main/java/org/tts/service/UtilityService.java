package org.tts.service;

import org.springframework.stereotype.Service;

@Service
public class UtilityService {

	
	public String translateSBOString(String sboString) {
		return (sboString.equals("") || sboString.equals("unknown") || sboString.equals("undefined in source")) ? "undefined in source" : org.sbml.jsbml.SBO.getTerm(sboString).getName();
	}
	
	public String translateToSBOString(String alias) {
		return org.sbml.jsbml.SBO.intToString(org.sbml.jsbml.SBO.convertAlias2SBO(alias)) != "" ?
				org.sbml.jsbml.SBO.intToString(org.sbml.jsbml.SBO.convertAlias2SBO(alias)) :
					"undefined in source"; // error prone TODO not all not known aliases should lead to "undefined in source"
	}
	
}
