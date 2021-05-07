package org.tts.model.api;

import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * NetworkOptions
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class NetworkOptions {
  @JsonProperty("annotation") private AnnotationItem annotation;

  @JsonProperty("filter") private FilterOptions filter;

  public NetworkOptions annotation(AnnotationItem annotation) {
    this.annotation = annotation;
    return this;
  }

  /**
   * Get annotation
   * @return annotation
   */
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
   */
  @ApiModelProperty(value = "")

  @Valid

  public FilterOptions getFilter() {
    return filter;
  }

  public void setFilter(FilterOptions filter) { this.filter = filter; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkOptions networkOptions = (NetworkOptions)o;
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

    sb.append("    annotation: ")
        .append(toIndentedString(annotation))
        .append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
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
