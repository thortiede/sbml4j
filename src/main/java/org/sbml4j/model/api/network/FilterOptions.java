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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The filterOptions to be used in the step upon creation.  - NodeSymbols: Only
 * nodes listed here can be part of the network (and if they match given
 * nodeTypes)   - NodeTypes: Only nodes having one of the listed nodesTypes here
 * can be part of the network (and if they are contained in NodeSymbols)   -
 * RelationSymbols: Only relations listed here can be part of the network (and
 * if they match given relationTypes)   - RelationTypes: Only edges having one
 * of the listed relationTypes here can be part of the network
 */
@ApiModel(
    description =
        "The filterOptions to be used in the step upon creation.  - NodeSymbols: Only nodes listed here can be part of the network (and if they match given nodeTypes)   - NodeTypes: Only nodes having one of the listed nodesTypes here can be part of the network (and if they are contained in NodeSymbols)   - RelationSymbols: Only relations listed here can be part of the network (and if they match given relationTypes)   - RelationTypes: Only edges having one of the listed relationTypes here can be part of the network ")
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
@JsonInclude(Include.NON_NULL)
public class FilterOptions extends ApiRequestItem {
  @JsonProperty("nodeTypes") @Valid private List<String> nodeTypes = null;

  @JsonProperty("relationTypes")
  @Valid
  private List<String> relationTypes = null;

  @JsonProperty("nodeSymbols") @Valid private List<String> nodeSymbols = null;

  @JsonProperty("relationSymbols")
  @Valid
  private List<String> relationSymbols = null;

  public FilterOptions nodeTypes(List<String> nodeTypes) {
    this.nodeTypes = nodeTypes;
    return this;
  }

  public FilterOptions addNodeTypesItem(String nodeTypesItem) {
    if (this.nodeTypes == null) {
      this.nodeTypes = new ArrayList<>();
    }
    this.nodeTypes.add(nodeTypesItem);
    return this;
  }

  /**
   * Get nodeTypes
   * @return nodeTypes
   */
  @ApiModelProperty(value = "")

  public List<String> getNodeTypes() {
    return nodeTypes;
  }

  public void setNodeTypes(List<String> nodeTypes) {
    this.nodeTypes = nodeTypes;
  }

  public FilterOptions relationTypes(List<String> relationTypes) {
    this.relationTypes = relationTypes;
    return this;
  }

  public FilterOptions addRelationTypesItem(String relationTypesItem) {
    if (this.relationTypes == null) {
      this.relationTypes = new ArrayList<>();
    }
    this.relationTypes.add(relationTypesItem);
    return this;
  }

  /**
   * Get relationTypes
   * @return relationTypes
   */
  @ApiModelProperty(value = "")

  public List<String> getRelationTypes() {
    return relationTypes;
  }

  public void setRelationTypes(List<String> relationTypes) {
    this.relationTypes = relationTypes;
  }

  public FilterOptions nodeSymbols(List<String> nodeSymbols) {
    this.nodeSymbols = nodeSymbols;
    return this;
  }

  public FilterOptions addNodeSymbolsItem(String nodeSymbolsItem) {
    if (this.nodeSymbols == null) {
      this.nodeSymbols = new ArrayList<>();
    }
    this.nodeSymbols.add(nodeSymbolsItem);
    return this;
  }

  /**
   * Get nodeSymbols
   * @return nodeSymbols
   */
  @ApiModelProperty(value = "")

  public List<String> getNodeSymbols() {
    return nodeSymbols;
  }

  public void setNodeSymbols(List<String> nodeSymbols) {
    this.nodeSymbols = nodeSymbols;
  }

  public FilterOptions relationSymbols(List<String> relationSymbols) {
    this.relationSymbols = relationSymbols;
    return this;
  }

  public FilterOptions addRelationSymbolsItem(String relationSymbolsItem) {
    if (this.relationSymbols == null) {
      this.relationSymbols = new ArrayList<>();
    }
    this.relationSymbols.add(relationSymbolsItem);
    return this;
  }

  /**
   * Get relationSymbols
   * @return relationSymbols
   */
  @ApiModelProperty(value = "")

  public List<String> getRelationSymbols() {
    return relationSymbols;
  }

  public void setRelationSymbols(List<String> relationSymbols) {
    this.relationSymbols = relationSymbols;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FilterOptions filterOptions = (FilterOptions)o;
    return Objects.equals(this.nodeTypes, filterOptions.nodeTypes) &&
        Objects.equals(this.relationTypes, filterOptions.relationTypes) &&
        Objects.equals(this.nodeSymbols, filterOptions.nodeSymbols) &&
        Objects.equals(this.relationSymbols, filterOptions.relationSymbols);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeTypes, relationTypes, nodeSymbols, relationSymbols);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FilterOptions {\n");

    sb.append("    nodeTypes: ")
        .append(toIndentedString(nodeTypes))
        .append("\n");
    sb.append("    relationTypes: ")
        .append(toIndentedString(relationTypes))
        .append("\n");
    sb.append("    nodeSymbols: ")
        .append(toIndentedString(nodeSymbols))
        .append("\n");
    sb.append("    relationSymbols: ")
        .append(toIndentedString(relationSymbols))
        .append("\n");
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
