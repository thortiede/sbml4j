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
import java.util.UUID;

import javax.validation.Valid;

import org.sbml4j.model.api.ApiResponseItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * NetworkInventoryItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class NetworkInventoryItem extends ApiResponseItem {
  @JsonProperty("UUID") private UUID UUID;

  @JsonProperty("name") private String name;

  @JsonProperty("organismCode") private String organismCode;

  @JsonProperty("numberOfNodes") private Integer numberOfNodes;

  @JsonProperty("numberOfRelations") private Integer numberOfRelations;
  
  @JsonProperty("numberOfReactions") private Integer numberOfReactions;

  @JsonProperty("nodeTypes") @Valid private List<String> nodeTypes = null;

  @JsonProperty("relationTypes")
  @Valid
  private List<String> relationTypes = null;

  @JsonProperty("networkMappingType") private String networkMappingType;

  public NetworkInventoryItem UUID(UUID UUID) {
    this.UUID = UUID;
    return this;
  }

  /**
   * Get UUID
   * @return UUID
   */
  @ApiModelProperty(value = "")

  @Valid

  public UUID getUUID() {
    return UUID;
  }

  public void setUUID(UUID UUID) { this.UUID = UUID; }

  public NetworkInventoryItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @ApiModelProperty(example = "Example Network", value = "")

  public String getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public NetworkInventoryItem organismCode(String organismCode) {
    this.organismCode = organismCode;
    return this;
  }

  /**
   * Three letter organism code this network belongs to
   * @return organismCode
   */
  @ApiModelProperty(
      example = "hsa",
      value = "Three letter organism code this network belongs to")

  public String
  getOrganismCode() {
    return organismCode;
  }

  public void setOrganismCode(String organismCode) {
    this.organismCode = organismCode;
  }

  public NetworkInventoryItem numberOfNodes(Integer numberOfNodes) {
    this.numberOfNodes = numberOfNodes;
    return this;
  }

  /**
   * Get numberOfNodes
   * @return numberOfNodes
   */
  @ApiModelProperty(value = "")

  public Integer getNumberOfNodes() {
    return numberOfNodes;
  }

  public void setNumberOfNodes(Integer numberOfNodes) {
    this.numberOfNodes = numberOfNodes;
  }

  public NetworkInventoryItem numberOfRelations(Integer numberOfRelations) {
    this.numberOfRelations = numberOfRelations;
    return this;
  }

  /**
   * Get numberOfRelations
   * @return numberOfRelations
   */
  @ApiModelProperty(value = "")

  public Integer getNumberOfRelations() {
    return numberOfRelations;
  }

  public void setNumberOfRelations(Integer numberOfRelations) {
    this.numberOfRelations = numberOfRelations;
  }

  public NetworkInventoryItem numberOfReactions(Integer numberOfReactions) {
    this.numberOfReactions = numberOfReactions;
    return this;
  }

  /**
   * Get numberOfReactions
   * @return numberOfReactions
   */
  @ApiModelProperty(value = "")

  public Integer getNumberOfReactions() {
    return numberOfReactions;
  }

  public void setNumberOfReactions(Integer numberOfReactions) {
    this.numberOfReactions = numberOfReactions;
  }

  public NetworkInventoryItem nodeTypes(List<String> nodeTypes) {
    this.nodeTypes = nodeTypes;
    return this;
  }

  public NetworkInventoryItem addNodeTypesItem(String nodeTypesItem) {
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

  public NetworkInventoryItem relationTypes(List<String> relationTypes) {
    this.relationTypes = relationTypes;
    return this;
  }

  public NetworkInventoryItem addRelationTypesItem(String relationTypesItem) {
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

  public NetworkInventoryItem networkMappingType(String networkMappingType) {
    this.networkMappingType = networkMappingType;
    return this;
  }

  /**
   * One of: REGULATORY, SIGNALLING, PPI, METABOLIC, PATHWAYMAPPING
   * @return networkMappingType
   */
  @ApiModelProperty(
      example = "REGULATORY",
      value = "One of: REGULATORY, SIGNALLING, PPI, METABOLIC, PATHWAYMAPPING")

  public String
  getNetworkMappingType() {
    return networkMappingType;
  }

  public void setNetworkMappingType(String networkMappingType) {
    this.networkMappingType = networkMappingType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkInventoryItem networkInventoryItem = (NetworkInventoryItem)o;
    return Objects.equals(this.UUID, networkInventoryItem.UUID) &&
        Objects.equals(this.name, networkInventoryItem.name) &&
        Objects.equals(this.organismCode, networkInventoryItem.organismCode) &&
        Objects.equals(this.numberOfNodes,
                       networkInventoryItem.numberOfNodes) &&
        Objects.equals(this.numberOfRelations,
                       networkInventoryItem.numberOfRelations) &&
        Objects.equals(this.nodeTypes, networkInventoryItem.nodeTypes) &&
        Objects.equals(this.relationTypes,
                       networkInventoryItem.relationTypes) &&
        Objects.equals(this.networkMappingType,
                       networkInventoryItem.networkMappingType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(UUID, name, organismCode, numberOfNodes,
                        numberOfRelations, nodeTypes, relationTypes,
                        networkMappingType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NetworkInventoryItem {\n");

    sb.append("    UUID: ").append(toIndentedString(UUID)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    organismCode: ")
        .append(toIndentedString(organismCode))
        .append("\n");
    sb.append("    numberOfNodes: ")
        .append(toIndentedString(numberOfNodes))
        .append("\n");
    sb.append("    numberOfRelations: ")
        .append(toIndentedString(numberOfRelations))
        .append("\n");
    sb.append("    nodeTypes: ")
        .append(toIndentedString(nodeTypes))
        .append("\n");
    sb.append("    relationTypes: ")
        .append(toIndentedString(relationTypes))
        .append("\n");
    sb.append("    networkMappingType: ")
        .append(toIndentedString(networkMappingType))
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
