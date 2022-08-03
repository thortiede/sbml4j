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
package org.sbml4j.model.sbml.simple;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.sbml4j.model.sbml.SBMLCompartmentalizedSBaseEntity;
import org.sbml4j.model.sbml.SBMLSpecies;

public class SBMLSimpleReaction extends SBMLCompartmentalizedSBaseEntity {
	
	private boolean reversible;
	
	@Relationship(type = "IS_REACTANT", direction = Relationship.OUTGOING)
	private List<SBMLSpecies> reactants;
	
	@Relationship(type = "IS_PRODUCT", direction = Relationship.OUTGOING)
	private List<SBMLSpecies> products;
	
	@Relationship(type = "IS_CATALYST", direction = Relationship.OUTGOING)
	private List<SBMLSpecies> catalysts;

	public boolean isReversible() {
		return reversible;
	}

	public void setReversible(boolean reversible) {
		this.reversible = reversible;
	}

	public List<SBMLSpecies> getReactants() {
		return reactants;
	}

	public void setReactants(List<SBMLSpecies> reactants) {
		this.reactants = reactants;
	}

	public void addReactant(SBMLSpecies species) {
		if (this.reactants == null) {
			this.reactants = new ArrayList<>();
		}
		this.reactants.add(species);
	}
	
	public List<SBMLSpecies> getProducts() {
		return products;
	}

	public void setProducts(List<SBMLSpecies> products) {
		this.products = products;
	}

	public void addProduct(SBMLSpecies species) {
		if (this.products == null) {
			this.products = new ArrayList<>();
		}
		this.products.add(species);
	}
	
	public List<SBMLSpecies> getCatalysts() {
		return catalysts;
	}

	public void setCatalysts(List<SBMLSpecies> catalysts) {
		this.catalysts = catalysts;
	}
	
	public void addCatalysts(SBMLSpecies species) {
		if (this.catalysts == null) {
			this.catalysts = new ArrayList<>();
		}
		this.catalysts.add(species);
	}
	
}
