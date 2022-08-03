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
package org.sbml4j.model.full;

import org.neo4j.ogm.annotation.NodeEntity;
import org.sbml4j.model.sbml.SBMLSBaseEntity;


@NodeEntity(label = "SBML")
public class SBMLDocumentEntity extends SBMLSBaseEntity {

	private String sbmlXmlNamespace;
	
	private int sbmlLevel;
	
	private int sbmlVersion;
	
	private String sbmlFileName;
		
	/*@Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
	private SBMLModelEntity model;
	 */
	public String getSbmlXmlNamespace() {
		return sbmlXmlNamespace;
	}

	public void setSbmlXmlNamespace(String sbmlXmlNamespace) {
		this.sbmlXmlNamespace = sbmlXmlNamespace;
	}

	public int getSbmlLevel() {
		return sbmlLevel;
	}

	public void setSbmlLevel(int sbmlLevel) {
		this.sbmlLevel = sbmlLevel;
	}

	public int getSbmlVersion() {
		return sbmlVersion;
	}

	public void setSbmlVersion(int sbmlVersion) {
		this.sbmlVersion = sbmlVersion;
	}

	public String getSbmlFileName() {
		return sbmlFileName;
	}

	public void setSbmlFileName(String sbmlFileName) {
		this.sbmlFileName = sbmlFileName;
	}

	/*@JsonIgnore
	public SBMLModelEntity getModel() {
		return model;
	}

	public void setModel(SBMLModelEntity model) {
		this.model = model;
	}*/
		
}
