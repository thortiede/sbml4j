/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2022.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.model.full;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.sbml.SBMLCompartment;
import org.sbml4j.model.sbml.SBMLSBaseEntity;
import org.sbml4j.model.sbml.SBMLSpecies;

public class SBMLModelEntity extends SBMLSBaseEntity {

	/**
	 * The following String fields are actually links
	 * to SBMLUnitDefintion Entities
	 * Those would need to be created beforehand
	 * Be sure to then also link them on creation of the model.
	 */
	private String substanceUnits;

	private String timeUnits;
	
	private String volumeUnits;
	
	private String areaUnits;
	
	private String lengthUnits;
	
	private String extentUnits;
	
	/**
	 * conversionFactor references a Paramter object
	 * It is a globally set conversion factor for all
	 * species that do not define their own conversion factor
	 */
	private String conversionFactor;
	
	@Relationship(type = "CONTAINS", direction = Relationship.INCOMING)
	private SBMLDocumentEntity enclosingSBMLEntity;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLFunctionDefinition> sbmlFunctionDefinitions;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLUnitDefinition> sbmlUnitDefinitions;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLCompartment> sbmlCompartments;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLSpecies> sbmlSpecies;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLParameter> sbmlParameters;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLInitialAssignment> sbmlInitialAssigments;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLRule> sbmlRules;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLConstraint> sbmlConstraints;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLReaction> sbmlReactions;
	
	@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private List<SBMLEvent> sbmlEvents;

	public String getSubstanceUnits() {
		return substanceUnits;
	}

	public void setSubstanceUnits(String substanceUnits) {
		this.substanceUnits = substanceUnits;
	}

	public String getTimeUnits() {
		return timeUnits;
	}

	public void setTimeUnits(String timeUnits) {
		this.timeUnits = timeUnits;
	}

	public String getVolumeUnits() {
		return volumeUnits;
	}

	public void setVolumeUnits(String volumeUnits) {
		this.volumeUnits = volumeUnits;
	}

	public String getAreaUnits() {
		return areaUnits;
	}

	public void setAreaUnits(String areaUnits) {
		this.areaUnits = areaUnits;
	}

	public String getLengthUnits() {
		return lengthUnits;
	}

	public void setLengthUnits(String lengthUnits) {
		this.lengthUnits = lengthUnits;
	}

	public String getExtentUnits() {
		return extentUnits;
	}

	public void setExtentUnits(String extentUnits) {
		this.extentUnits = extentUnits;
	}

	public String getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(String conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public SBMLDocumentEntity getEnclosingSBMLEntity() {
		return enclosingSBMLEntity;
	}

	public void setEnclosingSBMLEntity(SBMLDocumentEntity enclosingSBMLEntity) {
		this.enclosingSBMLEntity = enclosingSBMLEntity;
	}

	public List<SBMLFunctionDefinition> getSbmlFunctionDefinitions() {
		return sbmlFunctionDefinitions;
	}

	public void setSbmlFunctionDefinitions(List<SBMLFunctionDefinition> sbmlFunctionDefinitions) {
		this.sbmlFunctionDefinitions = sbmlFunctionDefinitions;
	}

	public List<SBMLUnitDefinition> getSbmlUnitDefinitions() {
		return sbmlUnitDefinitions;
	}

	public void setSbmlUnitDefinitions(List<SBMLUnitDefinition> sbmlUnitDefinitions) {
		this.sbmlUnitDefinitions = sbmlUnitDefinitions;
	}

	public List<SBMLCompartment> getSbmlCompartments() {
		return sbmlCompartments;
	}

	public void setSbmlCompartments(List<SBMLCompartment> sbmlCompartments) {
		this.sbmlCompartments = sbmlCompartments;
	}

	public List<SBMLSpecies> getSbmlSpecies() {
		return sbmlSpecies;
	}

	public void setSbmlSpecies(List<SBMLSpecies> sbmlSpecies) {
		this.sbmlSpecies = sbmlSpecies;
	}

	public List<SBMLParameter> getSbmlParameters() {
		return sbmlParameters;
	}

	public void setSbmlParameters(List<SBMLParameter> sbmlParameters) {
		this.sbmlParameters = sbmlParameters;
	}

	public List<SBMLInitialAssignment> getSbmlInitialAssigments() {
		return sbmlInitialAssigments;
	}

	public void setSbmlInitialAssigments(List<SBMLInitialAssignment> sbmlInitialAssigments) {
		this.sbmlInitialAssigments = sbmlInitialAssigments;
	}

	public List<SBMLRule> getSbmlRules() {
		return sbmlRules;
	}

	public void setSbmlRules(List<SBMLRule> sbmlRules) {
		this.sbmlRules = sbmlRules;
	}

	public List<SBMLConstraint> getSbmlConstraints() {
		return sbmlConstraints;
	}

	public void setSbmlConstraints(List<SBMLConstraint> sbmlConstraints) {
		this.sbmlConstraints = sbmlConstraints;
	}

	public List<SBMLReaction> getSbmlReactions() {
		return sbmlReactions;
	}

	public void setSbmlReactions(List<SBMLReaction> sbmlReactions) {
		this.sbmlReactions = sbmlReactions;
	}

	public List<SBMLEvent> getSbmlEvents() {
		return sbmlEvents;
	}

	public void setSbmlEvents(List<SBMLEvent> sbmlEvents) {
		this.sbmlEvents = sbmlEvents;
	}

}
