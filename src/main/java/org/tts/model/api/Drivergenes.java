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
 * Submit a list of driver genes to build a drivergene network based on the network given in &#x27;baseNetworkUUID&#x27;. If the latter is omitted, a default Network defined by the service will be used. Typically this  will contain all relations and reactions known to the database. 
 */
@ApiModel(description = "Submit a list of driver genes to build a drivergene network based on the network given in 'baseNetworkUUID'. If the latter is omitted, a default Network defined by the service will be used. Typically this  will contain all relations and reactions known to the database. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class Drivergenes   {
  @JsonProperty("baseNetworkUUID")
  private UUID baseNetworkUUID = null;

  @JsonProperty("genes")
  @Valid
  private List<String> genes = null;

  public Drivergenes baseNetworkUUID(UUID baseNetworkUUID) {
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

  public Drivergenes genes(List<String> genes) {
    this.genes = genes;
    return this;
  }

  public Drivergenes addGenesItem(String genesItem) {
    if (this.genes == null) {
      this.genes = new ArrayList<>();
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Drivergenes drivergenes = (Drivergenes) o;
    return Objects.equals(this.baseNetworkUUID, drivergenes.baseNetworkUUID) &&
        Objects.equals(this.genes, drivergenes.genes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseNetworkUUID, genes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Drivergenes {\n");
    
    sb.append("    baseNetworkUUID: ").append(toIndentedString(baseNetworkUUID)).append("\n");
    sb.append("    genes: ").append(toIndentedString(genes)).append("\n");
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
  
  /**
   * Convert the DriverGenes Object to a singleLine String
   * @return single Line String of this Drivergenes Object
   */
  public String toSingleLineString () {
	  StringBuilder sb = new StringBuilder();
	  sb.append("drivergenes_for_");
	  if(this.baseNetworkUUID != null) {
		  sb.append(baseNetworkUUID);
		  sb.append("_");
	  }
	  sb.append("with_genes");
	  for(String gene : genes) {
		  sb.append('_');
		  sb.append(gene);
	  }
	  return sb.toString();
  }
}
