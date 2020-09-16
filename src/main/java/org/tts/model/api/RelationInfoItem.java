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

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * RelationInfoItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class RelationInfoItem   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("pathways")
  @Valid
  private List<PathwayInfoItem> pathways = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("sboTerm")
  private String sboTerm = null;

  /**
   * The direction of this relation in relation to the parent
   */
  public enum DirectionEnum {
    IN("in"),
    
    OUT("out"),
    
    INOUT("inout");

    private String value;

    DirectionEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static DirectionEnum fromValue(String text) {
      for (DirectionEnum b : DirectionEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }
  @JsonProperty("direction")
  private DirectionEnum direction = null;

  public RelationInfoItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the related gene
   * @return name
  **/
  @ApiModelProperty(example = "MAP2K", value = "The name of the related gene")
  
    public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RelationInfoItem pathways(List<PathwayInfoItem> pathways) {
    this.pathways = pathways;
    return this;
  }

  public RelationInfoItem addPathwaysItem(PathwayInfoItem pathwaysItem) {
    if (this.pathways == null) {
      this.pathways = new ArrayList<>();
    }
    this.pathways.add(pathwaysItem);
    return this;
  }

  /**
   * The pathways this related gene is found in
   * @return pathways
  **/
  @ApiModelProperty(value = "The pathways this related gene is found in")
      @Valid
    public List<PathwayInfoItem> getPathways() {
    return pathways;
  }

  public void setPathways(List<PathwayInfoItem> pathways) {
    this.pathways = pathways;
  }

  public RelationInfoItem type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of the relation between the two genes
   * @return type
  **/
  @ApiModelProperty(example = "stimulation", value = "The type of the relation between the two genes")
  
    public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public RelationInfoItem sboTerm(String sboTerm) {
    this.sboTerm = sboTerm;
    return this;
  }

  /**
   * The sboTerm of this relation
   * @return sboTerm
  **/
  @ApiModelProperty(example = "SBO:0000170", value = "The sboTerm of this relation")
  
    public String getSboTerm() {
    return sboTerm;
  }

  public void setSboTerm(String sboTerm) {
    this.sboTerm = sboTerm;
  }

  public RelationInfoItem direction(DirectionEnum direction) {
    this.direction = direction;
    return this;
  }

  /**
   * The direction of this relation in relation to the parent
   * @return direction
  **/
  @ApiModelProperty(example = "out", value = "The direction of this relation in relation to the parent")
  
    public DirectionEnum getDirection() {
    return direction;
  }

  public void setDirection(DirectionEnum direction) {
    this.direction = direction;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RelationInfoItem relationInfoItem = (RelationInfoItem) o;
    return Objects.equals(this.name, relationInfoItem.name) &&
        Objects.equals(this.pathways, relationInfoItem.pathways) &&
        Objects.equals(this.type, relationInfoItem.type) &&
        Objects.equals(this.sboTerm, relationInfoItem.sboTerm) &&
        Objects.equals(this.direction, relationInfoItem.direction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, pathways, type, sboTerm, direction);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RelationInfoItem {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    pathways: ").append(toIndentedString(pathways)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    sboTerm: ").append(toIndentedString(sboTerm)).append("\n");
    sb.append("    direction: ").append(toIndentedString(direction)).append("\n");
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
