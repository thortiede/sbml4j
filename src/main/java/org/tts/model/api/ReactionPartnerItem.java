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

import java.util.Objects;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * ReactionPartnerItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-10-10T16:25:40.095Z[GMT]")


public class ReactionPartnerItem   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("sboTerm")
  private String sboTerm = null;

  public ReactionPartnerItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the reaction partner
   * @return name
  **/
  @ApiModelProperty(example = "C20H22N7O6", value = "The name of the reaction partner")
  
    public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ReactionPartnerItem type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of the reaction partner
   * @return type
  **/
  @ApiModelProperty(example = "compound", value = "The type of the reaction partner")
  
    public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ReactionPartnerItem sboTerm(String sboTerm) {
    this.sboTerm = sboTerm;
    return this;
  }

  /**
   * The sboTerm of the reaction partner
   * @return sboTerm
  **/
  @ApiModelProperty(value = "The sboTerm of the reaction partner")
  
    public String getSboTerm() {
    return sboTerm;
  }

  public void setSboTerm(String sboTerm) {
    this.sboTerm = sboTerm;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReactionPartnerItem reactionPartnerItem = (ReactionPartnerItem) o;
    return Objects.equals(this.name, reactionPartnerItem.name) &&
        Objects.equals(this.type, reactionPartnerItem.type) &&
        Objects.equals(this.sboTerm, reactionPartnerItem.sboTerm);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, sboTerm);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReactionPartnerItem {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    sboTerm: ").append(toIndentedString(sboTerm)).append("\n");
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
