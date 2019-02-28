package org.tts.model.api.Input;

import java.util.List;

public class FilterOptions {
	List<String> transitionTypes;
	
	
	public List<String> getTransitionTypes() {
		return transitionTypes;
	}


	public void setTransitionTypes(List<String> transitionTypes) {
		this.transitionTypes = transitionTypes;
	}


	public String toString() {
		String ret = "Filter options are: transitionTypes: ";
		for (String type : this.transitionTypes) {
			ret += type;
			ret += ", ";
		}
		return ret.substring(0, ret.length() -2);
	}
	
	
}
