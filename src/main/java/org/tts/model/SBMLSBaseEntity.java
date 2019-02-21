package org.tts.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;
import org.sbml.jsbml.CVTerm;
import org.tts.Exception.SBML4jAddExtensionException;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity(label="SBase")
public class SBMLSBaseEntity extends GraphBaseEntity {

	private String sBaseId;
	
	private String sBaseName;
	
	private String sBaseMetaId;
	
	private String sBaseSboTerm;
	
	private String sBaseNotes;
	
	@Transient
	private List<CVTerm> cvTermList;
	
	@Relationship(type="IN_MODEL", direction = Relationship.OUTGOING)
	private List<SBMLModelEntity> models = new ArrayList<>();
	
	@Relationship(type="HAS", direction = Relationship.OUTGOING)
	private List<SBMLSBaseExtension> extensions;
	
	@Relationship(type = "BQ", direction = Relationship.OUTGOING)
	private List<BiomodelsQualifier> biomodelQualifier;
	
	
	public List<BiomodelsQualifier> getBiomodelQualifier() {
		return biomodelQualifier;
	}

	public void setBiomodelQualifier(List<BiomodelsQualifier> biomodelQualifier) {
		this.biomodelQualifier = biomodelQualifier;
	}
	
	public boolean addBiomodelQualifier(BiomodelsQualifier newBiomodelQualifier) {
		if (biomodelQualifier == null) {
			biomodelQualifier = new ArrayList<>();
		}
		if (biomodelQualifier.contains(newBiomodelQualifier))
		{ 
			return true;
		} else {
			try {
				this.biomodelQualifier.add(newBiomodelQualifier);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	

	public SBMLSBaseExtension getExtension(String nameOrURI) {
		for (SBMLSBaseExtension plugin : this.extensions) {
			if (plugin.getShortLabel().equals(nameOrURI) || plugin.getNamespaceURI().equals(nameOrURI)) {
				return plugin;
			}
		}
		return null;
	}
	
	public void addExtension(String nameOrURI, SBMLSBaseExtension sBaseExtension) throws SBML4jAddExtensionException {
		if (this.getExtension(nameOrURI) == null) {
			if (sBaseExtension.getShortLabel().equals(nameOrURI) || sBaseExtension.getNamespaceURI().equals(nameOrURI)) {
				this.extensions.add(sBaseExtension);
			} else {
				throw new SBML4jAddExtensionException("Cannot add Extension (ShortLabel: " + sBaseExtension.getShortLabel() + " / URI: "
														+ sBaseExtension.getNamespaceURI() + ") und different nameOrURI (given: " + nameOrURI 
														+ ") to SBMLSBaseEntity " + this.getsBaseName());
			}
		} else {
			throw new SBML4jAddExtensionException("Extension with nameOrURI " + nameOrURI + " already exists on SBMLSBaseEntity " + this.getsBaseName());
		}
	}

	public String getsBaseId() {
		return sBaseId;
	}

	public void setsBaseId(String sBaseId) {
		this.sBaseId = sBaseId;
	}

	public String getsBaseName() {
		return sBaseName;
	}

	public void setsBaseName(String sBaseName) {
		this.sBaseName = sBaseName;
	}

	public String getsBaseMetaId() {
		return sBaseMetaId;
	}

	public void setsBaseMetaId(String sBaseMetaId) {
		this.sBaseMetaId = sBaseMetaId;
	}

	public String getsBaseSboTerm() {
		return sBaseSboTerm;
	}

	public void setsBaseSboTerm(String sBaseSboTerm) {
		this.sBaseSboTerm = sBaseSboTerm;
	}

	public String getsBaseNotes() {
		return sBaseNotes;
	}

	public void setsBaseNotes(String sBaseNotes) {
		this.sBaseNotes = sBaseNotes;
	}
	
	@JsonIgnore
	public List<CVTerm> getCvTermList() {
		return cvTermList;
	}

	public void setCvTermList(List<CVTerm> cvTermList) {
		this.cvTermList = cvTermList;
	}

	public List<SBMLModelEntity> getModels() {
		return models;
	}

	public void setModels(List<SBMLModelEntity> models) {
		this.models = models;
	}

	public List<SBMLSBaseExtension> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<SBMLSBaseExtension> extensions) {
		this.extensions = extensions;
	}
	
	
}
