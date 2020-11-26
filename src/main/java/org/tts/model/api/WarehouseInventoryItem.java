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
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * WarehouseInventoryItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class WarehouseInventoryItem   {
  @JsonProperty("UUID")
  private UUID UUID = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("organismCode")
  private String organismCode = null;

  public WarehouseInventoryItem UUID(UUID UUID) {
    this.UUID = UUID;
    return this;
  }

  /**
   * Get UUID
   * @return UUID
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public UUID getUUID() {
    return UUID;
  }

  public void setUUID(UUID UUID) {
    this.UUID = UUID;
  }

  public WarehouseInventoryItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(example = "Example Mapping", value = "")
  
    public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public WarehouseInventoryItem organismCode(String organismCode) {
    this.organismCode = organismCode;
    return this;
  }

  /**
   * Three letter organism code this network belongs to
   * @return organismCode
  **/
  @ApiModelProperty(example = "hsa", value = "Three letter organism code this network belongs to")
  
    public String getOrganismCode() {
    return organismCode;
  }

  public void setOrganismCode(String organismCode) {
    this.organismCode = organismCode;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WarehouseInventoryItem warehouseInventoryItem = (WarehouseInventoryItem) o;
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
    sb.append("    organismCode: ").append(toIndentedString(organismCode)).append("\n");
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
