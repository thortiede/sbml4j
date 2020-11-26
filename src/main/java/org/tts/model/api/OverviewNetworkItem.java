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
package org.tts.model.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Submit a list of genes to build a overview network based on the network given in &#x27;baseNetworkUUID&#x27;. If the latter is omitted, a default Network defined by the service will be used. Typically this  will contain all relations and reactions known to the database. The given genes will retrieve a boolean annotation  given by annotationName. The networkName will be used if given.  A standard name is generated when ommited. 
 */
@ApiModel(description = "Submit a list of genes to build a overview network based on the network given in 'baseNetworkUUID'. If the latter is omitted, a default Network defined by the service will be used. Typically this  will contain all relations and reactions known to the database. The given genes will retrieve a boolean annotation  given by annotationName. The networkName will be used if given.  A standard name is generated when ommited. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-10-06T11:55:24.162Z[GMT]")


public class OverviewNetworkItem   {
  @JsonProperty("baseNetworkUUID")
  private UUID baseNetworkUUID = null;

  @JsonProperty("genes")
  @Valid
  private List<String> genes = null;

  @JsonProperty("annotationName")
  private String annotationName = null;

  @JsonProperty("networkName")
  private String networkName = null;

  public OverviewNetworkItem baseNetworkUUID(UUID baseNetworkUUID) {
    this.baseNetworkUUID = baseNetworkUUID;
    return this;
  }

  /**
   * Get baseNetworkUUID
   * @return baseNetworkUUID
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public UUID getBaseNetworkUUID() {
    return baseNetworkUUID;
  }

  public void setBaseNetworkUUID(UUID baseNetworkUUID) {
    this.baseNetworkUUID = baseNetworkUUID;
  }

  public OverviewNetworkItem genes(List<String> genes) {
    this.genes = genes;
    return this;
  }

  public OverviewNetworkItem addGenesItem(String genesItem) {
    if (this.genes == null) {
      this.genes = new ArrayList<String>();
    }
    this.genes.add(genesItem);
    return this;
  }

  /**
   * Get genes
   * @return genes
  **/
  @ApiModelProperty(value = "")
  
    public List<String> getGenes() {
    return genes;
  }

  public void setGenes(List<String> genes) {
    this.genes = genes;
  }

  public OverviewNetworkItem annotationName(String annotationName) {
    this.annotationName = annotationName;
    return this;
  }

  /**
   * Get annotationName
   * @return annotationName
  **/
  @ApiModelProperty(example = "Drivergene", value = "")
  
    public String getAnnotationName() {
    return annotationName;
  }

  public void setAnnotationName(String annotationName) {
    this.annotationName = annotationName;
  }

  public OverviewNetworkItem networkName(String networkName) {
    this.networkName = networkName;
    return this;
  }

  /**
   * Get networkName
   * @return networkName
  **/
  @ApiModelProperty(example = "Drivergene network", value = "")
  
    public String getNetworkName() {
    return networkName;
  }

  public void setNetworkName(String networkName) {
    this.networkName = networkName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OverviewNetworkItem overviewNetworkItem = (OverviewNetworkItem) o;
    return Objects.equals(this.baseNetworkUUID, overviewNetworkItem.baseNetworkUUID) &&
        Objects.equals(this.genes, overviewNetworkItem.genes) &&
        Objects.equals(this.annotationName, overviewNetworkItem.annotationName) &&
        Objects.equals(this.networkName, overviewNetworkItem.networkName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseNetworkUUID, genes, annotationName, networkName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OverviewNetworkItem {\n");
    
    sb.append("    baseNetworkUUID: ").append(toIndentedString(baseNetworkUUID)).append("\n");
    sb.append("    genes: ").append(toIndentedString(genes)).append("\n");
    sb.append("    annotationName: ").append(toIndentedString(annotationName)).append("\n");
    sb.append("    networkName: ").append(toIndentedString(networkName)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
