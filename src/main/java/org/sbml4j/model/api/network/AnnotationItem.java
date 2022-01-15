package org.sbml4j.model.api.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * The fields of the AnnotationItem are used to create annotations in a network
 * as follows:  Must provide  - either all fields associated with nodes
 * (nodeAnnotation, nodeAnnotationName) - or all fields associated wth relations
 * (relationAnnotation, relationAnnotationName)  - or both.  Fields related to
 * nodes: - nodeAnnotationName denotes the name under which the annotations in
 * the nodeAnnotation field of the request body are attached to nodes -
 * nodeAnnotation is a dictionary of \&quot;node-symbol\&quot;:
 * \&quot;annotation\&quot; - pairs where node-symbol must match one of the node
 * symbols in the network (see field nodeSymbols in FilterOptions); the
 * annotation is added to the node under the name defined in nodeAnnotationName
 * using the type in nodeAnnotationType      Fields related to relations: -
 * relationAnnotationName denotes the name under which the annotations in the
 * relationAnnotation field of the request body are attached to relations   -
 * relationAnnotation is a dictionary of \&quot;relation-symbol\&quot;:
 * \&quot;annotation\&quot; - pairs where realation-symbol must match one of the
 * relation symbols in the network (see field relationSymbols in FilterOptions);
 * the annotation is added to the relation under the name defined in
 * relationAnnotationName using the type in relationAnnotationType
 */
@ApiModel(
    description =
        "The fields of the AnnotationItem are used to create annotations in a network as follows:  Must provide  - either all fields associated with nodes (nodeAnnotation, nodeAnnotationName) - or all fields associated wth relations (relationAnnotation, relationAnnotationName)  - or both.  Fields related to nodes: - nodeAnnotationName denotes the name under which the annotations in the nodeAnnotation field of the request body are attached to nodes - nodeAnnotation is a dictionary of \"node-symbol\": \"annotation\" - pairs where node-symbol must match one of the node symbols in the network (see field nodeSymbols in FilterOptions); the annotation is added to the node under the name defined in nodeAnnotationName using the type in nodeAnnotationType      Fields related to relations: - relationAnnotationName denotes the name under which the annotations in the relationAnnotation field of the request body are attached to relations   - relationAnnotation is a dictionary of \"relation-symbol\": \"annotation\" - pairs where realation-symbol must match one of the relation symbols in the network (see field relationSymbols in FilterOptions); the annotation is added to the relation under the name defined in relationAnnotationName using the type in relationAnnotationType ")
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-03-26T13:08:34.054518+01:00[Europe/Berlin]")
public class AnnotationItem {
  @JsonProperty("nodeAnnotationName") private String nodeAnnotationName;

  @JsonProperty("nodeAnnotation")
  @Valid
  private Map<String, Object> nodeAnnotation = null;

  @JsonProperty("relationAnnotationName") private String relationAnnotationName;

  @JsonProperty("relationAnnotation")
  @Valid
  private Map<String, Object> relationAnnotation = null;

  public AnnotationItem nodeAnnotationName(String nodeAnnotationName) {
    this.nodeAnnotationName = nodeAnnotationName;
    return this;
  }

  /**
   * The name of the annotation to be added to nodes
   * @return nodeAnnotationName
   */
  @ApiModelProperty(example = "DriverGene",
                    value = "The name of the annotation to be added to nodes")

  public String
  getNodeAnnotationName() {
    return nodeAnnotationName;
  }

  public void setNodeAnnotationName(String nodeAnnotationName) {
    this.nodeAnnotationName = nodeAnnotationName;
  }

  public AnnotationItem
  nodeAnnotation(Map<String, Object> nodeAnnotation) {
    this.nodeAnnotation = nodeAnnotation;
    return this;
  }

  public AnnotationItem
  putNodeAnnotationItem(String key,
                        Object nodeAnnotationItem) {
    if (this.nodeAnnotation == null) {
      this.nodeAnnotation = new HashMap<>();
    }
    this.nodeAnnotation.put(key, nodeAnnotationItem);
    return this;
  }

  /**
   * Get nodeAnnotation
   * @return nodeAnnotation
   */
  @ApiModelProperty(example = "{\"BRAF\":true,\"BRCA\":false}", value = "")

  @Valid

  public Map<String, Object> getNodeAnnotation() {
    return nodeAnnotation;
  }

  public void
  setNodeAnnotation(Map<String, Object> nodeAnnotation) {
    this.nodeAnnotation = nodeAnnotation;
  }

  public AnnotationItem relationAnnotationName(String relationAnnotationName) {
    this.relationAnnotationName = relationAnnotationName;
    return this;
  }

  /**
   * The name of the annotation to be added to relations
   * @return relationAnnotationName
   */
  @ApiModelProperty(
      example = "traversalScore",
      value = "The name of the annotation to be added to relations")

  public String
  getRelationAnnotationName() {
    return relationAnnotationName;
  }

  public void setRelationAnnotationName(String relationAnnotationName) {
    this.relationAnnotationName = relationAnnotationName;
  }

  public AnnotationItem
  relationAnnotation(Map<String, Object> relationAnnotation) {
    this.relationAnnotation = relationAnnotation;
    return this;
  }

  public AnnotationItem
  putRelationAnnotationItem(String key,
                            Object relationAnnotationItem) {
    if (this.relationAnnotation == null) {
      this.relationAnnotation = new HashMap<>();
    }
    this.relationAnnotation.put(key, relationAnnotationItem);
    return this;
  }

  /**
   * Get relationAnnotation
   * @return relationAnnotation
   */
  @ApiModelProperty(
      example = "{\"BRAF-stimulation->BRCA\":1,\"BRCA-inhibition->BRAF\":5}",
      value = "")

  @Valid

  public Map<String, Object>
  getRelationAnnotation() {
    return relationAnnotation;
  }

  public void setRelationAnnotation(
      Map<String, Object> relationAnnotation) {
    this.relationAnnotation = relationAnnotation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnnotationItem annotationItem = (AnnotationItem)o;
    return Objects.equals(this.nodeAnnotationName,
                          annotationItem.nodeAnnotationName) &&
        Objects.equals(this.nodeAnnotation, annotationItem.nodeAnnotation) &&
        Objects.equals(this.relationAnnotationName,
                       annotationItem.relationAnnotationName) &&
        Objects.equals(this.relationAnnotation,
                       annotationItem.relationAnnotation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeAnnotationName, nodeAnnotation,
                        relationAnnotationName, relationAnnotation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnnotationItem {\n");

    sb.append("    nodeAnnotationName: ")
        .append(toIndentedString(nodeAnnotationName))
        .append("\n");
    sb.append("    nodeAnnotation: ")
        .append(toIndentedString(nodeAnnotation))
        .append("\n");
    sb.append("    relationAnnotationName: ")
        .append(toIndentedString(relationAnnotationName))
        .append("\n");
    sb.append("    relationAnnotation: ")
        .append(toIndentedString(relationAnnotation))
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
