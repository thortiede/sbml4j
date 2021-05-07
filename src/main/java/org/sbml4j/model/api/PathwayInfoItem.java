package org.tts.model.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * PathwayInfoItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class PathwayInfoItem {
  @JsonProperty("name") private String name;

  @JsonProperty("KEGGId") private String keGGId;

  @JsonProperty("KEGGUrl") private String keGGUrl;

  public PathwayInfoItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the Pathway
   * @return name
   */
  @ApiModelProperty(example = "HIF-1 signaling pathway (Homo sapiens (human))",
                    value = "The name of the Pathway")

  public String
  getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public PathwayInfoItem keGGId(String keGGId) {
    this.keGGId = keGGId;
    return this;
  }

  /**
   * The Kegg pathways identifier
   * @return keGGId
   */
  @ApiModelProperty(example = "path_hsa04066",
                    value = "The Kegg pathways identifier")

  public String
  getKeGGId() {
    return keGGId;
  }

  public void setKeGGId(String keGGId) { this.keGGId = keGGId; }

  public PathwayInfoItem keGGUrl(String keGGUrl) {
    this.keGGUrl = keGGUrl;
    return this;
  }

  /**
   * The Url to retrieve the map from KEGG
   * @return keGGUrl
   */
  @ApiModelProperty(
      example =
          "https://www.kegg.jp/kegg-bin/show_pathway?org_name=hsa&mapno=00620&mapscale=&show_description=show",
      value = "The Url to retrieve the map from KEGG")

  public String
  getKeGGUrl() {
    return keGGUrl;
  }

  public void setKeGGUrl(String keGGUrl) { this.keGGUrl = keGGUrl; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathwayInfoItem pathwayInfoItem = (PathwayInfoItem)o;
    return Objects.equals(this.name, pathwayInfoItem.name) &&
        Objects.equals(this.keGGId, pathwayInfoItem.keGGId) &&
        Objects.equals(this.keGGUrl, pathwayInfoItem.keGGUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, keGGId, keGGUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PathwayInfoItem {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    keGGId: ").append(toIndentedString(keGGId)).append("\n");
    sb.append("    keGGUrl: ").append(toIndentedString(keGGUrl)).append("\n");
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
