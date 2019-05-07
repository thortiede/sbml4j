package org.tts.model.api.Input;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FilterOptions  extends ResourceSupport {
	
	String mappingUuid;
	
	List<String> transitionTypes;
	
	List<String> nodeTypes;

	String networkType;
	
	@JsonIgnore
	public String getMappingUuid() {
		return mappingUuid;
	}

	public void setMappingUuid(String mappingUuid) {
		this.mappingUuid = mappingUuid;
	}

	public List<String> getTransitionTypes() {
		return transitionTypes;
	}

	public void setTransitionTypes(List<String> transitionTypes) {
		this.transitionTypes = transitionTypes;
	}

	public List<String> getNodeTypes() {
		return nodeTypes;
	}

	public void setNodeTypes(List<String> nodeTypes) {
		this.nodeTypes = nodeTypes;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String toString() {
		String ret = "Filter options ";
		if (this.mappingUuid != null) {
			ret += "(uuid: ";
			ret += this.mappingUuid;
			ret += ") ";
		}		
		ret += "are: (";
		if(this.transitionTypes != null) {
			ret += "transitionTypes: ";
			for (String type : this.transitionTypes) {
				ret += type;
				ret += ", ";
			}
		}
		ret = ret.substring(0, ret.length() - 2);
		ret += "); ";
		
		if(this.nodeTypes != null) {
			ret += "(nodeTypes: ";
			for (String nodeType : this.nodeTypes) {
				ret += nodeType;
				ret += ", ";
			}
		}
		ret = ret.substring(0, ret.length() - 2);
		ret += "); ";
		
		if (this.networkType != null) {
			ret += "(networkType: ";
			ret += this.networkType;
			ret += ")";
		}
		
		return ret;
	}
	
	public boolean equals(FilterOptions other) {
		
		if(this.mappingUuid != null && 
				other.getMappingUuid() != null && 
				!this.mappingUuid.equals(other.getMappingUuid())) {
			return false;
		}
		
		// compare NetworkType
		if(!this.networkType.equals(other.networkType)) {
			return false;
		}
		// compare NodeTypes
		List<String> thisNodeTypes = new ArrayList<>(this.getNodeTypes());
		for (String nodeType : other.getNodeTypes()) {
			if(thisNodeTypes.remove(nodeType) != true) {
				return false;
			}
		}
		if (thisNodeTypes.size() != 0) {
			return false;
		}
		// compare transitionTypes
		List<String> thisTransitionTypes = new ArrayList<>(this.getTransitionTypes());
		for (String transitionType : other.getTransitionTypes()) {
			if (thisTransitionTypes.remove(transitionType) != true) {
				return false;
			}
		}
		if (thisTransitionTypes.size() != 0) {
			return false;
		}
		// all comparisions valid, filterOptions are equal		
		return true;
	}
	
}
