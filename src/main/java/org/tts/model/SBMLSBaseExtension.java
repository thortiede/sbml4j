package org.tts.model;


public interface SBMLSBaseExtension {

	// should be moved/altered to fit into SBMLModelExtension
	public boolean extendsModel();
	
	// should be moved/altered to fit into SBMLDocumentExtension  
	public boolean extendsSBML();
	
	public boolean isRequired();
	
	public String getShortLabel();
	
	public void setShortLabel(String shortLabel);
	
	public String getNamespaceURI();
	
	public void setNamespaceURI(String namespaceURI);
	
}
