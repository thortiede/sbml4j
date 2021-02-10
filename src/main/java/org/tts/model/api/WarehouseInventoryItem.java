package org.tts.model.api;

import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * WarehouseInventoryItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class WarehouseInventoryItem {
  @JsonProperty("UUID") private UUID UUID;

  @JsonProperty("name") private String name;

  @JsonProperty("organismCode") private String organismCode;

  public WarehouseInventoryItem UUID(UUID UUID) {
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

  public WarehouseInventoryItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @ApiModelProperty(example = "Example Mapping", value = "")

  public String getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public WarehouseInventoryItem organismCode(String organismCode) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WarehouseInventoryItem warehouseInventoryItem = (WarehouseInventoryItem)o;
    return Objects.equals(this.UUID, warehouseInventoryItem.UUID) &&
        Objects.equals(this.name, warehouseInventoryItem.name) &&
        Objects.equals(this.organismCode, warehouseInventoryItem.organismCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(UUID, name, organismCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WarehouseInventoryItem {\n");

    sb.append("    UUID: ").append(toIndentedString(UUID)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    organismCode: ")
        .append(toIndentedString(organismCode))
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
