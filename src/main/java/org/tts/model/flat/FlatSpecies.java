package org.tts.model.flat;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.tts.model.SBMLCompartmentalizedSBaseEntity;

public class FlatSpecies extends SBMLCompartmentalizedSBaseEntity {

	 @Relationship(type = "dissociation")
	 List<FlatSpecies> dissociationSpeciesList; // SBO:0000180
	 
	 @Relationship(type = "dephosphorylation")
	 List<FlatSpecies> dephosphorylationSpeciesList; // SBO:0000330
	 
	 @Relationship(type = "uncertain process")
	 List<FlatSpecies> uncertainProcessSpeciesList; // SBO:0000396
	 
	 @Relationship(type = "non-covalent binding")
	 List<FlatSpecies> nonCovalentBindingSpeciesList; // SBO:0000177
	 
	 @Relationship(type = "stimulation")
	 List<FlatSpecies> stimulationSpeciesList; // SBO:0000170
	 
	 @Relationship(type = "glycosylation")
	 List<FlatSpecies> glycosylationSpeciesList; // SBO:0000217
	 
	 @Relationship(type = "phosphorylation")
	 List<FlatSpecies> phosphorylationSpeciesList; // SBO:0000216
	 
	 @Relationship(type = "inhibition")
	 List<FlatSpecies> inhibitionSpeciesList; // SBO:0000169
	 
	 @Relationship(type = "ubiquitination")
	 List<FlatSpecies> ubiquitinationSpeciesList; // SBO:0000224
	 
	 @Relationship(type = "methylation")
	 List<FlatSpecies> methylationSpeciesList; // SBO:0000214
	 
	 @Relationship(type = "molecular interaction")
	 List<FlatSpecies> molecularInteractionSpeciesList; // SBO:0000344
	 
	 @Relationship(type = "control")
	 List<FlatSpecies> controlSpeciesList; // SBO:0000168
	 
	 @Relationship(type = "unknownFromSource")
	 List<FlatSpecies> unknownFromSourceSpeciesList; // no SBO, eg. hsa05133 qual_K
	 
	// add one function like
	// addRelationship(sbo-term, flatspecies)
	//     switch case depending on sbo term, add to that list
	 // 	don't forget to initialise a list if nothing is in it yet.
}
