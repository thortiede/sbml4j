package org.tts.service;

import java.util.HashSet;
import java.util.Set;

import org.sbml.jsbml.ontology.Triple;
import org.springframework.stereotype.Service;

@Service
public class UtilityService {

	
	public String translateSBOString(String sboString) {
		return (sboString.equals("") || sboString.equals("unknown") || sboString.equals("undefined in source") || sboString.equals("unknownFromSource")) ? "unknownFromSource" : org.sbml.jsbml.SBO.getTerm(sboString).getName();
	}
	
	public String translateToSBOString(String alias) {
		return org.sbml.jsbml.SBO.intToString(org.sbml.jsbml.SBO.convertAlias2SBO(alias)) != "" ?
				org.sbml.jsbml.SBO.intToString(org.sbml.jsbml.SBO.convertAlias2SBO(alias)) :
					"undefined in source"; // error prone TODO not all not known aliases should lead to "undefined in source"
	}
	
	public Set<String> getSBOChildren(String sboRootTerm) {
		
		Set<String> childrenSet = new HashSet<>();
		Set<Triple> children = org.sbml.jsbml.SBO.getTriples(null, org.sbml.jsbml.SBO.getTerm("is_a"), org.sbml.jsbml.SBO.getTerm(sboRootTerm));
		for (Triple triple : children) {
			childrenSet.add(triple.getSubject().getId());
		}
		return childrenSet;
	}
	
	public Set<String> getAllSBOChildren(String sboRootTerm) {
		Set<String> directChildren = getSBOChildren(sboRootTerm);
		Set<String> allChildren = new HashSet<>();
		for (String sboChild : directChildren) {
			allChildren.add(sboChild);
			Set<String> childChildren = getSBOChildren(sboChild);
			for(String childChild : childChildren) {
				allChildren.add(childChild);
			}
		}
		return allChildren;
	}
	
}
