package org.sbml4j.model.api.pathway;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.sbml4j.model.api.ApiResponseItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * PathwayCollectionItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
public class PathwayCollectionItem  extends ApiResponseItem{
  @JsonProperty("name") private String name;

  @JsonProperty("description") private String description;

  @JsonProperty("hadMember") @Valid private List<String> hadMember = null;

  public PathwayCollectionItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the pathway collection
   * @return name
   */
  @ApiModelProperty(example = "ExPC",
                    value = "The name of the pathway collection")

  public String
  getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public PathwayCollectionItem description(String description) {
    this.description = description;
    return this;
  }

  /**
   * A descriptional text for this pathway collection
   * @return description
   */
  @ApiModelProperty(example = "Example pathway collection",
                    value = "A descriptional text for this pathway collection")

  public String
  getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PathwayCollectionItem hadMember(List<String> hadMember) {
    this.hadMember = hadMember;
    return this;
  }

  public PathwayCollectionItem addHadMemberItem(String hadMemberItem) {
    if (this.hadMember == null) {
      this.hadMember = new ArrayList<>();
    }
    this.hadMember.add(hadMemberItem);
    return this;
  }

  /**
   * Get hadMember
   * @return hadMember
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<String> getHadMember() {
    return hadMember;
  }

  public void setHadMember(List<String> hadMember) { this.hadMember = hadMember; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathwayCollectionItem pathwayCollectionItem = (PathwayCollectionItem)o;
    return Objects.equals(this.name, pathwayCollectionItem.name) &&
        Objects.equals(this.description, pathwayCollectionItem.description) &&
        Objects.equals(this.hadMember, pathwayCollectionItem.hadMember);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, hadMember);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PathwayCollectionItem {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ")
        .append(toIndentedString(description))
        .append("\n");
    sb.append("    hadMember: ")
        .append(toIndentedString(hadMember))
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
