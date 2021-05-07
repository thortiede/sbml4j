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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description
 * @author Thorsten Tiede
 *
 * @since 0.1
 */
@ConfigurationProperties(prefix = "sbml4j.externalresources.mdanderson")
public class MdAndersonProperties {

	private boolean addMdAndersonAnnotation;
	
	private List<String> genelist;
	
	private String baseurl;
	
	private String section;

	public List<String> getGenelist() {
		return genelist;
	}

	public void setGenelist(List<String> genelist) {
		this.genelist = genelist;
	}

	public String getBaseurl() {
		return baseurl;
	}

	public void setBaseurl(String baseurl) {
		this.baseurl = baseurl;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public boolean isAddMdAndersonAnnotation() {
		return addMdAndersonAnnotation;
	}

	public void setAddMdAndersonAnnotation(boolean addMdAndersonAnnotation) {
		this.addMdAndersonAnnotation = addMdAndersonAnnotation;
	}
}
