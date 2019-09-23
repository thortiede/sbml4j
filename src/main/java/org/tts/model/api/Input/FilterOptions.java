package org.tts.model.api.Input;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.ResourceSupport;
import org.tts.model.common.GraphEnum.NetworkMappingType;

public class FilterOptions  extends ResourceSupport {
	
	String mappingUuid;
	
	List<String> relationTypes;
	
	List<String> nodeTypes;
	
	List<String> nodeSymbols;


	int minSize;
	int maxSize;
	
	public int getMinSize() {
		return minSize;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	NetworkMappingType networkType;
	
	
	public String getMappingUuid() {
		return mappingUuid;
	}

	public void setMappingUuid(String mappingUuid) {
		this.mappingUuid = mappingUuid;
	}

	public List<String> getRelationTypes() {
		return relationTypes;
	}

	public void setRelationTypes(List<String> relationTypes) {
		this.relationTypes = relationTypes;
	}

	public List<String> getNodeTypes() {
		return nodeTypes;
	}

	public void setNodeTypes(List<String> nodeTypes) {
		this.nodeTypes = nodeTypes;
	}

	public List<String> getNodeSymbols() {
		return nodeSymbols;
	}

	public void setNodeSymbols(List<String> nodeSymbols) {
		this.nodeSymbols = nodeSymbols;
	}

	public NetworkMappingType getNetworkType() {
		return networkType;
	}

	public void setNetworkType(NetworkMappingType networkType) {
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
		if(this.relationTypes != null) {
			ret += "relationTypes: ";
			for (String type : this.relationTypes) {
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
		if(this.nodeSymbols != null) {
			ret += "(nodeSymbols: ";
			for(String nodeSymbol : this.nodeSymbols) {
				ret += nodeSymbol;
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
		// compare relationTypes
		List<String> thisTransitionTypes = new ArrayList<>(this.getRelationTypes());
		for (String transitionType : other.getRelationTypes()) {
			if (thisTransitionTypes.remove(transitionType) != true) {
				return false;
			}
		}
		
		List<String> thisNodeSymbols = new ArrayList<>(this.getNodeSymbols());
		for (String nodeSymbol : other.getNodeSymbols()) {
			if (thisNodeSymbols.remove(nodeSymbol) != true) {
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
