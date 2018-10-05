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
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
public class GraphReaction extends GraphSBase {


	private Map<String, List<String>> cvTermMap;
	
	private String sbmlCompartmentString;
	
	@JsonIgnore
	@Relationship(type = "inCompartment", direction = Relationship.INCOMING)
	private GraphCompartment compartment;

	@JsonIgnore
	@Relationship(type = "hasReaction", direction = Relationship.INCOMING)
	private GraphModel model;
	
	private String sbmlMetaIdString;
	
	boolean sbmlReversible;
	
	private String sbmlSBOTermIDString;

	private String sbmlNotesString;
	
	@JsonIgnore
	@Relationship(type = "isReactant", direction = Relationship.INCOMING)
	private List<GraphSpecies> reactants;
	
	@JsonIgnore
	@Relationship(type = "isProduct", direction = Relationship.INCOMING)
	private List<GraphSpecies> products;
	
	
	public GraphReaction() {}
	
	public GraphReaction(Reaction reaction, GraphCompartment _compartment, List<GraphSpecies> listGraphSpecies) {
		// <reaction compartment="default" fast="false" id="rnR00678" metaid="meta_rnR00678" name="rn:R00678" reversible="false" sboTerm="SBO:0000176">
	   setSbmlCompartmentString(reaction.getCompartment());
	   setCompartment(_compartment);
	   setSbmlIdString(reaction.getId());
	   setSbmlMetaIdString(reaction.getMetaId());
	   setSbmlNameString(reaction.getName());
	   setSbmlReversible(reaction.getReversible());
	   setSbmlSBOTermIDString(reaction.getSBOTermID());
	   //   <notes>
		try {
			setSbmlNotesString(reaction.getNotesString());
		} catch (XMLStreamException e) {
			System.out.println("Could not generate Notes-String for Reaction " + reaction.getName());
			e.printStackTrace();
		}
		// cvterms
		cvTermMap = new HashMap<String, List<String>>();
		for(CVTerm cvterm : reaction.getCVTerms()) {
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
		
		/**
		 * listofReactants
		 *   <listOfReactants>
         * 		<speciesReference constant="false" id="cpdC00078" metaid="meta_cpdC00078" name="cpd:C00078" sboTerm="SBO:0000015" species="C11H12N2O2" stoichiometry="1" />
         *	 </listOfReactants>
         *	the attribute species seems to come from the "SimpleSpeciesReference" and is the Id of the Species that is being referenced.
		 */
		reactants = new ArrayList<GraphSpecies>();
		for (SpeciesReference speciesReference : reaction.getListOfReactants()) {
			for (GraphSpecies species : listGraphSpecies) {
				if (species.getSbmlIdString().equals(speciesReference.getSpecies())) {
					// we found a species that is being referenced as reactant of this reaction
					reactants.add(species);
				}
			}
			// TODO: Additional Properties of this Species Reference also need to be stored; 
		}
		
		/**
		 * listofProducts
         *	 <listOfProducts>
         *   	<speciesReference constant="false" id="cpdC02700" metaid="meta_cpdC02700" name="cpd:C02700" sboTerm="SBO:0000011" species="C11H12N2O4" stoichiometry="1" />
         *   </listOfProducts>
         *	the attribute species seems to come from the "SimpleSpeciesReference" and is the Id of the Species that is being referenced.
		 */
		products = new ArrayList<GraphSpecies>();
		for (SpeciesReference speciesReference : reaction.getListOfProducts()) {
			for (GraphSpecies species : listGraphSpecies) {
				if (species.getSbmlIdString().equals(speciesReference.getSpecies())) {
					// we found a species that is being referenced as reactant of this reaction
					products.add(species);
				}
			}
			// TODO: Additional Properties of this Species Reference also need to be stored; 
		}
		
		//TODO: listOfModifiers
		
		//TODO: kineticLaw
		
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

	public String getSbmlMetaIdString() {
		return sbmlMetaIdString;
	}

	public void setSbmlMetaIdString(String sbmlMetaIdString) {
		this.sbmlMetaIdString = sbmlMetaIdString;
	}

	public boolean isSbmlReversible() {
		return sbmlReversible;
	}

	public void setSbmlReversible(boolean sbmlReversible) {
		this.sbmlReversible = sbmlReversible;
	}

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

	public List<GraphSpecies> getReactants() {
		return reactants;
	}

	public void setReactants(List<GraphSpecies> reactants) {
		this.reactants = reactants;
	}

	public List<GraphSpecies> getProducts() {
		return products;
	}

	public void setProducts(List<GraphSpecies> products) {
		this.products = products;
	}
	
}
 