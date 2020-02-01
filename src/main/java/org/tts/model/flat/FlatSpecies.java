package org.tts.model.flat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.common.ContentGraphNode;
import org.tts.model.common.GraphEnum.RelationTypes;
import org.tts.model.flat.relationship.*;


public class FlatSpecies extends ContentGraphNode {

	private String simpleModelEntityUUID; // maybe use ProvenanceEdge wasDerivedFrom to link back to the Entity in the simpleModel?
	
	private String symbol;
	
	private String sboTerm;
	
	
	 @Relationship(type = "DISSOCIATION")
	 List<FlatEdge> dissociationSpeciesList; // SBO:0000180
	 
	 @Relationship(type = "DEPHOSPHORYLATION")
	 List<FlatEdge> dephosphorylationSpeciesList; // SBO:0000330
	 
	 @Relationship(type = "UNCERTAINPROCESS")
	 List<FlatEdge> uncertainProcessSpeciesList; // SBO:0000396
	 
	 @Relationship(type = "NONCOVALENTBINDING")
	 List<FlatEdge> nonCovalentBindingSpeciesList; // SBO:0000177
	 
	 @Relationship(type = "STIMULATION")
	 List<FlatEdge> stimulationSpeciesList; // SBO:0000170
	 
	 @Relationship(type = "GLYCOSYLATION")
	 List<FlatEdge> glycosylationSpeciesList; // SBO:0000217
	 
	 @Relationship(type = "PHOSPHORYLATION")
	 List<FlatEdge> phosphorylationSpeciesList; // SBO:0000216
	 
	 @Relationship(type = "INHIBITION")
	 List<FlatEdge> inhibitionSpeciesList; // SBO:0000169
	 
	 @Relationship(type = "UBIQUITINATION")
	 List<FlatEdge> ubiquitinationSpeciesList; // SBO:0000224
	 
	 @Relationship(type = "METHYLATION")
	 List<FlatEdge> methylationSpeciesList; // SBO:0000214
	 
	 @Relationship(type = "MOLECULARINTERACTION")
	 List<FlatEdge> molecularInteractionSpeciesList; // SBO:0000344
	 
	 @Relationship(type = "CONTROL")
	 List<FlatEdge> controlSpeciesList; // SBO:0000168
	 
	 @Relationship(type = "UNKNOWNFROMSOURCE")
	 List<FlatEdge> unknownFromSourceSpeciesList; // no SBO, eg. hsa05133 qual_K
	 
	@Relationship(type = "TARGETS")
	List<FlatEdge> targetsSpeciesList; // no SBO, this comes from MyDrug / Drugbank
	
	@Relationship(type = "PRODUCTOF")
	List<FlatEdge> productOfSpeciesList; // Metabolic Relation denoting this Species is product of the reaction linked here
	
	@Relationship(type = "REACTANTOF")
	List<FlatEdge> reactantOfSpeciesList; // Metabolic Relation denoting this Species is reactant of the reaction linked here
	
	@Relationship(type = "CATALYSES")
	List<FlatEdge> catalysesSpeciesList; // Metabolic Relation denoting this Species catalyses the reaction linked here
	
	
	 public String getSimpleModelEntityUUID() {
		return simpleModelEntityUUID;
	}

