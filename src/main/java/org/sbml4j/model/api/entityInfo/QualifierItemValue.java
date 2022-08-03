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

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * QualifierItemValue
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-02-08T21:39:37.829466+01:00[Europe/Berlin]")
public class QualifierItemValue {
  @JsonProperty("url") private String url;

  @JsonProperty("identifier") private String identifier;

  public QualifierItemValue url(String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   * @return url
   */
  @ApiModelProperty(value = "")

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) { this.url = url; }

  public QualifierItemValue identifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * The identifier in the given identifier system
   * @return identifier
   */
  @ApiModelProperty(example = "hsa:10152",
                    value = "The identifier in the given identifier system")

  public String
  getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) { this.identifier = identifier; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QualifierItemValue qualifierItemValue = (QualifierItemValue)o;
    return Objects.equals(this.url, qualifierItemValue.url) &&
        Objects.equals(this.identifier, qualifierItemValue.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, identifier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QualifierItemValue {\n");

    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    identifier: ")
        .append(toIndentedString(identifier))
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
