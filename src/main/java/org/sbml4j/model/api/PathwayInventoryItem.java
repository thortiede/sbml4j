package org.tts.model.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * PathwayInventoryItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class PathwayInventoryItem {
  @JsonProperty("UUID") private UUID UUID;

  @JsonProperty("name") private String name;

  @JsonProperty("pathwayId") private String pathwayId;

  @JsonProperty("organismCode") private String organismCode;

  @JsonProperty("numberOfNodes") private Integer numberOfNodes;

  @JsonProperty("numberOfTransitions") private Integer numberOfTransitions;

  @JsonProperty("numberOfReactions") private Integer numberOfReactions;

  @JsonProperty("nodeTypes") @Valid private List<String> nodeTypes = null;

  @JsonProperty("transitionTypes")
  @Valid
  private List<String> transitionTypes = null;

  @JsonProperty("compartments") @Valid private List<String> compartments = null;

  public PathwayInventoryItem UUID(UUID UUID) {
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

  public PathwayInventoryItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @ApiModelProperty(example = "Example Pathway", value = "")

  public String getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public PathwayInventoryItem pathwayId(String pathwayId) {
    this.pathwayId = pathwayId;
    return this;
  }

  /**
   * Get pathwayId
   * @return pathwayId
   */
  @ApiModelProperty(example = "path_hsa05225", value = "")

  public String getPathwayId() {
    return pathwayId;
  }

  public void setPathwayId(String pathwayId) { this.pathwayId = pathwayId; }

  public PathwayInventoryItem organismCode(String organismCode) {
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

  public PathwayInventoryItem numberOfNodes(Integer numberOfNodes) {
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

  public PathwayInventoryItem numberOfTransitions(Integer numberOfTransitions) {
    this.numberOfTransitions = numberOfTransitions;
    return this;
  }

  /**
   * Get numberOfTransitions
   * @return numberOfTransitions
   */
  @ApiModelProperty(value = "")

  public Integer getNumberOfTransitions() {
    return numberOfTransitions;
  }

  public void setNumberOfTransitions(Integer numberOfTransitions) {
    this.numberOfTransitions = numberOfTransitions;
  }

  public PathwayInventoryItem numberOfReactions(Integer numberOfReactions) {
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

  public PathwayInventoryItem nodeTypes(List<String> nodeTypes) {
    this.nodeTypes = nodeTypes;
    return this;
  }

  public PathwayInventoryItem addNodeTypesItem(String nodeTypesItem) {
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

  public PathwayInventoryItem transitionTypes(List<String> transitionTypes) {
    this.transitionTypes = transitionTypes;
    return this;
  }

  public PathwayInventoryItem
  addTransitionTypesItem(String transitionTypesItem) {
    if (this.transitionTypes == null) {
      this.transitionTypes = new ArrayList<>();
    }
    this.transitionTypes.add(transitionTypesItem);
    return this;
  }

  /**
   * Get transitionTypes
   * @return transitionTypes
   */
  @ApiModelProperty(value = "")

  public List<String> getTransitionTypes() {
    return transitionTypes;
  }

  public void setTransitionTypes(List<String> transitionTypes) {
    this.transitionTypes = transitionTypes;
  }

  public PathwayInventoryItem compartments(List<String> compartments) {
    this.compartments = compartments;
    return this;
  }

  public PathwayInventoryItem addCompartmentsItem(String compartmentsItem) {
    if (this.compartments == null) {
      this.compartments = new ArrayList<>();
    }
    this.compartments.add(compartmentsItem);
    return this;
  }

  /**
   * Get compartments
   * @return compartments
   */
  @ApiModelProperty(value = "")

  public List<String> getCompartments() {
    return compartments;
  }

  public void setCompartments(List<String> compartments) {
    this.compartments = compartments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathwayInventoryItem pathwayInventoryItem = (PathwayInventoryItem)o;
    return Objects.equals(this.UUID, pathwayInventoryItem.UUID) &&
        Objects.equals(this.name, pathwayInventoryItem.name) &&
        Objects.equals(this.pathwayId, pathwayInventoryItem.pathwayId) &&
        Objects.equals(this.organismCode, pathwayInventoryItem.organismCode) &&
        Objects.equals(this.numberOfNodes,
                       pathwayInventoryItem.numberOfNodes) &&
        Objects.equals(this.numberOfTransitions,
                       pathwayInventoryItem.numberOfTransitions) &&
        Objects.equals(this.numberOfReactions,
                       pathwayInventoryItem.numberOfReactions) &&
        Objects.equals(this.nodeTypes, pathwayInventoryItem.nodeTypes) &&
        Objects.equals(this.transitionTypes,
                       pathwayInventoryItem.transitionTypes) &&
        Objects.equals(this.compartments, pathwayInventoryItem.compartments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(UUID, name, pathwayId, organismCode, numberOfNodes,
                        numberOfTransitions, numberOfReactions, nodeTypes,
                        transitionTypes, compartments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PathwayInventoryItem {\n");

    sb.append("    UUID: ").append(toIndentedString(UUID)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    pathwayId: ")
        .append(toIndentedString(pathwayId))
        .append("\n");
    sb.append("    organismCode: ")
        .append(toIndentedString(organismCode))
        .append("\n");
    sb.append("    numberOfNodes: ")
        .append(toIndentedString(numberOfNodes))
        .append("\n");
    sb.append("    numberOfTransitions: ")
        .append(toIndentedString(numberOfTransitions))
        .append("\n");
    sb.append("    numberOfReactions: ")
        .append(toIndentedString(numberOfReactions))
        .append("\n");
    sb.append("    nodeTypes: ")
        .append(toIndentedString(nodeTypes))
        .append("\n");
    sb.append("    transitionTypes: ")
        .append(toIndentedString(transitionTypes))
        .append("\n");
    sb.append("    compartments: ")
        .append(toIndentedString(compartments))
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
