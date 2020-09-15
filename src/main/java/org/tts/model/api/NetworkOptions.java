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

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * NetworkOptions consisting of AnnotationItem and FilterOptions
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class NetworkOptions   {
  @JsonProperty("annotation")
  private AnnotationItem annotation = null;

  @JsonProperty("filter")
  private FilterOptions filter = null;

  public NetworkOptions annotation(AnnotationItem annotation) {
    this.annotation = annotation;
    return this;
  }

  /**
   * Get annotation
   * @return annotation
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public AnnotationItem getAnnotation() {
    return annotation;
  }

  public void setAnnotation(AnnotationItem annotation) {
    this.annotation = annotation;
  }

  public NetworkOptions filter(FilterOptions filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Get filter
   * @return filter
  **/
  @ApiModelProperty(value = "")
  
    @Valid
    public FilterOptions getFilter() {
    return filter;
  }

  public void setFilter(FilterOptions filter) {
    this.filter = filter;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkOptions networkOptions = (NetworkOptions) o;
    return Objects.equals(this.annotation, networkOptions.annotation) &&
        Objects.equals(this.filter, networkOptions.filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(annotation, filter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NetworkOptions {\n");
    
    sb.append("    annotation: ").append(toIndentedString(annotation)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
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
