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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * ReactionInfoItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class ReactionInfoItem {
  @JsonProperty("name") private String name;

  @JsonProperty("type") private String type;

  @JsonProperty("sboTerm") private String sboTerm;

  @JsonProperty("reversible") private Boolean reversible;

  @JsonProperty("reactants")
  @Valid
  private List<ReactionPartnerItem> reactants = null;

  @JsonProperty("products")
  @Valid
  private List<ReactionPartnerItem> products = null;

  @JsonProperty("catalysts")
  @Valid
  private List<ReactionPartnerItem> catalysts = null;

  public ReactionInfoItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the reaction
   * @return name
   */
  @ApiModelProperty(example = "rn:R04125", value = "The name of the reaction")

  public String getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public ReactionInfoItem type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of the reaction
   * @return type
   */
  @ApiModelProperty(example = "biochemical reaction",
                    value = "The type of the reaction")

  public String
  getType() {
    return type;
  }

  public void setType(String type) { this.type = type; }

  public ReactionInfoItem sboTerm(String sboTerm) {
    this.sboTerm = sboTerm;
    return this;
  }

  /**
   * The SBO Term of the reaction
   * @return sboTerm
   */
  @ApiModelProperty(value = "The SBO Term of the reaction")

  public String getSboTerm() {
    return sboTerm;
  }

  public void setSboTerm(String sboTerm) { this.sboTerm = sboTerm; }

  public ReactionInfoItem reversible(Boolean reversible) {
    this.reversible = reversible;
    return this;
  }

  /**
   * Whether the reaction is reversible
   * @return reversible
   */
  @ApiModelProperty(example = "false",
                    value = "Whether the reaction is reversible")

  public Boolean
  getReversible() {
    return reversible;
  }

  public void setReversible(Boolean reversible) {
    this.reversible = reversible;
  }

  public ReactionInfoItem reactants(List<ReactionPartnerItem> reactants) {
    this.reactants = reactants;
    return this;
  }

  public ReactionInfoItem addReactantsItem(ReactionPartnerItem reactantsItem) {
    if (this.reactants == null) {
      this.reactants = new ArrayList<>();
    }
    this.reactants.add(reactantsItem);
    return this;
  }

  /**
   * Get reactants
   * @return reactants
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<ReactionPartnerItem> getReactants() {
    return reactants;
  }

  public void setReactants(List<ReactionPartnerItem> reactants) {
    this.reactants = reactants;
  }

  public ReactionInfoItem products(List<ReactionPartnerItem> products) {
    this.products = products;
    return this;
  }

  public ReactionInfoItem addProductsItem(ReactionPartnerItem productsItem) {
    if (this.products == null) {
      this.products = new ArrayList<>();
    }
    this.products.add(productsItem);
    return this;
  }

  /**
   * Get products
   * @return products
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<ReactionPartnerItem> getProducts() {
    return products;
  }

  public void setProducts(List<ReactionPartnerItem> products) {
    this.products = products;
  }

  public ReactionInfoItem catalysts(List<ReactionPartnerItem> catalysts) {
    this.catalysts = catalysts;
    return this;
  }

  public ReactionInfoItem addCatalystsItem(ReactionPartnerItem catalystsItem) {
    if (this.catalysts == null) {
      this.catalysts = new ArrayList<>();
    }
    this.catalysts.add(catalystsItem);
    return this;
  }

  /**
   * Get catalysts
   * @return catalysts
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<ReactionPartnerItem> getCatalysts() {
    return catalysts;
  }

  public void setCatalysts(List<ReactionPartnerItem> catalysts) {
    this.catalysts = catalysts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReactionInfoItem reactionInfoItem = (ReactionInfoItem)o;
    return Objects.equals(this.name, reactionInfoItem.name) &&
        Objects.equals(this.type, reactionInfoItem.type) &&
        Objects.equals(this.sboTerm, reactionInfoItem.sboTerm) &&
        Objects.equals(this.reversible, reactionInfoItem.reversible) &&
        Objects.equals(this.reactants, reactionInfoItem.reactants) &&
        Objects.equals(this.products, reactionInfoItem.products) &&
        Objects.equals(this.catalysts, reactionInfoItem.catalysts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, sboTerm, reversible, reactants, products,
                        catalysts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReactionInfoItem {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    sboTerm: ").append(toIndentedString(sboTerm)).append("\n");
    sb.append("    reversible: ")
        .append(toIndentedString(reversible))
        .append("\n");
    sb.append("    reactants: ")
        .append(toIndentedString(reactants))
        .append("\n");
    sb.append("    products: ").append(toIndentedString(products)).append("\n");
    sb.append("    catalysts: ")
        .append(toIndentedString(catalysts))
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
