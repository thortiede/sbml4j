package org.tts.model;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class SBMLReaction extends SBMLCompartmentalizedSBaseEntity {

	private boolean reversible;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLSpeciesReference> reactants;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLSpeciesReference> products;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private List<SBMLModifierSpeciesReference> modifiers;
	
	@Relationship(type = "HAS", direction = Relationship.OUTGOING)
	private SBMLKineticLaw kineticLaw;

	public boolean isReversible() {
		return reversible;
	}

	public void setReversible(boolean reversible) {
		this.reversible = reversible;
	}

	public List<SBMLSpeciesReference> getReactants() {
		return reactants;
	}

	public void setReactants(List<SBMLSpeciesReference> reactants) {
		this.reactants = reactants;
	}

	public List<SBMLSpeciesReference> getProducts() {
		return products;
	}

	public void setProducts(List<SBMLSpeciesReference> products) {
		this.products = products;
	}

	public List<SBMLModifierSpeciesReference> getModifiers() {
		return modifiers;
	}

	public void setModifiers(List<SBMLModifierSpeciesReference> modifiers) {
		this.modifiers = modifiers;
	}

	public SBMLKineticLaw getKineticLaw() {
		return kineticLaw;
	}

	public void setKineticLaw(SBMLKineticLaw kineticLaw) {
		this.kineticLaw = kineticLaw;
	}
}
