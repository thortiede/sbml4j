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
package org.sbml4j.model.api.entityInfo;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * RelationInfoItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class RelationInfoItem {
  @JsonProperty("name") private String name;

  @JsonProperty("type") private String type;

  @JsonProperty("sboTerm") private String sboTerm;

  /**
   * The direction of this relation in relation to the parent
   */
  public enum DirectionEnum {
    IN("in"),

    OUT("out"),

    INOUT("inout");

    private String value;

    DirectionEnum(String value) { this.value = value; }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static DirectionEnum fromValue(String value) {
      for (DirectionEnum b : DirectionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("direction") private DirectionEnum direction;

  public RelationInfoItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the related gene
   * @return name
   */
  @ApiModelProperty(example = "MAP2K", value = "The name of the related gene")

  public String getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public RelationInfoItem type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of the relation between the two genes
   * @return type
   */
  @ApiModelProperty(example = "stimulation",
                    value = "The type of the relation between the two genes")

  public String
  getType() {
    return type;
  }

  public void setType(String type) { this.type = type; }

  public RelationInfoItem sboTerm(String sboTerm) {
    this.sboTerm = sboTerm;
    return this;
  }

  /**
   * The sboTerm of this relation
   * @return sboTerm
   */
  @ApiModelProperty(example = "SBO:0000170",
                    value = "The sboTerm of this relation")

  public String
  getSboTerm() {
    return sboTerm;
  }

  public void setSboTerm(String sboTerm) { this.sboTerm = sboTerm; }

  public RelationInfoItem direction(DirectionEnum direction) {
    this.direction = direction;
    return this;
  }

  /**
   * The direction of this relation in relation to the parent
   * @return direction
   */
  @ApiModelProperty(
      example = "out",
      value = "The direction of this relation in relation to the parent")

  public DirectionEnum
  getDirection() {
    return direction;
  }

  public void setDirection(DirectionEnum direction) {
    this.direction = direction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RelationInfoItem relationInfoItem = (RelationInfoItem)o;
    return Objects.equals(this.name, relationInfoItem.name) &&
        Objects.equals(this.type, relationInfoItem.type) &&
        Objects.equals(this.sboTerm, relationInfoItem.sboTerm) &&
        Objects.equals(this.direction, relationInfoItem.direction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, sboTerm, direction);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RelationInfoItem {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    sboTerm: ").append(toIndentedString(sboTerm)).append("\n");
    sb.append("    direction: ")
        .append(toIndentedString(direction))
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
