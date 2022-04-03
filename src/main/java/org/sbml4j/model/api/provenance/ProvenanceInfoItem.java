package org.sbml4j.model.api.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

//import org.openapitools.jackson.nullable.JsonNullable;
import org.sbml4j.model.api.ApiResponseItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * ProvenanceInfoItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
public class ProvenanceInfoItem {
  @JsonProperty("type") private String type;

  @JsonProperty("contents")
  private ApiResponseItem contents = null;

  @JsonProperty("wasAttributedTo") private AgentItem wasAttributedTo;

  @JsonProperty("wasGeneratedBy")
  @Valid
  private List<ActivityItem> wasGeneratedBy = null;

  @JsonProperty("wasDerivedFrom")
  @Valid
  private List<ProvenanceInfoItem> wasDerivedFrom = null;

  public ProvenanceInfoItem type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of this provenance-entity
   * @return type
   */
  @ApiModelProperty(example = "MappingNode",
                    value = "The type of this provenance-entity")

  public String
  getType() {
    return type;
  }

  public void setType(String type) { this.type = type; }

  public ProvenanceInfoItem contents(
      ApiResponseItem contents) {
    this.contents = contents;
    return this;
  }

  /**
   * Get contents
   * @return contents
   */
  @ApiModelProperty(value = "")

  @Valid

  public ApiResponseItem getContents() {
    return contents;
  }

  public void setContents(
      ApiResponseItem contents) {
    this.contents = contents;
  }

  public ProvenanceInfoItem wasAttributedTo(AgentItem wasAttributedTo) {
    this.wasAttributedTo = wasAttributedTo;
    return this;
  }

  /**
   * Get wasAttributedTo
   * @return wasAttributedTo
   */
  @ApiModelProperty(value = "")

  @Valid

  public AgentItem getWasAttributedTo() {
    return wasAttributedTo;
  }

  public void setWasAttributedTo(AgentItem wasAttributedTo) {
    this.wasAttributedTo = wasAttributedTo;
  }

  public ProvenanceInfoItem wasGeneratedBy(List<ActivityItem> wasGeneratedBy) {
    this.wasGeneratedBy = wasGeneratedBy;
    return this;
  }

  public ProvenanceInfoItem
  addWasGeneratedByItem(ActivityItem wasGeneratedByItem) {
    if (this.wasGeneratedBy == null) {
      this.wasGeneratedBy = new ArrayList<>();
    }
    this.wasGeneratedBy.add(wasGeneratedByItem);
    return this;
  }

  /**
   * Get wasGeneratedBy
   * @return wasGeneratedBy
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<ActivityItem> getWasGeneratedBy() {
    return wasGeneratedBy;
  }

  public void setWasGeneratedBy(List<ActivityItem> wasGeneratedBy) {
    this.wasGeneratedBy = wasGeneratedBy;
  }

  public ProvenanceInfoItem
  wasDerivedFrom(List<ProvenanceInfoItem> wasDerivedFrom) {
    this.wasDerivedFrom = wasDerivedFrom;
    return this;
  }

  public ProvenanceInfoItem
  addWasDerivedFromItem(ProvenanceInfoItem wasDerivedFromItem) {
    if (this.wasDerivedFrom == null) {
      this.wasDerivedFrom = new ArrayList<>();
    }
    this.wasDerivedFrom.add(wasDerivedFromItem);
    return this;
  }

  /**
   * Get wasDerivedFrom
   * @return wasDerivedFrom
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<ProvenanceInfoItem> getWasDerivedFrom() {
    return wasDerivedFrom;
  }

  public void setWasDerivedFrom(List<ProvenanceInfoItem> wasDerivedFrom) {
    this.wasDerivedFrom = wasDerivedFrom;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProvenanceInfoItem provenanceInfoItem = (ProvenanceInfoItem)o;
    return Objects.equals(this.type, provenanceInfoItem.type) &&
        Objects.equals(this.contents, provenanceInfoItem.contents) &&
        Objects.equals(this.wasAttributedTo,
                       provenanceInfoItem.wasAttributedTo) &&
        Objects.equals(this.wasGeneratedBy,
                       provenanceInfoItem.wasGeneratedBy) &&
        Objects.equals(this.wasDerivedFrom, provenanceInfoItem.wasDerivedFrom);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, contents, wasAttributedTo, wasGeneratedBy,
                        wasDerivedFrom);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProvenanceInfoItem {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    contents: ").append(toIndentedString(contents)).append("\n");
    sb.append("    wasAttributedTo: ")
        .append(toIndentedString(wasAttributedTo))
        .append("\n");
    sb.append("    wasGeneratedBy: ")
        .append(toIndentedString(wasGeneratedBy))
        .append("\n");
    sb.append("    wasDerivedFrom: ")
        .append(toIndentedString(wasDerivedFrom))
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
