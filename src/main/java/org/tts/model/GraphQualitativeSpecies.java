package org.tts.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.ext.qual.QualitativeSpecies;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class GraphQualitativeSpecies {

	@Id @GeneratedValue
	private Long id = null;

	@Version
	private Long version;
	
	// a name for the webviewer of neo4j
	private String name;
	
	
	private String sbmlIdString;
	
	private String sbmlNameString;
	
	private String sbmlCompartmentString;
	
	@JsonIgnore
	@Relationship(type = "inCompartment", direction = Relationship.INCOMING)
	private GraphCompartment compartment;
	
	@JsonIgnore
	@Relationship(type = "hasQualSpecies", direction = Relationship.INCOMING)
	private GraphModel model;
	
	@Relationship(type = "IS_SPECIES", direction = Relationship.OUTGOING)
	private GraphSpecies species;
	
	private int sbmlMaxLevel;
	
	private int sbmlInitialLevel;
		
	private Map<String, List<String>> cvTermMap;
		
	// Relation
	// TODO: Check whether we actually do need it?
	// Or do we actually wanted to link to the regular Species element?
	//@Relationship(type = "RELATION")
	//private GraphQualitativeSpecies relatedQualSpecies;
	
	
	public GraphQualitativeSpecies() {
		cvTermMap = new HashMap<String, List<String>>();
		
	}
	
	public GraphQualitativeSpecies(QualitativeSpecies qualitativeSpecies, GraphCompartment _compartment) {
		
		// Set Attributes
		// TODO: Use setters
		sbmlIdString = qualitativeSpecies.getId();
		sbmlNameString = qualitativeSpecies.getName();
		name = sbmlNameString;
		sbmlCompartmentString = qualitativeSpecies.getCompartment();
		compartment = _compartment;
		if(qualitativeSpecies.isSetMaxLevel()) {
			setSbmlMaxLevel(qualitativeSpecies.getMaxLevel());
		} 
		if(qualitativeSpecies.isSetInitialLevel()) {
			setSbmlInitialLevel(qualitativeSpecies.getInitialLevel());
		}
		// CVTerms
		cvTermMap = new HashMap<String, List<String>>();
		for(CVTerm cvterm : qualitativeSpecies.getCVTerms()) {
			// check if key already present
			if(cvTermMap.containsKey(cvterm.toString())){
				// already present
				List<String> valueList = cvTermMap.get(cvterm.toString());
				for(int i = 0; i!= cvterm.getResourceCount(); i++) {
					valueList.add(cvterm.getResource(i));
				}
				cvTermMap.replace(cvterm.toString(), valueList);
			} else {
				// not present
				List<String> valueList = new ArrayList<String>();
				for(int i = 0; i!= cvterm.getResourceCount(); i++) {
					valueList.add(cvterm.getResource(i));
				}
				cvTermMap.put(cvterm.toString(), valueList);
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GraphModel getModel() {
		return model;
	}

	public void setModel(GraphModel model) {
		this.model = model;
	}

	public GraphSpecies getSpecies() {
		return species;
	}

	public void setSpecies(GraphSpecies species) {
		this.species = species;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getSbmlIdString() {
		return sbmlIdString;
	}

	public void setSbmlIdString(String sbmlIdString) {
		this.sbmlIdString = sbmlIdString;
	}

	public String getSbmlNameString() {
		return sbmlNameString;
	}

	public void setSbmlNameString(String sbmlNameString) {
		this.sbmlNameString = sbmlNameString;
	}

	public String getSbmlCompartmentString() {
		return sbmlCompartmentString;
	}

	public void setSbmlCompartmentString(String sbmlCompartmentString) {
		this.sbmlCompartmentString = sbmlCompartmentString;
	}

	public GraphCompartment getCompartment() {
		return compartment;
	}

	public void setCompartment(GraphCompartment compartment) {
		this.compartment = compartment;
	}

	public int getSbmlMaxLevel() {
		return sbmlMaxLevel;
	}

	public void setSbmlMaxLevel(int sbmlMaxLevel) {
		this.sbmlMaxLevel = sbmlMaxLevel;
	}

	public int getSbmlInitialLevel() {
		return sbmlInitialLevel;
	}

	public void setSbmlInitialLevel(int sbmlInitialLevel) {
		this.sbmlInitialLevel = sbmlInitialLevel;
	}

	public Map<String, List<String>> getCvTermMap() {
		return cvTermMap;
	}

	public void setCvTermMap(Map<String, List<String>> cvTermMap) {
		this.cvTermMap = cvTermMap;
	}

	/*public GraphQualitativeSpecies getRelatedQualSpecies() {
		return relatedQualSpecies;
	}

	public void setRelatedQualSpecies(GraphQualitativeSpecies relatedQualSpecies) {
		this.relatedQualSpecies = relatedQualSpecies;
	}*/
}
