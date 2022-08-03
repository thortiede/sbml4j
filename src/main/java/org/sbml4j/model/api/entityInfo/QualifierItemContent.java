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
 * QualifierItemContent
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-02-08T21:39:37.829466+01:00[Europe/Berlin]")
public class QualifierItemContent {
  @JsonProperty("type") private String type;

  @JsonProperty("values") @Valid private List<QualifierItemValue> values = null;

  public QualifierItemContent type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of this qualifer
   * @return type
   */
  @ApiModelProperty(example = "BQB_HAS_VERSION",
                    value = "The type of this qualifer")

  public String
  getType() {
    return type;
  }

  public void setType(String type) { this.type = type; }

  public QualifierItemContent values(List<QualifierItemValue> values) {
    this.values = values;
    return this;
  }

  public QualifierItemContent addValuesItem(QualifierItemValue valuesItem) {
    if (this.values == null) {
      this.values = new ArrayList<>();
    }
    this.values.add(valuesItem);
    return this;
  }

  /**
   * Get values
   * @return values
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<QualifierItemValue> getValues() {
    return values;
  }

  public void setValues(List<QualifierItemValue> values) {
    this.values = values;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QualifierItemContent qualifierItemContent = (QualifierItemContent)o;
    return Objects.equals(this.type, qualifierItemContent.type) &&
        Objects.equals(this.values, qualifierItemContent.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, values);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QualifierItemContent {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
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
