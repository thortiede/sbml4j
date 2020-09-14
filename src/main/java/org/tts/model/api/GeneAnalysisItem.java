package org.tts.model.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.tts.model.api.PathwayInfoItem;
import org.tts.model.api.RelationInfoItem;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * GeneAnalysisItem
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-28T10:58:57.976Z[GMT]")
public class GeneAnalysisItem   {
  @JsonProperty("gene")
  private String gene = null;

  @JsonProperty("pathways")
  @Valid
  private List<PathwayInfoItem> pathways = null;

  @JsonProperty("relations")
  @Valid
  private List<RelationInfoItem> relations = null;

  public GeneAnalysisItem gene(String gene) {
    this.gene = gene;
    return this;
  }

  /**
   * The gene name this item is for
   * @return gene
  **/
  @ApiModelProperty(example = "BRAF", value = "The gene name this item is for")
  
    public String getGene() {
    return gene;
  }

  public void setGene(String gene) {
    this.gene = gene;
  }

  public GeneAnalysisItem pathways(List<PathwayInfoItem> pathways) {
    this.pathways = pathways;
    return this;
  }

  public GeneAnalysisItem addPathwaysItem(PathwayInfoItem pathwaysItem) {
    if (this.pathways == null) {
      this.pathways = new ArrayList<>();
    }
    this.pathways.add(pathwaysItem);
    return this;
  }

  /**
   * The pathways this gene is found in
   * @return pathways
  **/
  @ApiModelProperty(value = "The pathways this gene is found in")
      @Valid
    public List<PathwayInfoItem> getPathways() {
    return pathways;
  }

  public void setPathways(List<PathwayInfoItem> pathways) {
    this.pathways = pathways;
  }

  public GeneAnalysisItem relations(List<RelationInfoItem> relations) {
    this.relations = relations;
    return this;
  }

  public GeneAnalysisItem addRelationsItem(RelationInfoItem relationsItem) {
    if (this.relations == null) {
      this.relations = new ArrayList<>();
    }
    this.relations.add(relationsItem);
    return this;
  }

  /**
   * The relations this gene is part of
   * @return relations
  **/
  @ApiModelProperty(value = "The relations this gene is part of")
      @Valid
    public List<RelationInfoItem> getRelations() {
    return relations;
  }

  public void setRelations(List<RelationInfoItem> relations) {
    this.relations = relations;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeneAnalysisItem geneAnalysisItem = (GeneAnalysisItem) o;
    return Objects.equals(this.gene, geneAnalysisItem.gene) &&
        Objects.equals(this.pathways, geneAnalysisItem.pathways) &&
        Objects.equals(this.relations, geneAnalysisItem.relations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gene, pathways, relations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GeneAnalysisItem {\n");
    
    sb.append("    gene: ").append(toIndentedString(gene)).append("\n");
    sb.append("    pathways: ").append(toIndentedString(pathways)).append("\n");
    sb.append("    relations: ").append(toIndentedString(relations)).append("\n");
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
