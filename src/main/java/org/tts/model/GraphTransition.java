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
import org.sbml.jsbml.ext.qual.FunctionTerm;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.Output;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.jsbml.ext.qual.Transition;

import com.fasterxml.jackson.annotation.JsonIgnore;


@NodeEntity
public class GraphTransition extends GraphSBase {

	private String name;
	
	@Relationship(type="SPECIES_ONE_OF_RELATION", direction = Relationship.OUTGOING)
	GraphQualitativeSpecies qualSpeciesOne;
	
	
	@Relationship(type="SPECIES_TWO_OF_RELATION", direction = Relationship.OUTGOING)
	GraphQualitativeSpecies qualSpeciesTwo;
	
	private String qualSpeciesOneSbmlNameString;
	
	private String inputTransitionEffect;
	private String qualSpeciesTwoSbmlNameString;
	private String outputTransitionEffect;
	
	private String sbmlSignString;
	
	private String metaid;
	
	private String sbmlSBOTerm;
	
	//List<GraphTransitionInput> listGraphTransitionInput;
	//List<GraphTransitionOutput> listGraphTransitionOutput;
	//List<GraphTransitonFunctionTerm> listGraphTransitionFunctionTerm;

	private Map<String, List<String>> cvTermMap;
	
	
	public GraphTransition() {}
	
