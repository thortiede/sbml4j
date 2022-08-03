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
package org.sbml4j.model.full.ext;

import org.sbml4j.model.sbml.SBMLSBaseEntity;

public class SBMLSBaseExtension extends SBMLSBaseEntity {

	private boolean extendsModel;
	
	private boolean extendsSBML;
	
	private String shortLabel;
	
	private String namespaceURI;
	
	private boolean required;

	public boolean isExtendsModel() {
		return extendsModel;
	}

	public void setExtendsModel(boolean extendsModel) {
		this.extendsModel = extendsModel;
	}

	public boolean isExtendsSBML() {
		return extendsSBML;
	}

	public void setExtendsSBML(boolean extendsSBML) {
		this.extendsSBML = extendsSBML;
	}

	public String getShortLabel() {
		return shortLabel;
	}

	public void setShortLabel(String shortLabel) {
		this.shortLabel = shortLabel;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
	
	
	
}
