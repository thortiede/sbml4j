package org.tts.model.api.Input;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;


/**
 * Drivergenes
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-03T14:37:11.702Z[GMT]")
public class Drivergenes   {
	
  @JsonProperty("baseNetworkUUID")
  private String baseNetworkUUID = null;

  @JsonProperty("genes")
  @Valid
  private List<String> genes = null;

  public Drivergenes baseNetworkUUID(String baseNetworkUUID) {
    this.baseNetworkUUID = baseNetworkUUID;
    return this;
  }

  /**
   * Get baseNetworkUUID
   * @return baseNetworkUUID
  **/
  @Valid
  public String getBaseNetworkUUID() {
    return baseNetworkUUID;
  }

  public void setBaseNetworkUUID(String baseNetworkUUID) {
    this.baseNetworkUUID = baseNetworkUUID;
  }

  public Drivergenes genes(List<String> genes) {
    this.genes = genes;
    return this;
  }

  public Drivergenes addGenesItem(String genesItem) {
    if (this.genes == null) {
      this.genes = new ArrayList<String>();
    }
    this.genes.add(genesItem);
    return this;
  }

  /**
   * Get genes
   * @return genes
  **/  
    public List<String> getGenes() {
    return genes;
  }

  public void setGenes(List<String> genes) {
    this.genes = genes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Drivergenes drivergenes = (Drivergenes) o;
    return Objects.equals(this.baseNetworkUUID, drivergenes.baseNetworkUUID) &&
        Objects.equals(this.genes, drivergenes.genes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseNetworkUUID, genes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Drivergenes {\n");
    
    sb.append("    baseNetworkUUID: ").append(toIndentedString(baseNetworkUUID)).append("\n");
    sb.append("    genes: ").append(toIndentedString(genes)).append("\n");
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
  
  public String toSingleLineString () {
	  StringBuilder sb = new StringBuilder();
	  sb.append("drivergenes_for_");
	  sb.append(baseNetworkUUID);
	  sb.append("_with_genes");
	  for(String gene : genes) {
		  sb.append('_');
		  sb.append(gene);
	  }
	  return sb.toString();
  }
}