	public void setSimpleModelEntityUUID(String simpleModelEntityUUID) {
		this.simpleModelEntityUUID = simpleModelEntityUUID;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSboTerm() {
		return sboTerm;
	}

	public void setSboTerm(String sboTerm) {
		this.sboTerm = sboTerm;
	}


	public Map<String, List<FlatEdge>> getAllRelatedSpecies(){
		Map<String, List<FlatEdge>> allRelatedSpecies = new HashMap<>();
		for(RelationTypes type : RelationTypes.values()) {
			switch(type) {
			case INHIBITION:
				if(this.inhibitionSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), inhibitionSpeciesList);
				}
				break;
			case CONTROL:
				if(this.controlSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), controlSpeciesList);
				}
				break;
			case DEPHOSPHORYLATION:
				if(this.dephosphorylationSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), dephosphorylationSpeciesList);
				}
				break;
			case DISSOCIATION:
				if(this.dissociationSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), dissociationSpeciesList);
				}
				break;
			case GLYCOSYLATION:
				if(this.glycosylationSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), glycosylationSpeciesList);
				}
				break;
			case METHYLATION:
				if(this.methylationSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), methylationSpeciesList);
				}
				break;
			case MOLECULARINTERACTION:
				if(this.molecularInteractionSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), molecularInteractionSpeciesList);
				}
				break;
			case NONCOVALENTBINDING:
				if(this.nonCovalentBindingSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), nonCovalentBindingSpeciesList);
				}
				break;
			case PHOSPHORYLATION:
				if(this.phosphorylationSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), phosphorylationSpeciesList);
				}
				break;
			case STIMULATION:
				if(this.stimulationSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), stimulationSpeciesList);
				}
				break;
			case UBIQUITINATION:
				if(this.ubiquitinationSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), ubiquitinationSpeciesList);
				}
				break;
			case UNCERTAINPROCESS:
				if(this.uncertainProcessSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), uncertainProcessSpeciesList);
				}
				break;
			case UNKNOWNFROMSOURCE:
				if(this.unknownFromSourceSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), unknownFromSourceSpeciesList);
				}
				break;
			case TARGETS:
				if(this.targetsSpeciesList != null) {
					allRelatedSpecies.put(type.getRelType(), targetsSpeciesList);
				}
			default:
				if(this.unknownFromSourceSpeciesList != null) {
					allRelatedSpecies.put("unknownFromSource", unknownFromSourceSpeciesList);
				}
				break;
			}
		}
		return allRelatedSpecies;
	}
	
	public FlatSpecies addRelatedSpecies(Map<String, List<FlatSpecies>> relatedSpeciesMap) {
		relatedSpeciesMap.forEach((relationType, relatedSpeciesList) -> {
			for(FlatSpecies other : relatedSpeciesList) {
				this.addRelatedSpecies(other, relationType);
			}
		});
		return this;
	}
	
	public FlatSpecies addRelatedSpecies(FlatSpecies other, String sboTermString) {
		FlatEdge otherEdge;
		 switch (sboTermString) {
		case "SBO:0000180":
			if(dissociationSpeciesList == null) {
				dissociationSpeciesList = new ArrayList<>();
			}
			//DissociationFlatEdge 
			otherEdge = new DissociationFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			dissociationSpeciesList.add(otherEdge);
			break;
		case "SBO:0000330":
			if(dephosphorylationSpeciesList == null) {
				dephosphorylationSpeciesList = new ArrayList<>();
			}
			//DephosphorylationFlatEdge 
			otherEdge = new DephosphorylationFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			dephosphorylationSpeciesList.add(otherEdge);
			break;
		case "SBO:0000396":
			if(uncertainProcessSpeciesList == null) {
				uncertainProcessSpeciesList = new ArrayList<>();
			}
			otherEdge = new UncertainProcessFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			uncertainProcessSpeciesList.add(otherEdge);
			break;
		case "SBO:0000177":
			if(nonCovalentBindingSpeciesList == null) {
				nonCovalentBindingSpeciesList = new ArrayList<>();
			}
			otherEdge = new NonCovalentBindingFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			nonCovalentBindingSpeciesList.add(otherEdge);
			break;
		case "SBO:0000170":
			if(stimulationSpeciesList == null) {
				stimulationSpeciesList = new ArrayList<>();
			}
			otherEdge = new StimulationFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			stimulationSpeciesList.add(otherEdge);
			break;
		case "SBO:0000217":
			if(glycosylationSpeciesList == null) {
				glycosylationSpeciesList = new ArrayList<>();
			}
			otherEdge = new GlycosylationFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			glycosylationSpeciesList.add(otherEdge);
			break;
		case "SBO:0000216":
			if(phosphorylationSpeciesList == null) {
				phosphorylationSpeciesList = new ArrayList<>();
			}
			otherEdge = new PhosphorylationFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			phosphorylationSpeciesList.add(otherEdge);
			break;
		case "SBO:0000169":
			if(inhibitionSpeciesList == null) {
				inhibitionSpeciesList = new ArrayList<>();
			}
			otherEdge = new InhibitionFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			inhibitionSpeciesList.add(otherEdge);
			break;
		case "SBO:0000224":
			if(ubiquitinationSpeciesList == null) {
				ubiquitinationSpeciesList = new ArrayList<>();
			}
			otherEdge = new UbiquitinationFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			ubiquitinationSpeciesList.add(otherEdge);
			break;
		case "SBO:0000214":
			if(methylationSpeciesList == null) {
				methylationSpeciesList = new ArrayList<>();
			}
			otherEdge = new MethylationFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			methylationSpeciesList.add(otherEdge);
			break;
		case "SBO:0000344":
			if(molecularInteractionSpeciesList == null) {
				molecularInteractionSpeciesList = new ArrayList<>();
			}
			otherEdge = new MolecularInteractionFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			molecularInteractionSpeciesList.add(otherEdge);
			break;
		case "SBO:0000168":
			if(controlSpeciesList == null) {
				controlSpeciesList = new ArrayList<>();
			}
			otherEdge = new ControlFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			controlSpeciesList.add(otherEdge);
			break;
		case "targets":
			if(targetsSpeciesList == null) {
				targetsSpeciesList = new ArrayList<>();
			}
			otherEdge = new TargetsFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			targetsSpeciesList.add(otherEdge);
			this.addLabel("Drug");
			break;
		case "unknownFromSource":
			if(unknownFromSourceSpeciesList == null) {
				unknownFromSourceSpeciesList = new ArrayList<>();
			}
			otherEdge = new UnknownFromSourceFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			unknownFromSourceSpeciesList.add(otherEdge);
			break;
		case "reactant":
			if (reactantOfSpeciesList == null) {
				reactantOfSpeciesList = new ArrayList<>();
			}
			otherEdge = new ReactantFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			reactantOfSpeciesList.add(otherEdge);
			break;
		case "product":
			if (productOfSpeciesList == null) {
				productOfSpeciesList = new ArrayList<>();
			}
			otherEdge = new ProductFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			productOfSpeciesList.add(otherEdge);
			break;
		case "catalyst":
			if (catalysesSpeciesList == null) {
				catalysesSpeciesList = new ArrayList<>();
			}
			otherEdge = new CatalystFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			catalysesSpeciesList.add(otherEdge);
			break;
		default:
			if(unknownFromSourceSpeciesList == null) {
				unknownFromSourceSpeciesList = new ArrayList<>();
			}
			otherEdge = new UnknownFromSourceFlatEdge();
			otherEdge.setInputFlatSpecies(this);
			otherEdge.setOutputFlatSpecies(other);
			unknownFromSourceSpeciesList.add(otherEdge);
			break;
		}
		return this; 
	 }

	public List<FlatEdge> getDissociationSpeciesList() {
		return dissociationSpeciesList;
	}

	public void setDissociationSpeciesList(List<FlatEdge> dissociationSpeciesList) {
		this.dissociationSpeciesList = dissociationSpeciesList;
	}

	public List<FlatEdge> getDephosphorylationSpeciesList() {
		return dephosphorylationSpeciesList;
	}

	public void setDephosphorylationSpeciesList(List<FlatEdge> dephosphorylationSpeciesList) {
		this.dephosphorylationSpeciesList = dephosphorylationSpeciesList;
	}

	public List<FlatEdge> getUncertainProcessSpeciesList() {
		return uncertainProcessSpeciesList;
	}

	public void setUncertainProcessSpeciesList(List<FlatEdge> uncertainProcessSpeciesList) {
		this.uncertainProcessSpeciesList = uncertainProcessSpeciesList;
	}

	public List<FlatEdge> getNonCovalentBindingSpeciesList() {
		return nonCovalentBindingSpeciesList;
	}

	public void setNonCovalentBindingSpeciesList(List<FlatEdge> nonCovalentBindingSpeciesList) {
		this.nonCovalentBindingSpeciesList = nonCovalentBindingSpeciesList;
	}

	public List<FlatEdge> getStimulationSpeciesList() {
		return stimulationSpeciesList;
	}

	public void setStimulationSpeciesList(List<FlatEdge> stimulationSpeciesList) {
		this.stimulationSpeciesList = stimulationSpeciesList;
	}

	public List<FlatEdge> getGlycosylationSpeciesList() {
		return glycosylationSpeciesList;
	}

	public void setGlycosylationSpeciesList(List<FlatEdge> glycosylationSpeciesList) {
		this.glycosylationSpeciesList = glycosylationSpeciesList;
	}

	public List<FlatEdge> getPhosphorylationSpeciesList() {
		return phosphorylationSpeciesList;
	}

	public void setPhosphorylationSpeciesList(List<FlatEdge> phosphorylationSpeciesList) {
		this.phosphorylationSpeciesList = phosphorylationSpeciesList;
	}

	public List<FlatEdge> getInhibitionSpeciesList() {
		return inhibitionSpeciesList;
	}

	public void setInhibitionSpeciesList(List<FlatEdge> inhibitionSpeciesList) {
		this.inhibitionSpeciesList = inhibitionSpeciesList;
	}

	public List<FlatEdge> getUbiquitinationSpeciesList() {
		return ubiquitinationSpeciesList;
	}

	public void setUbiquitinationSpeciesList(List<FlatEdge> ubiquitinationSpeciesList) {
		this.ubiquitinationSpeciesList = ubiquitinationSpeciesList;
	}

	public List<FlatEdge> getMethylationSpeciesList() {
		return methylationSpeciesList;
	}

	public void setMethylationSpeciesList(List<FlatEdge> methylationSpeciesList) {
		this.methylationSpeciesList = methylationSpeciesList;
	}

	public List<FlatEdge> getMolecularInteractionSpeciesList() {
		return molecularInteractionSpeciesList;
	}

	public void setMolecularInteractionSpeciesList(List<FlatEdge> molecularInteractionSpeciesList) {
		this.molecularInteractionSpeciesList = molecularInteractionSpeciesList;
	}

	public List<FlatEdge> getControlSpeciesList() {
		return controlSpeciesList;
	}

	public void setControlSpeciesList(List<FlatEdge> controlSpeciesList) {
		this.controlSpeciesList = controlSpeciesList;
	}

	public List<FlatEdge> getUnknownFromSourceSpeciesList() {
		return unknownFromSourceSpeciesList;
	}

	public void setUnknownFromSourceSpeciesList(List<FlatEdge> unknownFromSourceSpeciesList) {
		this.unknownFromSourceSpeciesList = unknownFromSourceSpeciesList;
	}

	public List<FlatEdge> getTargetsSpeciesList() {
		return targetsSpeciesList;
	}

	public void setTargetsSpeciesList(List<FlatEdge> targetsSpeciesList) {
		this.targetsSpeciesList = targetsSpeciesList;
	}

	public List<FlatEdge> getProductOfSpeciesList() {
		return productOfSpeciesList;
	}

	public void setProductOfSpeciesList(List<FlatEdge> productOfSpeciesList) {
		this.productOfSpeciesList = productOfSpeciesList;
	}

	public List<FlatEdge> getReactantOfSpeciesList() {
		return reactantOfSpeciesList;
	}

	public void setReactantOfSpeciesList(List<FlatEdge> reactantOfSpeciesList) {
		this.reactantOfSpeciesList = reactantOfSpeciesList;
	}

	public List<FlatEdge> getCatalysesSpeciesList() {
		return catalysesSpeciesList;
	}

	public void setCatalysesSpeciesList(List<FlatEdge> catalysesSpeciesList) {
		this.catalysesSpeciesList = catalysesSpeciesList;
	}


	
	 
	 
}