	/***
	 * Constructor to build one (possibly more) Graph Transitions from one SBML Transition
	 * This function creates one Transition Object from the @param transition 
	 * @param transition The original Transition object
	 * @param inputIndex The index of the Input in listOfInputs that should be the StartNode of this Transition
	 * @param outputIndex The index of the Output in listOfOutputs that should be the EndNode of this Transition
	 * @param listGraphQualitiativeSpecies The List of qualitative Species that the models knows
	 */
	public GraphTransition(Transition transition, int inputIndex, int outputIndex, int functionTermIndex, List<GraphQualitativeSpecies> listGraphQualitiativeSpecies, GraphModel model) {
		setModel(model);
		setOrganism(model.getOrganism());
		setOrganismTaxonomyId(model.getOrganismTaxonomyId());
		setMetaid(transition.getMetaId());
		Input input = transition.getListOfInputs().get(inputIndex);
		Output output = transition.getListOfOutputs().get(outputIndex);
		/** 
		 * Function Term is not set in KGML Models
		 *
		FunctionTerm functionTerm = null; // Does this work?
		if(transition.getFunctionTermCount() >= functionTermIndex) {
			functionTerm = transition.getListOfFunctionTerms().get(functionTermIndex);
		}*/
		// set start and end node
		String inputQualSpeciesIdString = input.getQualitativeSpeciesInstance().getId();
		String outputQualSpeciesIdString = output.getQualitativeSpeciesInstance().getId();
		boolean isInputSet = false;
		boolean isOutputSet = false;
		for (GraphQualitativeSpecies species : listGraphQualitiativeSpecies) {
			//System.out.println("Input: Comparing " + species.getSbmlIdString() + " with " + inputQualSpeciesIdString);
			if (species.getSbmlIdString().equals(inputQualSpeciesIdString) && !isInputSet) {
				setQualSpeciesOne(species);
				setQualSpeciesOneSbmlNameString(species.getSbmlNameString());
				if(input.isSetTransitionEffect()) {
					setInputTransitionEffect(input.getTransitionEffect().toString());
				} else {
					setInputTransitionEffect("Not Set"); // TODO: Do not hard code this
				}
				//setSbmlSignString(input.getSign().toString());
				isInputSet = true;
			}
			// don't do an else if here, as we could have the same species as start and end node ? (Do we?)
			//System.out.println("Output: Comparing " + species.getSbmlIdString() + " with " + outputQualSpeciesIdString);
			if(species.getSbmlIdString().equals(outputQualSpeciesIdString) && !isOutputSet) {
				setQualSpeciesTwo(species);
				setQualSpeciesTwoSbmlNameString(species.getSbmlNameString());
				if(output.isSetTransitionEffect()) {
					setOutputTransitionEffect(output.getTransitionEffect().toString());
				} else {
					setOutputTransitionEffect("Not Set"); // TODO: Do not hard code this
				}
				isOutputSet = true;
			}
		}
		// Both qualSpeciesOne and qualSpeciesTwo should be set now
		if(!(isInputSet && isOutputSet)) {
			System.out.println("WARNING, input or output not set in transition " + transition.getName());
			return;
		}
		// set additional Properties of this relation
		setSbmlIdString(transition.getId());
		setSbmlNameString(transition.getName());
		setSbmlSBOTerm(transition.getSBOTermID());
		// CVTerms
		cvTermMap = new HashMap<String, List<String>>();
		
		for(CVTerm cvterm : transition.getCVTerms()) { //getAnnotation().getListOfCVTerms()) { // we might need to use transition.getAnnotation().getListOfCVTerms() here, as the cvterms are 
														// defined in the annotation of a transition element. . It appears that getting the CV Terms directly dies work. No need to go through the annotations
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

	
	public GraphQualitativeSpecies getQualSpeciesOne() {
		return qualSpeciesOne;
	}

	public void setQualSpeciesOne(GraphQualitativeSpecies qualSpeciesOne) {
		this.qualSpeciesOne = qualSpeciesOne;
	}

	public GraphQualitativeSpecies getQualSpeciesTwo() {
		return qualSpeciesTwo;
	}

	public void setQualSpeciesTwo(GraphQualitativeSpecies qualSpeciesTwo) {
		this.qualSpeciesTwo = qualSpeciesTwo;
	}

	public String getInputTransitionEffect() {
		return inputTransitionEffect;
	}

	public void setInputTransitionEffect(String inputTransitionEffect) {
		this.inputTransitionEffect = inputTransitionEffect;
	}

	public String getOutputTransitionEffect() {
		return outputTransitionEffect;
	}

	public void setOutputTransitionEffect(String outputTransitionEffect) {
		this.outputTransitionEffect = outputTransitionEffect;
	}

	public String getSbmlSignString() {
		return sbmlSignString;
	}

	public void setSbmlSignString(String sbmlSignString) {
		this.sbmlSignString = sbmlSignString;
	}
/*
	public List<GraphTransitionInput> getListGraphTransitionInput() {
		return listGraphTransitionInput;
	}

	public void setListGraphTransitionInput(List<GraphTransitionInput> listGraphTransitionInput) {
		this.listGraphTransitionInput = listGraphTransitionInput;
	}

	public List<GraphTransitionOutput> getListGraphTransitionOutput() {
		return listGraphTransitionOutput;
	}

	public void setListGraphTransitionOutput(List<GraphTransitionOutput> listGraphTransitionOutput) {
		this.listGraphTransitionOutput = listGraphTransitionOutput;
	}

	public List<GraphTransitonFunctionTerm> getListGraphTransitionFunctionTerm() {
		return listGraphTransitionFunctionTerm;
	}

	public void setListGraphTransitionFunctionTerm(List<GraphTransitonFunctionTerm> listGraphTransitionFunctionTerm) {
		this.listGraphTransitionFunctionTerm = listGraphTransitionFunctionTerm;
	}
*/
	public Map<String, List<String>> getCvTermMap() {
		return cvTermMap;
	}

	public void setCvTermMap(Map<String, List<String>> cvTermMap) {
		this.cvTermMap = cvTermMap;
	}

	public String getSbmlSBOTerm() {
		return sbmlSBOTerm;
	}

	public void setSbmlSBOTerm(String sbmlSBOTerm) {
		this.sbmlSBOTerm = sbmlSBOTerm;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQualSpeciesOneSbmlNameString() {
		return qualSpeciesOneSbmlNameString;
	}

	public void setQualSpeciesOneSbmlNameString(String qualSpeciesOneSbmlNameString) {
		this.qualSpeciesOneSbmlNameString = qualSpeciesOneSbmlNameString;
	}

	public String getQualSpeciesTwoSbmlNameString() {
		return qualSpeciesTwoSbmlNameString;
	}

	public void setQualSpeciesTwoSbmlNameString(String qualSpeciesTwoSbmlNameString) {
		this.qualSpeciesTwoSbmlNameString = qualSpeciesTwoSbmlNameString;
	}

	public String getMetaid() {
		return metaid;
	}

	public void setMetaid(String metaid) {
		this.metaid = metaid;
	}

	
	
	
}
