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
package org.sbml4j.model.api.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.sbml4j.model.api.ApiRequestItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A list of node-symbols for which an action is to be performed (i.e. generate
 * a multi-gene context)
 */
@ApiModel(
    description =
        "A list of node-symbols for which an action is to be performed (i.e. generate a multi-gene context) ")
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class NodeList extends ApiRequestItem {
  @JsonProperty("genes") @Valid protected List<String> genes = null;

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
   */
  @ApiModelProperty(value = "")

  public List<String> getGenes() {
    return genes;
  }

  public void setGenes(List<String> genes) { this.genes = genes; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeList nodeList = (NodeList)o;
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
