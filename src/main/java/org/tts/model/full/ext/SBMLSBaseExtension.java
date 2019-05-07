package org.tts.model;


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
