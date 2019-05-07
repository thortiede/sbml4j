package org.tts.model;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.sbml.jsbml.Compartment;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class GraphCompartment extends GraphSBase {

	
	private boolean sbmlConstant;
	
	private String sbmlName;
	
	private String sbmlSboTerm;
	
	private double sbmlSize;
	
	private double sbmlSpatialDimensions;
	
	//private boolean sbmlIsSetSpacialDimensions;
	
	//private Double sbmlSpatialDimensions;
	
	@JsonIgnore
	@Relationship(type = "IN_COMPARTMENT", direction = Relationship.INCOMING)
	private List<GraphSpecies> speciesInThisCompartment;
	
	@JsonIgnore
	@Relationship(type = "IN_COMPARTMENT", direction = Relationship.INCOMING)
	private List<GraphQualitativeSpecies> qualSpeciesInThisCompartment;
	
	@JsonIgnore
	@Relationship(type = "IN_COMPARTMENT", direction = Relationship.INCOMING)
	private List<GraphReaction> graphReactionsInThisCompartment;
	
	
	
	
	public GraphCompartment() {
		
	}

	// Constructor to create a GraphCompartment Element from a jsbml Compartment instance
	public GraphCompartment(Compartment compartment, GraphModel model) {
		setModel(model);
		setOrganism(model.getOrganism());
		setOrganismTaxonomyId(model.getOrganismTaxonomyId());
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