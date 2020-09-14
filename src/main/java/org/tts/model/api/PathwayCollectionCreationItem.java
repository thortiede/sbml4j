package org.tts.model.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * PathwayCollectionCreationItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class PathwayCollectionCreationItem   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("databaseUUID")
  private UUID databaseUUID = null;

  @JsonProperty("sourcePathwayUUIDs")
  @Valid
  private List<UUID> sourcePathwayUUIDs = null;

  public PathwayCollectionCreationItem name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the collection to be created
   * @return name
  **/
  @ApiModelProperty(example = "CollectionPathway", value = "The name of the collection to be created")
  
    public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PathwayCollectionCreationItem description(String description) {
    this.description = description;
    return this;
  }

  /**
   * The description for the collection created
   * @return description
  **/
  @ApiModelProperty(example = "This is a collection pathway", value = "The description for the collection created")
  
    public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public PathwayCollectionCreationItem databaseUUID(UUID databaseUUID) {
    this.databaseUUID = databaseUUID;
    return this;
  }

  /**
   * The UUID of the database those pathways are from
   * @return databaseUUID
  **/
  @ApiModelProperty(example = "0d25f4b9-8dd5-4bc3-9d04-9af418302244", value = "The UUID of the database those pathways are from")
  
    @Valid
    public UUID getDatabaseUUID() {
    return databaseUUID;
  }

  public void setDatabaseUUID(UUID databaseUUID) {
    this.databaseUUID = databaseUUID;
  }

  public PathwayCollectionCreationItem sourcePathwayUUIDs(List<UUID> sourcePathwayUUIDs) {
    this.sourcePathwayUUIDs = sourcePathwayUUIDs;
    return this;
  }

  public PathwayCollectionCreationItem addSourcePathwayUUIDsItem(UUID sourcePathwayUUIDsItem) {
    if (this.sourcePathwayUUIDs == null) {
      this.sourcePathwayUUIDs = new ArrayList<>();
    }
    this.sourcePathwayUUIDs.add(sourcePathwayUUIDsItem);
    return this;
  }

  /**
   * List of pathway UUIDs that are to be included in this collection
   * @return sourcePathwayUUIDs
  **/
  @ApiModelProperty(value = "List of pathway UUIDs that are to be included in this collection")
      @Valid
    public List<UUID> getSourcePathwayUUIDs() {
    return sourcePathwayUUIDs;
  }

  public void setSourcePathwayUUIDs(List<UUID> sourcePathwayUUIDs) {
    this.sourcePathwayUUIDs = sourcePathwayUUIDs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathwayCollectionCreationItem pathwayCollectionCreationItem = (PathwayCollectionCreationItem) o;
    return Objects.equals(this.name, pathwayCollectionCreationItem.name) &&
        Objects.equals(this.description, pathwayCollectionCreationItem.description) &&
        Objects.equals(this.databaseUUID, pathwayCollectionCreationItem.databaseUUID) &&
        Objects.equals(this.sourcePathwayUUIDs, pathwayCollectionCreationItem.sourcePathwayUUIDs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, databaseUUID, sourcePathwayUUIDs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PathwayCollectionCreationItem {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    databaseUUID: ").append(toIndentedString(databaseUUID)).append("\n");
    sb.append("    sourcePathwayUUIDs: ").append(toIndentedString(sourcePathwayUUIDs)).append("\n");
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
