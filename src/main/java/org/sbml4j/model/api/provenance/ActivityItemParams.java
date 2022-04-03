package org.sbml4j.model.api.provenance;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * ActivityItemParams
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
public class ActivityItemParams {
  @JsonProperty("parameter") private String parameter;

  @JsonProperty("value") private String value;

  public ActivityItemParams parameter(String parameter) {
    this.parameter = parameter;
    return this;
  }

  /**
   * The name of the api-parameter
   * @return parameter
   */
  @ApiModelProperty(example = "parentUUID",
                    value = "The name of the api-parameter")

  public String
  getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) { this.parameter = parameter; }

  public ActivityItemParams value(String value) {
    this.value = value;
    return this;
  }

  /**
   * The value of the api-parameter
   * @return value
   */
  @ApiModelProperty(example = "d25f4b9-8dd5-4bc3-9d04-9af418302244",
                    value = "The value of the api-parameter")

  public String
  getValue() {
    return value;
  }

  public void setValue(String value) { this.value = value; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActivityItemParams activityItemParams = (ActivityItemParams)o;
    return Objects.equals(this.parameter, activityItemParams.parameter) &&
        Objects.equals(this.value, activityItemParams.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameter, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActivityItemParams {\n");

    sb.append("    parameter: ")
        .append(toIndentedString(parameter))
        .append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
