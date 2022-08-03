package org.sbml4j.model.api.provenance;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

/**
 * AgentItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2022-04-02T23:09:46.842042+02:00[Europe/Berlin]")
@JsonInclude(Include.NON_NULL)
public class AgentItem {
  @JsonProperty("name") private String name;

  @JsonProperty("type") private String type;

  public AgentItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the agent
   * @return name
   */
  @ApiModelProperty(example = "sbml4j", value = "The name of the agent")

  public String getName() {
    return name;
  }

  public void setName(String name) { this.name = name; }

  public AgentItem type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of the agent
   * @return type
   */
  @ApiModelProperty(example = "User", value = "The type of the agent")

  public String getType() {
    return type;
  }

  public void setType(String type) { this.type = type; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AgentItem agentItem = (AgentItem)o;
    return Objects.equals(this.name, agentItem.name) &&
        Objects.equals(this.type, agentItem.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AgentItem {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
