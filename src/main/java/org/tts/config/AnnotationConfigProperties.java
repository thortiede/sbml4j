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
package org.tts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tts.model.common.GraphEnum.AnnotationName;

@ConfigurationProperties(prefix = "sbml4j.annotation")
public class AnnotationConfigProperties {

	private String keggGenesSeparator;
	
	private String entrezGeneSeparator;
	
	private String ensembleSeparator;
	
	private String hgncSeparator;
	
	private String omimSeparator;
	
	private String uniprotSeparator;
	
	private String obo_chebiSeparator;
	
	private String pdb_ccdSeparator;
	
	private String ecCodeSeparator;
	
	private String secondaryNamesSeparator;
	
	private String pathwaysSeparator;

	private boolean keepFirst;
	
	private boolean append;
	
	private boolean annotateWithLinks;
	
	public boolean isSetSeparator(AnnotationName name) {
		String compareString = null;
		if (name == null) {
			return false;
		}
		switch (name) {
		case ECCODE:
			compareString = this.getEcCodeSeparator();
			break;
		case ENSEMBL:
			compareString = this.getEnsembleSeparator();
			break;
		case ENTREZGENE: 
			compareString = this.getEntrezGeneSeparator();
			break;
		case HGNC:
			compareString = this.getHgncSeparator();
			break;
		case KEGGGENES:
			compareString = this.getKeggGenesSeparator();
			break;
		case NAME:
			compareString = null;
			break;
		case OMIM:
			compareString = this.getOmimSeparator();
			break;
		case PATHWAYS:
			compareString = this.getPathwaysSeparator();
			break;
		case SECONDARYNAMES:
			compareString = this.getSecondaryNamesSeparator();
			break;
		case UNIPROT:
			compareString = this.getUniprotSeparator();
			break;
		default:
			compareString = null;
			break;
		}
		return compareString == null ? false : true;
	}
	
	public String getSeparator(AnnotationName name) {
		switch (name) {
		case ECCODE:
			return this.getEcCodeSeparator();
		case ENSEMBL:
			return this.getEnsembleSeparator();
		case ENTREZGENE: 
			return this.getEntrezGeneSeparator();
		case HGNC:
			return this.getHgncSeparator();
		case KEGGGENES:
			return this.getKeggGenesSeparator();
		case NAME:
			return null;
		case OMIM:
			return this.getOmimSeparator();
		case PATHWAYS:
			return this.getPathwaysSeparator();
		case SECONDARYNAMES:
			return this.getSecondaryNamesSeparator();
		case UNIPROT:
			return this.getUniprotSeparator();
		default:
			return null;
		}
	}
	
	
	
	
	public String getKeggGenesSeparator() {
		return keggGenesSeparator;
	}

	public void setKeggGenesSeparator(String keggGenesSeparator) {
		this.keggGenesSeparator = keggGenesSeparator;
	}

	public String getEntrezGeneSeparator() {
		return entrezGeneSeparator;
	}

	public void setEntrezGeneSeparator(String entrezGeneSeparator) {
		this.entrezGeneSeparator = entrezGeneSeparator;
	}

	public String getEnsembleSeparator() {
		return ensembleSeparator;
	}

	public void setEnsembleSeparator(String ensembleSeparator) {
		this.ensembleSeparator = ensembleSeparator;
	}

	public String getHgncSeparator() {
		return hgncSeparator;
	}

	public void setHgncSeparator(String hgncSeparator) {
		this.hgncSeparator = hgncSeparator;
	}

	public String getOmimSeparator() {
		return omimSeparator;
	}

	public void setOmimSeparator(String omimSeparator) {
		this.omimSeparator = omimSeparator;
	}

	public String getUniprotSeparator() {
		return uniprotSeparator;
	}

	public void setUniprotSeparator(String uniprotSeparator) {
		this.uniprotSeparator = uniprotSeparator;
	}

	public String getObo_chebiSeparator() {
		return obo_chebiSeparator;
	}

	public void setObo_chebiSeparator(String obo_chebiSeparator) {
		this.obo_chebiSeparator = obo_chebiSeparator;
	}

	public String getPdb_ccdSeparator() {
		return pdb_ccdSeparator;
	}

	public void setPdb_ccdSeparator(String pdb_ccdSeparator) {
		this.pdb_ccdSeparator = pdb_ccdSeparator;
	}

	public String getEcCodeSeparator() {
		return ecCodeSeparator;
	}

	public void setEcCodeSeparator(String ecCodeSeparator) {
		this.ecCodeSeparator = ecCodeSeparator;
	}

	public String getSecondaryNamesSeparator() {
		return secondaryNamesSeparator;
	}

	public void setSecondaryNamesSeparator(String secondaryNamesSeparator) {
		this.secondaryNamesSeparator = secondaryNamesSeparator;
	}

	public String getPathwaysSeparator() {
		return pathwaysSeparator;
	}

	public void setPathwaysSeparator(String pathwaysSeparator) {
		this.pathwaysSeparator = pathwaysSeparator;
	}

	public boolean isKeepFirst() {
		return keepFirst;
	}

	public void setKeepFirst(boolean keepFirst) {
		this.keepFirst = keepFirst;
	}

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public boolean isAnnotateWithLinks() {
		return annotateWithLinks;
	}

	public void setAnnotateWithLinks(boolean annotateWithLinks) {
		this.annotateWithLinks = annotateWithLinks;
	}
}
