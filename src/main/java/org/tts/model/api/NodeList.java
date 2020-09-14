package org.tts.model.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A list of node-symbols for which an action is to be performed (i.e. generate a multi-gene context) 
 */
@ApiModel(description = "A list of node-symbols for which an action is to be performed (i.e. generate a multi-gene context) ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class NodeList   {
  @JsonProperty("genes")
  @Valid
  private List<String> genes = null;

  public NodeList genes(List<String> genes) {
    this.genes = genes;
    return this;
  }

  public NodeList addGenesItem(String genesItem) {
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
    NodeList nodeList = (NodeList) o;
    return Objects.equals(this.genes, nodeList.genes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NodeList {\n");
    
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
}
