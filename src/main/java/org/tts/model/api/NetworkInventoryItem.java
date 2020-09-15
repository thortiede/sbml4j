/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
 */

package org.tts.model.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.validation.annotation.Validated;
import org.tts.model.common.GraphEnum.NetworkMappingType;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * NetworkInventoryItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class NetworkInventoryItem extends RepresentationModel<NetworkInventoryItem>  {
  @JsonProperty("UUID")
  private UUID UUID = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("organismCode")
  private String organismCode = null;

  @JsonProperty("numberOfNodes")
  private Integer numberOfNodes = null;

  @JsonProperty("numberOfRelations")
  private Integer numberOfRelations = null;

  @JsonProperty("nodeTypes")
  @Valid
  private List<String> nodeTypes = null;

  @JsonProperty("relationTypes")
  @Valid
  private List<String> relationTypes = null;

  @JsonProperty("networkMappingType")
  private NetworkMappingType networkMappingType = null;

  
  public NetworkInventoryItem UUID(UUID UUID) {
    this.UUID = UUID;
    return this;
  }

  /**
   * Get UUID
   * @return UUID
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public UUID getUUID() {
    return UUID;
  }

  public void setUUID(UUID UUID) {
    this.UUID = UUID;
  }

  public NetworkInventoryItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "Example Network", value = "")
  
    public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public NetworkInventoryItem organismCode(String organismCode) {
    this.organismCode = organismCode;
    return this;
  }

  /**
   * Three letter organism code this network belongs to
   * @return organismCode
  **/
  @ApiModelProperty(example = "hsa", value = "Three letter organism code this network belongs to")
  
    public String getOrganismCode() {
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
  **/
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
  **/
  @ApiModelProperty(value = "")
  
    public Integer getNumberOfRelations() {
    return numberOfRelations;
  }

  public void setNumberOfRelations(Integer numberOfRelations) {
    this.numberOfRelations = numberOfRelations;
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
  **/
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
  **/
  @ApiModelProperty(value = "")
  
    public List<String> getRelationTypes() {
    return relationTypes;
  }

  public void setRelationTypes(List<String> relationTypes) {
    this.relationTypes = relationTypes;
  }

  public NetworkInventoryItem networkMappingType(NetworkMappingType networkMappingType) {
    this.networkMappingType = networkMappingType;
    return this;
  }

  /**
   * One of: REGULATORY, SIGNALLING, PPI, METABOLIC, PATHWAYMAPPING
   * @return networkMappingType
  **/
  @ApiModelProperty(example = "REGULATORY", value = "One of: REGULATORY, SIGNALLING, PPI, METABOLIC, PATHWAYMAPPING")
  
    public NetworkMappingType getNetworkMappingType() {
    return networkMappingType;
  }

  public void setNetworkMappingType(NetworkMappingType networkMappingType) {
    this.networkMappingType = networkMappingType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkInventoryItem networkInventoryItem = (NetworkInventoryItem) o;
    return Objects.equals(this.UUID, networkInventoryItem.UUID) &&
        Objects.equals(this.name, networkInventoryItem.name) &&
        Objects.equals(this.organismCode, networkInventoryItem.organismCode) &&
        Objects.equals(this.numberOfNodes, networkInventoryItem.numberOfNodes) &&
        Objects.equals(this.numberOfRelations, networkInventoryItem.numberOfRelations) &&
        Objects.equals(this.nodeTypes, networkInventoryItem.nodeTypes) &&
        Objects.equals(this.relationTypes, networkInventoryItem.relationTypes) &&
        Objects.equals(this.networkMappingType, networkInventoryItem.networkMappingType) &&
        Objects.equals(this.getLinks(), networkInventoryItem.getLinks());
  }

  @Override
  public int hashCode() {
    return Objects.hash(UUID, name, organismCode, numberOfNodes, numberOfRelations, nodeTypes, relationTypes, networkMappingType, getLinks());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NetworkInventoryItem {\n");
    
    sb.append("    UUID: ").append(toIndentedString(UUID)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    organismCode: ").append(toIndentedString(organismCode)).append("\n");
    sb.append("    numberOfNodes: ").append(toIndentedString(numberOfNodes)).append("\n");
    sb.append("    numberOfRelations: ").append(toIndentedString(numberOfRelations)).append("\n");
    sb.append("    nodeTypes: ").append(toIndentedString(nodeTypes)).append("\n");
    sb.append("    relationTypes: ").append(toIndentedString(relationTypes)).append("\n");
    sb.append("    networkMappingType: ").append(toIndentedString(networkMappingType)).append("\n");
    sb.append("    links: ").append(toIndentedString(getLinks())).append("\n");
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
