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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Brief summary of a file. Includes the name of the file, the md5 sum and the
 * type of file.
 */
@ApiModel(
    description =
        "Brief summary of a file. Includes the name of the file, the md5 sum and the type of file. ")
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
public class FileInventoryItem extends ApiResponseItem {
  @JsonProperty("filename") private String filename;

  @JsonProperty("md5sum") private String md5sum;

  @JsonProperty("fileNodeType") private String fileNodeType;

  public FileInventoryItem filename(String filename) {
    this.filename = filename;
    return this;
  }

  /**
   * The name of the file
   * @return filename
   */
  @ApiModelProperty(example = "hsa05225.sbml.xml",
                    value = "The name of the file")

  public String
  getFilename() {
    return filename;
  }

  public void setFilename(String filename) { this.filename = filename; }

  public FileInventoryItem md5sum(String md5sum) {
    this.md5sum = md5sum;
    return this;
  }

  /**
   * The md5 sum of the file
   * @return md5sum
   */
  @ApiModelProperty(example = "3084b45d53e1085e8b9c3dee7f53bc49",
                    value = "The md5 sum of the file")

  public String
  getMd5sum() {
    return md5sum;
  }

  public void setMd5sum(String md5sum) { this.md5sum = md5sum; }

  public FileInventoryItem fileNodeType(String fileNodeType) {
    this.fileNodeType = fileNodeType;
    return this;
  }

  /**
   * The type of file
   * @return fileNodeType
   */
  @ApiModelProperty(example = "SBML", value = "The type of file")

  public String getFileNodeType() {
    return fileNodeType;
  }

  public void setFileNodeType(String fileNodeType) {
    this.fileNodeType = fileNodeType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FileInventoryItem fileInventoryItem = (FileInventoryItem)o;
    return Objects.equals(this.filename, fileInventoryItem.filename) &&
        Objects.equals(this.md5sum, fileInventoryItem.md5sum) &&
        Objects.equals(this.fileNodeType, fileInventoryItem.fileNodeType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filename, md5sum, fileNodeType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FileInventoryItem {\n");

    sb.append("    filename: ").append(toIndentedString(filename)).append("\n");
    sb.append("    md5sum: ").append(toIndentedString(md5sum)).append("\n");
    sb.append("    fileNodeType: ")
        .append(toIndentedString(fileNodeType))
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
