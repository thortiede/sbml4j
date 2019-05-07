package org.tts.model.old;

public class FileInfoObject {

	String modelName;
	String fileName;
	boolean isQualitativeModel;
	int numSpecies;
	int numReactions;
	int numQualSpecies;
	int numTransitions;
	
	int numTransitionAnnotationCVTerms;
	int numTransitonCVTerms;
	
	
	public FileInfoObject(String fileName) {
		this.fileName = fileName;
	}

	public String getModelName() {
		return modelName;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public int getNumSpecies() {
		return numSpecies;
	}

	public void setNumSpecies(int numSpecies) {
		this.numSpecies = numSpecies;
	}

	public int getNumReactions() {
		return numReactions;
	}

	public void setNumReactions(int numReactions) {
		this.numReactions = numReactions;
	}

	public int getNumQualSpecies() {
		return numQualSpecies;
	}

	public void setNumQualSpecies(int numQualSpecies) {
		this.numQualSpecies = numQualSpecies;
	}

	public int getNumTransitions() {
		return numTransitions;
	}

	public void setNumTransitions(int numTransitions) {
		this.numTransitions = numTransitions;
	}

	public boolean isQualitativeModel() {
		return isQualitativeModel;
	}

	public void setQualitativeModel(boolean isQualitativeModel) {
		this.isQualitativeModel = isQualitativeModel;
	}

	public int getNumTransitionAnnotationCVTerms() {
		return numTransitionAnnotationCVTerms;
	}

	public void setNumTransitionAnnotationCVTerms(int numTransitionAnnotationCVTerms) {
		this.numTransitionAnnotationCVTerms = numTransitionAnnotationCVTerms;
	}

	public int getNumTransitonCVTerms() {
		return numTransitonCVTerms;
	}

	public void setNumTransitonCVTerms(int numTransitonCVTerms) {
		this.numTransitonCVTerms = numTransitonCVTerms;
	}
	
	
}
