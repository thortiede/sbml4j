package org.sbml4j.model.api;

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
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class QualifierItemContent {
  @JsonProperty("type") private String type;

  @JsonProperty("url") @Valid private List<String> url = null;

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

  public QualifierItemContent url(List<String> url) {
    this.url = url;
    return this;
  }

  public QualifierItemContent addUrlItem(String urlItem) {
    if (this.url == null) {
      this.url = new ArrayList<>();
    }
    this.url.add(urlItem);
    return this;
  }

  /**
   * Get url
   * @return url
   */
  @ApiModelProperty(value = "")

  public List<String> getUrl() {
    return url;
  }

  public void setUrl(List<String> url) { this.url = url; }

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
        Objects.equals(this.url, qualifierItemContent.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, url);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QualifierItemContent {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
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
