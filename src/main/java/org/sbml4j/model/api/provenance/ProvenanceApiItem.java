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
package org.sbml4j.model.api.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public class ProvenanceApiItem {

	

	  @JsonProperty("provenance")
	  @Valid
	  private List<Map<String, Object>> provenance = null;
	  
	  public ProvenanceApiItem
	  provenance(List<Map<String, Object>> provenance) {
	    this.provenance = provenance;
	    return this;
	  }

	  public ProvenanceApiItem
	  addProvenanceItem(Map<String, Object> provenanceItem) {
	    if (this.provenance == null) {
	      this.provenance = new ArrayList<>();
	    }
	    this.provenance.add(provenanceItem);
	    return this;
	  }

	  /**
	   * Get provenance
	   * @return provenance
	   */
	  @ApiModelProperty(value = "")

	  @Valid

	  public List<Map<String, Object>> getProvenance() {
	    return provenance;
	  }

	  public void setProvenance(List<Map<String, Object>> provenance) {
	    this.provenance = provenance;
	  }

}
