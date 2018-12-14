package org.tts.model;

public class FileInfoObject {

	String modelName;
	String fileName;
	boolean isQualitativeModel;
	int numSpecies;
	int numReactions;
	int numQualSpecies;
	int numTransitions;
	
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
	
	
}
