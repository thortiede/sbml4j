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
package org.sbml4j.model.api.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.sbml4j.model.api.ApiRequestItem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * ActivityItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
@JsonInclude(Include.NON_NULL)
public class ActivityItem extends ProvenanceApiItem {
  @JsonProperty("name") private String name;

  @JsonProperty("type") private String type;

  @JsonProperty("operation") private String operation;

  @JsonProperty("endpoint") private String endpoint;

  @JsonProperty("params") @Valid private List<ActivityItemParams> params = null;

  @JsonProperty("body")
  private ApiRequestItem body = null;


  public ActivityItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the activity
   * @return name
   */
  @ApiModelProperty(example = "Create_Neighborhood_Network",
                    value = "The name of the activity")

  public String
  getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public ActivityItem type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of the activity
   * @return type
   */
  @ApiModelProperty(example = "createContext",
                    value = "The type of the activity")

  public String
  getType() {
    return type;
  }

  public void setType(String type) { this.type = type; }

  public ActivityItem operation(String operation) {
    this.operation = operation;
    return this;
  }

  /**
   * The type of REST operation (one of POST, GET, DELETE, PUT)
   * @return operation
   */
  @ApiModelProperty(
      example = "POST",
      value = "The type of REST operation (one of POST, GET, DELETE, PUT)")

  public String
  getOperation() {
    return operation;
  }

  public void setOperation(String operation) { this.operation = operation; }

  public ActivityItem endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * The endpoint this activity was created by
   * @return endpoint
   */
  @ApiModelProperty(
      example = "network/d25f4b9-8dd5-4bc3-9d04-9af418302244/context",
      value = "The endpoint this activity was created by")

  public String
  getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

  public ActivityItem params(List<ActivityItemParams> params) {
    this.params = params;
    return this;
  }

  public ActivityItem addParamsItem(ActivityItemParams paramsItem) {
    if (this.params == null) {
      this.params = new ArrayList<>();
    }
    this.params.add(paramsItem);
    return this;
  }

  /**
   * Get params
   * @return params
   */
  @ApiModelProperty(value = "")

  @Valid

  public List<ActivityItemParams> getParams() {
    return params;
  }

  public void setParams(List<ActivityItemParams> params) {
    this.params = params;
  }

  public ActivityItem body(
		  ApiRequestItem body) {
    this.body = body;
    return this;
  }

  /**
   * Get body
   * @return body
   */
  @ApiModelProperty(value = "")

  @Valid

  public ApiRequestItem getBody() {
    return body;
  }

  public void
  setBody(ApiRequestItem body) {
    this.body = body;
  }

  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActivityItem activityItem = (ActivityItem)o;
    return Objects.equals(this.name, activityItem.name) &&
        Objects.equals(this.type, activityItem.type) &&
        Objects.equals(this.operation, activityItem.operation) &&
        Objects.equals(this.endpoint, activityItem.endpoint) &&
        Objects.equals(this.params, activityItem.params) &&
        Objects.equals(this.body, activityItem.body);
  }


  @Override
  public int hashCode() {
    return Objects.hash(name, type, operation, endpoint, params, body
                        );
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActivityItem {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    operation: ")
        .append(toIndentedString(operation))
        .append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
    sb.append("    body: ").append(toIndentedString(body)).append("\n");
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
