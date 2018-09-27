package org.tts.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;
import org.sbml.jsbml.Compartment;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class GraphCompartment {

	@Id @GeneratedValue
	private Long id = null;

	@Version
	private Long version;
	
	private String sbmlIdString;
	
	private boolean sbmlConstant;
	
	private String sbmlName;
	
	private String sbmlSboTerm;
	
	private double sbmlSize;
	
	private double sbmlSpatialDimensions;
	
	//private boolean sbmlIsSetSpacialDimensions;
	
	//private Double sbmlSpatialDimensions;
	
	
	@Relationship(type = "inCompartment", direction = Relationship.OUTGOING)
	private List<GraphSpecies> speciesInThisCompartment;
	
	
	@Relationship(type = "inCompartment", direction = Relationship.OUTGOING)
	private List<GraphQualitativeSpecies> qualSpeciesInThisCompartment;
	
	@Relationship(type = "inCompartment", direction = Relationship.OUTGOING)
	private List<GraphReaction> graphReactionsInThisCompartment;
	
	
	@JsonIgnore
	@Relationship(type = "hasCompartment", direction = Relationship.INCOMING)
	private GraphModel model;
	
	public GraphCompartment() {
		
	}

	// Constructor to create a GraphCompartment Element from a jsbml Compartment instance
	public GraphCompartment(Compartment compartment) {
		speciesInThisCompartment = new ArrayList<GraphSpecies>();
		// fill the Compartment here with the fields from the jsbml compartment
		//   <compartment constant="true" id="default" name="default" sboTerm="SBO:0000410" size="1" spatialDimensions="3" />
		setSbmlIdString(compartment.getId());
		setSbmlConstant(compartment.getConstant());
		setSbmlName(compartment.getName());
		setSbmlSboTerm(compartment.getSBOTermID());
		setSbmlSize(compartment.getSize());
		setSbmlSpatialDimensions(compartment.getSpatialDimensions());
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

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getSbmlIdString() {
		return sbmlIdString;
	}

	public void setSbmlIdString(String sbmlIdString) {
		this.sbmlIdString = sbmlIdString;
	}

	public boolean isSbmlConstant() {
		return sbmlConstant;
	}

	public void setSbmlConstant(boolean sbmlConstant) {
		this.sbmlConstant = sbmlConstant;
	}

	public String getSbmlName() {
		return sbmlName;
	}

	public void setSbmlName(String sbmlName) {
		this.sbmlName = sbmlName;
	}

	public String getSbmlSboTerm() {
		return sbmlSboTerm;
	}

	public void setSbmlSboTerm(String sbmlSboTerm) {
		this.sbmlSboTerm = sbmlSboTerm;
	}

	public double getSbmlSize() {
		return sbmlSize;
	}

	public void setSbmlSize(double sbmlSize) {
		this.sbmlSize = sbmlSize;
	}

	public double getSbmlSpatialDimensions() {
		return sbmlSpatialDimensions;
	}

	public void setSbmlSpatialDimensions(double sbmlSpatialDimensions) {
		this.sbmlSpatialDimensions = sbmlSpatialDimensions;
	}

	public void addToContainedSpecies(GraphSpecies species) {
		speciesInThisCompartment.add(species);
		
	}

	public List<GraphSpecies> getSpeciesInThisCompartment() {
		return speciesInThisCompartment;
	}

	public void setSpeciesInThisCompartment(List<GraphSpecies> speciesInThisCompartment) {
		this.speciesInThisCompartment = speciesInThisCompartment;
	}
	
}
