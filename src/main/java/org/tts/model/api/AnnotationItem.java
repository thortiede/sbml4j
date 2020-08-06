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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * AnnotationItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class AnnotationItem   {
  @JsonProperty("nodeAnnotationName")
  private String nodeAnnotationName = null;

  @JsonProperty("nodeAnnotationType")
  private String nodeAnnotationType = null;

  @JsonProperty("nodeAnnotation")
  @Valid
  private Map<String, Object> nodeAnnotation = null;

  @JsonProperty("relationAnnotationName")
  private String relationAnnotationName = null;

  @JsonProperty("relationAnnotationType")
  private String relationAnnotationType = null;

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
  **/
  @ApiModelProperty(example = "DriverGene", value = "The name of the annotation to be added to nodes")
  
    public String getNodeAnnotationName() {
    return nodeAnnotationName;
  }

  public void setNodeAnnotationName(String nodeAnnotationName) {
    this.nodeAnnotationName = nodeAnnotationName;
  }

  public AnnotationItem nodeAnnotationType(String nodeAnnotationType) {
    this.nodeAnnotationType = nodeAnnotationType;
    return this;
  }

  /**
   * The type of the annotation values (e.g. string, int, boolean)
   * @return nodeAnnotationType
  **/
  @ApiModelProperty(example = "boolean", value = "The type of the annotation values (e.g. string, int, boolean)")
  
    public String getNodeAnnotationType() {
    return nodeAnnotationType;
  }

  public void setNodeAnnotationType(String nodeAnnotationType) {
    this.nodeAnnotationType = nodeAnnotationType;
  }

  public AnnotationItem nodeAnnotation(Map<String, Object> nodeAnnotation) {
    this.nodeAnnotation = nodeAnnotation;
    return this;
  }

  public AnnotationItem putNodeAnnotationItem(String key, Object nodeAnnotationItem) {
    if (this.nodeAnnotation == null) {
      this.nodeAnnotation = new HashMap<>();
    }
    this.nodeAnnotation.put(key, nodeAnnotationItem);
    return this;
  }

  /**
   * Get nodeAnnotation
   * @return nodeAnnotation
  **/
  @ApiModelProperty(example = "{\"BRAF\":true,\"BRCA\":false}", value = "")
  
    public Map<String, Object> getNodeAnnotation() {
    return nodeAnnotation;
  }

  public void setNodeAnnotation(Map<String, Object> nodeAnnotation) {
    this.nodeAnnotation = nodeAnnotation;
  }

  public AnnotationItem relationAnnotationName(String relationAnnotationName) {
    this.relationAnnotationName = relationAnnotationName;
    return this;
  }

  /**
   * The name of the annotation to be added to relations
   * @return relationAnnotationName
  **/
  @ApiModelProperty(example = "traversalScore", value = "The name of the annotation to be added to relations")
  
    public String getRelationAnnotationName() {
    return relationAnnotationName;
  }

  public void setRelationAnnotationName(String relationAnnotationName) {
    this.relationAnnotationName = relationAnnotationName;
  }

  public AnnotationItem relationAnnotationType(String relationAnnotationType) {
    this.relationAnnotationType = relationAnnotationType;
    return this;
  }

  /**
   * The type of the annotation values (e.g. string, int, boolean)
   * @return relationAnnotationType
  **/
  @ApiModelProperty(example = "int", value = "The type of the annotation values (e.g. string, int, boolean)")
  
    public String getRelationAnnotationType() {
    return relationAnnotationType;
  }

  public void setRelationAnnotationType(String relationAnnotationType) {
    this.relationAnnotationType = relationAnnotationType;
  }

  public AnnotationItem relationAnnotation(Map<String, Object> relationAnnotation) {
    this.relationAnnotation = relationAnnotation;
    return this;
  }

  public AnnotationItem putRelationAnnotationItem(String key, Object relationAnnotationItem) {
    if (this.relationAnnotation == null) {
      this.relationAnnotation = new HashMap<>();
    }
    this.relationAnnotation.put(key, relationAnnotationItem);
    return this;
  }

  /**
   * Get relationAnnotation
   * @return relationAnnotation
  **/
  @ApiModelProperty(example = "{\"BRAF-stimulation->BRCA\":1,\"BRCA-inhibition->BRAF\":5}", value = "")
  
    public Map<String, Object> getRelationAnnotation() {
    return relationAnnotation;
  }

  public void setRelationAnnotation(Map<String, Object> relationAnnotation) {
    this.relationAnnotation = relationAnnotation;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnnotationItem annotationItem = (AnnotationItem) o;
    return Objects.equals(this.nodeAnnotationName, annotationItem.nodeAnnotationName) &&
        Objects.equals(this.nodeAnnotationType, annotationItem.nodeAnnotationType) &&
        Objects.equals(this.nodeAnnotation, annotationItem.nodeAnnotation) &&
        Objects.equals(this.relationAnnotationName, annotationItem.relationAnnotationName) &&
        Objects.equals(this.relationAnnotationType, annotationItem.relationAnnotationType) &&
        Objects.equals(this.relationAnnotation, annotationItem.relationAnnotation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeAnnotationName, nodeAnnotationType, nodeAnnotation, relationAnnotationName, relationAnnotationType, relationAnnotation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnnotationItem {\n");
    
    sb.append("    nodeAnnotationName: ").append(toIndentedString(nodeAnnotationName)).append("\n");
    sb.append("    nodeAnnotationType: ").append(toIndentedString(nodeAnnotationType)).append("\n");
    sb.append("    nodeAnnotation: ").append(toIndentedString(nodeAnnotation)).append("\n");
    sb.append("    relationAnnotationName: ").append(toIndentedString(relationAnnotationName)).append("\n");
    sb.append("    relationAnnotationType: ").append(toIndentedString(relationAnnotationType)).append("\n");
    sb.append("    relationAnnotation: ").append(toIndentedString(relationAnnotation)).append("\n");
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
