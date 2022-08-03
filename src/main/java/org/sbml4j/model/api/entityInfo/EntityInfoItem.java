package org.sbml4j.model.api.entityInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

/**
 * EntityInfoItem
 */
@javax.annotation.
Generated(value = "org.openapitools.codegen.languages.SpringCodegen",
          date = "2021-02-09T17:24:41.444924+01:00[Europe/Berlin]")
@JsonInclude(Include.NON_NULL)
public class EntityInfoItem {
  @JsonProperty("gene") private String gene;

  @JsonProperty("secondary names")
  @Valid
  private List<String> secondaryNames = null;

  @JsonProperty("qualifier")
  @Valid
  private List<QualifierItem> qualifier = null;

  @JsonProperty("pathways")
  @Valid
  private List<PathwayInfoItem> pathways = null;

  @JsonProperty("relations")
  @Valid
  private List<RelationInfoItem> relations = null;

  @JsonProperty("reactions")
  @Valid
  private List<ReactionInfoItem> reactions = null;

  public EntityInfoItem gene(String gene) {
    this.gene = gene;
    return this;
  }

  /**
   * The gene name this item is for
   * @return gene
   */
  @ApiModelProperty(example = "BRAF", value = "The gene name this item is for")

  public String getGene() {
    return gene;
  }

  public void setGene(String gene) { this.gene = gene; }

  public EntityInfoItem secondaryNames(List<String> secondaryNames) {
    this.secondaryNames = secondaryNames;
    return this;
  }

  public EntityInfoItem addSecondaryNamesItem(String secondaryNamesItem) {
    if (this.secondaryNames == null) {
      this.secondaryNames = new ArrayList<>();
    }
    if (!this.secondaryNames.contains(secondaryNamesItem))
    	this.secondaryNames.add(secondaryNamesItem);
    return this;
  }

  /**
   * Get secondaryNames
   * @return secondaryNames
   */
  @ApiModelProperty(value = "")

  public List<String> getSecondaryNames() {
    return secondaryNames;
  }

  public void setSecondaryNames(List<String> secondaryNames) {
    this.secondaryNames = secondaryNames;
  }

  public EntityInfoItem qualifier(List<QualifierItem> qualifier) {
    this.qualifier = qualifier;
    return this;
  }

  public EntityInfoItem addQualifierItem(QualifierItem qualifierItem) {
    if (this.qualifier == null) {
      this.qualifier = new ArrayList<>();
    }
    this.qualifier.add(qualifierItem);
    return this;
  }

  /**
   * The qualifier of this gene
   * @return qualifier
   */
  @ApiModelProperty(value = "The qualifier of this gene")

  @Valid

  public List<QualifierItem> getQualifier() {
    return qualifier;
  }

  public void setQualifier(List<QualifierItem> qualifier) {
    this.qualifier = qualifier;
  }

  public EntityInfoItem pathways(List<PathwayInfoItem> pathways) {
    this.pathways = pathways;
    return this;
  }

  public EntityInfoItem addPathwaysItem(PathwayInfoItem pathwaysItem) {
    if (this.pathways == null) {
      this.pathways = new ArrayList<>();
    }
    if (!this.pathways.contains(pathwaysItem)) this.pathways.add(pathwaysItem);
    return this;
  }

  /**
   * The pathways this gene is found in
   * @return pathways
   */
  @ApiModelProperty(value = "The pathways this gene is found in")

  @Valid

  public List<PathwayInfoItem> getPathways() {
    return pathways;
  }

  public void setPathways(List<PathwayInfoItem> pathways) {
    this.pathways = pathways;
  }

  public EntityInfoItem relations(List<RelationInfoItem> relations) {
    this.relations = relations;
    return this;
  }

  public EntityInfoItem addRelationsItem(RelationInfoItem relationsItem) {
    if (this.relations == null) {
      this.relations = new ArrayList<>();
    }
    if(!this.relations.contains(relationsItem)) this.relations.add(relationsItem);
    return this;
  }

  /**
   * The relations this gene is part of
   * @return relations
   */
  @ApiModelProperty(value = "The relations this gene is part of")

  @Valid

  public List<RelationInfoItem> getRelations() {
    return relations;
  }

  public void setRelations(List<RelationInfoItem> relations) {
    this.relations = relations;
  }

  public EntityInfoItem reactions(List<ReactionInfoItem> reactions) {
    this.reactions = reactions;
    return this;
  }

  public EntityInfoItem addReactionsItem(ReactionInfoItem reactionsItem) {
    if (this.reactions == null) {
      this.reactions = new ArrayList<>();
    }
    this.reactions.add(reactionsItem);
    return this;
  }

  /**
   * The reactions this gene is part of
   * @return reactions
   */
  @ApiModelProperty(value = "The reactions this gene is part of")

  @Valid

  public List<ReactionInfoItem> getReactions() {
    return reactions;
  }

  public void setReactions(List<ReactionInfoItem> reactions) {
    this.reactions = reactions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EntityInfoItem geneAnalysisItem = (EntityInfoItem)o;
    return Objects.equals(this.gene, geneAnalysisItem.gene) &&
        Objects.equals(this.secondaryNames, geneAnalysisItem.secondaryNames) &&
        Objects.equals(this.qualifier, geneAnalysisItem.qualifier) &&
        Objects.equals(this.pathways, geneAnalysisItem.pathways) &&
        Objects.equals(this.relations, geneAnalysisItem.relations) &&
        Objects.equals(this.reactions, geneAnalysisItem.reactions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gene, secondaryNames, qualifier, pathways, relations,
                        reactions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EntityInfoItem {\n");

    sb.append("    gene: ").append(toIndentedString(gene)).append("\n");
    sb.append("    secondaryNames: ")
        .append(toIndentedString(secondaryNames))
        .append("\n");
    sb.append("    qualifier: ")
        .append(toIndentedString(qualifier))
        .append("\n");
    sb.append("    pathways: ").append(toIndentedString(pathways)).append("\n");
    sb.append("    relations: ")
        .append(toIndentedString(relations))
        .append("\n");
    sb.append("    reactions: ")
        .append(toIndentedString(reactions))
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
