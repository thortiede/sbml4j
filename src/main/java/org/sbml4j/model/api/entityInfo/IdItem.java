package org.sbml4j.model.api.entityInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * IdItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
public class IdItem {
  @JsonProperty("gene") private String gene;

  @JsonProperty("secondary names")
  @Valid
  private List<String> secondaryNames = null;

  @JsonProperty("qualifier")
  @Valid
  private List<QualifierItem> qualifier = null;

  public IdItem gene(String gene) {
    this.gene = gene;
    return this;
  }

  /**
   * The gene name this item is for
   * @return gene
   */
  @ApiModelProperty(example = "BRAF", value = "The gene name this item is for")

  public String getGene() {
    return gene;
  }

  public void setGene(String gene) { this.gene = gene; }

  public IdItem secondaryNames(List<String> secondaryNames) {
    this.secondaryNames = secondaryNames;
    return this;
  }

  public IdItem addSecondaryNamesItem(String secondaryNamesItem) {
    if (this.secondaryNames == null) {
      this.secondaryNames = new ArrayList<>();
    }
    this.secondaryNames.add(secondaryNamesItem);
    return this;
  }

  /**
   * Get secondaryNames
   * @return secondaryNames
   */
  @ApiModelProperty(value = "")

  public List<String> getSecondaryNames() {
    return secondaryNames;
  }

  public void setSecondaryNames(List<String> secondaryNames) {
    this.secondaryNames = secondaryNames;
  }

  public IdItem qualifier(List<QualifierItem> qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  public IdItem addQualifierItem(QualifierItem qualifierItem) {
    if (this.qualifier == null) {
      this.qualifier = new ArrayList<>();
    }
    this.qualifier.add(qualifierItem);
    return this;
  }

  /**
   * The qualifier of this gene
   * @return qualifier
   */
  @ApiModelProperty(value = "The qualifier of this gene")

  @Valid

  public List<QualifierItem> getQualifier() {
    return qualifier;
  }

  public void setQualifier(List<QualifierItem> qualifier) {
    this.qualifier = qualifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IdItem idItem = (IdItem)o;
    return Objects.equals(this.gene, idItem.gene) &&
        Objects.equals(this.secondaryNames, idItem.secondaryNames) &&
        Objects.equals(this.qualifier, idItem.qualifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gene, secondaryNames, qualifier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdItem {\n");

    sb.append("    gene: ").append(toIndentedString(gene)).append("\n");
    sb.append("    secondaryNames: ")
        .append(toIndentedString(secondaryNames))
        .append("\n");
    sb.append("    qualifier: ")
        .append(toIndentedString(qualifier))
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
