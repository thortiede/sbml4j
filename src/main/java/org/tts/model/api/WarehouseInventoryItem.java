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
