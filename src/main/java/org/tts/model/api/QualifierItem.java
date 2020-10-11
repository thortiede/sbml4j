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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * QualifierItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-10-10T16:25:40.095Z[GMT]")


public class QualifierItem   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("content")
  @Valid
  private List<QualifierItemContent> content = null;

  public QualifierItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of this qualifier
   * @return name
  **/
  @ApiModelProperty(example = "entrez-gene", value = "The name of this qualifier")
  
    public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public QualifierItem content(List<QualifierItemContent> content) {
    this.content = content;
    return this;
  }

  public QualifierItem addContentItem(QualifierItemContent contentItem) {
    if (this.content == null) {
      this.content = new ArrayList<QualifierItemContent>();
    }
    this.content.add(contentItem);
    return this;
  }

  /**
   * Get content
   * @return content
  **/
  @ApiModelProperty(value = "")
      @Valid
    public List<QualifierItemContent> getContent() {
    return content;
  }

  public void setContent(List<QualifierItemContent> content) {
    this.content = content;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QualifierItem qualifierItem = (QualifierItem) o;
    return Objects.equals(this.name, qualifierItem.name) &&
        Objects.equals(this.content, qualifierItem.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, content);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QualifierItem {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
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
