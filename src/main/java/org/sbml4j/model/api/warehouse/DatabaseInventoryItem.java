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
package org.sbml4j.model.api.warehouse;

import java.util.Objects;

import org.sbml4j.model.api.ApiResponseItem;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * DatabaseInventoryItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
public class DatabaseInventoryItem extends ApiResponseItem {
  @JsonProperty("source") private String source;

  @JsonProperty("version") private String version;

  public DatabaseInventoryItem source(String source) {
    this.source = source;
    return this;
  }

  /**
   * The name of the database source
   * @return source
   */
  @ApiModelProperty(example = "KEGG", value = "The name of the database source")

  public String getSource() {
    return source;
  }

  public void setSource(String source) { this.source = source; }

  public DatabaseInventoryItem version(String version) {
    this.version = version;
    return this;
  }

  /**
   * The version of the database source
   * @return version
   */
  @ApiModelProperty(example = "v1.7",
                    value = "The version of the database source")

  public String
  getVersion() {
    return version;
  }

  public void setVersion(String version) { this.version = version; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DatabaseInventoryItem databaseInventoryItem = (DatabaseInventoryItem)o;
    return Objects.equals(this.source, databaseInventoryItem.source) &&
        Objects.equals(this.version, databaseInventoryItem.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, version);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DatabaseInventoryItem {\n");

    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
