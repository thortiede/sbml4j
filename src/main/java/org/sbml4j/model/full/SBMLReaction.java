/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
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
import org.sbml4j.model.sbml.SBMLCompartmentalizedSBaseEntity;

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
