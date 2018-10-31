package org.tts.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Species;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class GraphSpecies extends GraphSBase{


	
	// a name for the webviewer of neo4j
	private String name;
	
	
	private Map<String, List<String>> cvTermMap;
	
	private String sbmlCompartmentString;
	
	@JsonIgnore
	@Relationship(type = "IN_COMPARTMENT", direction = Relationship.OUTGOING)
	private GraphCompartment compartment;
	
	
	
	
//	@Relationship(type = "isReactant", direction = Relationship.OUTGOING)
//	private GraphReaction graphReactionReactant;
	
	
//	@Relationship(type = "isProduct", direction = Relationship.OUTGOING)
//	private GraphReaction graphReactionProduct;
	
	
	
	//private boolean sbmlBoundaryCondition;
	
	//private boolean sbmlConstant;
	
	//private boolean sbmlHasOnlySubstanceUnits;
	
	private String sbmlMetaIdString;
	
	//private double sbmlInitialAmount;
	
	//private double sbmlInitialConcentration;
	
	private String sbmlSBOTermIDString;
	
	private String sbmlNotesString;
	
	
	public GraphSpecies() {}
	
	public GraphSpecies(Species species, GraphCompartment _compartment, GraphModel model) {
		// <species boundaryCondition="false" compartment="default" constant="false" hasOnlySubstanceUnits="false" id="OGDH" initialAmount="1" metaid="meta_OGDH" name="OGDH" sboTerm="SBO:0000252">
	    setModel(model);
		setCompartment(_compartment);
		setSbmlCompartmentString(species.getCompartment());
		setSbmlIdString(species.getId());
		setSbmlNameString(species.getName());
		setName(species.getName());
		//setSbmlBoundaryCondition(species.getBoundaryCondition());
		//setSbmlConstant(species.getConstant());
		//setSbmlHasOnlySubstanceUnits(species.getHasOnlySubstanceUnits());
		setSbmlMetaIdString(species.getMetaId());
		//setSbmlInitialAmount(species.getInitialAmount());
		//setSbmlInitialConcentration(species.getInitialConcentration());
		setSbmlSBOTermIDString(species.getSBOTermID());
		try {
			setSbmlNotesString(species.getNotesString());
		} catch (XMLStreamException e) {
			System.out.println("Could not generate Notes-String for Species " + species.getName());
			e.printStackTrace();
		}

		// CVTerms
		cvTermMap = new HashMap<String, List<String>>();
		//System.out.println("CVTerms of Species " + species.getName() + " are " + species.getCVTermCount());
		for(CVTerm cvterm : species.getCVTerms()) {
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


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, List<String>> getCvTermMap() {
		return cvTermMap;
	}

	public void setCvTermMap(Map<String, List<String>> cvTermMap) {
		this.cvTermMap = cvTermMap;
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

	
/*
	public GraphReaction getGraphReactionReactant() {
		return graphReactionReactant;
	}

	public void setGraphReactionReactant(GraphReaction graphReactionReactant) {
		this.graphReactionReactant = graphReactionReactant;
	}

	public GraphReaction getGraphReactionProduct() {
		return graphReactionProduct;
	}

	public void setGraphReactionProduct(GraphReaction graphReactionProduct) {
		this.graphReactionProduct = graphReactionProduct;
	}
*/

	/*public boolean isSbmlBoundaryCondition() {
		return sbmlBoundaryCondition;
	}

	public void setSbmlBoundaryCondition(boolean sbmlBoundaryCondition) {
		this.sbmlBoundaryCondition = sbmlBoundaryCondition;
	}

	public boolean isSbmlConstant() {
		return sbmlConstant;
	}

	public void setSbmlConstant(boolean sbmlConstant) {
		this.sbmlConstant = sbmlConstant;
	}

	public boolean isSbmlHasOnlySubstanceUnits() {
		return sbmlHasOnlySubstanceUnits;
	}

	public void setSbmlHasOnlySubstanceUnits(boolean sbmlHasOnlySubstanceUnits) {
		this.sbmlHasOnlySubstanceUnits = sbmlHasOnlySubstanceUnits;
	}*/

	public String getSbmlMetaIdString() {
		return sbmlMetaIdString;
	}

	public void setSbmlMetaIdString(String sbmlMetaIdString) {
		this.sbmlMetaIdString = sbmlMetaIdString;
	}
/*
	public double getSbmlInitialAmount() {
		return sbmlInitialAmount;
	}

	public void setSbmlInitialAmount(double sbmlInitialAmount) {
		this.sbmlInitialAmount = sbmlInitialAmount;
	}

	public double getSbmlInitialConcentration() {
		return sbmlInitialConcentration;
	}

	public void setSbmlInitialConcentration(double sbmlInitialConcentration) {
		this.sbmlInitialConcentration = sbmlInitialConcentration;
	}
*/
	public String getSbmlSBOTermIDString() {
		return sbmlSBOTermIDString;
	}

	public void setSbmlSBOTermIDString(String sbmlSBOTermIDString) {
		this.sbmlSBOTermIDString = sbmlSBOTermIDString;
	}

	public String getSbmlNotesString() {
		return sbmlNotesString;
	}

	public void setSbmlNotesString(String sbmlNotesString) {
		this.sbmlNotesString = sbmlNotesString;
	}
	
}
