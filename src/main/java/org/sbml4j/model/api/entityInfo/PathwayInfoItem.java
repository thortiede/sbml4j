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
 * PathwayInfoItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
public class PathwayInfoItem {
  @JsonProperty("name") private String name;

  @JsonProperty("SourceId") private String sourceId;

  @JsonProperty("SourceUrl") private String sourceUrl;

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
    this.sourceId = keGGId;
    return this;
  }

  /**
   * The Kegg pathways identifier
   * @return sourceId
   */
  @ApiModelProperty(example = "path_hsa04066",
                    value = "The Kegg pathways identifier")

  public String
  getKeGGId() {
    return sourceId;
  }

  public void setKeGGId(String keGGId) { this.sourceId = keGGId; }

  public PathwayInfoItem keGGUrl(String keGGUrl) {
    this.sourceUrl = keGGUrl;
    return this;
  }

  /**
   * The Url to retrieve the map from KEGG
   * @return sourceUrl
   */
  @ApiModelProperty(
      example =
          "https://www.kegg.jp/kegg-bin/show_pathway?org_name=hsa&mapno=00620&mapscale=&show_description=show",
      value = "The Url to retrieve the map from KEGG")

  public String
  getKeGGUrl() {
    return sourceUrl;
  }

  public void setKeGGUrl(String keGGUrl) { this.sourceUrl = keGGUrl; }

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
        Objects.equals(this.sourceId, pathwayInfoItem.sourceId) &&
        Objects.equals(this.sourceUrl, pathwayInfoItem.sourceUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, sourceId, sourceUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PathwayInfoItem {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    sourceId: ").append(toIndentedString(sourceId)).append("\n");
    sb.append("    sourceUrl: ").append(toIndentedString(sourceUrl)).append("\n");
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
