package org.tts.model.common;

public class GraphEnum {
	
	
	public enum NetworkMappingType {
		PPI,
		REGULATORY,
		METABOLIC,
		SIGNALLING
	}
	
	/**
	 * DATABASE<br>
	 * FILE<br>
	 * PATHWAY<br>
	 * KNOWLEDGEGRAPH<br>
	 * MAPPING<br>
	 */
	public enum WarehouseGraphNodeType {
		DATABASE,
		FILE,
		PATHWAY,
		KNOWLEDGEGRAPH,
		MAPPING,
		ORGANISM
	}
	/**
	 * SBML<br>
	 * GRAPHML<br>
	 * SQL<br>
	 */
	public enum FileNodeType {
		SBML,
		GRAPHML,
		SQL
	}
	/**
	 *	used, 				// (Activity to Entity)<br>
	 *	wasGeneratedBy, 	// (Entity to Activity)<br>
		wasAssociatedWith, 	// (Activity to Agent)<br>
		wasAttributedTo, 	// (Entity to Agent)<br>
		wasDerivedFrom, 	// (Entity to Entity)<br>
		hadMember			// (Entity (Collection) to Entity)<br>
	 *
	 */
	public enum ProvenanceGraphEdgeType {
		used, 				// (Activity to Entity)
		wasGeneratedBy, 	// (Entity to Activity)
		wasAssociatedWith, 	// (Activity to Agent)
		wasAttributedTo, 	// (Entity to Agent)
		wasDerivedFrom, 	// (Entity to Entity)
		hadMember			// (Entity (Collection) to Entity)
	}
	
	/**
	 * Entity<br>
	 * Activity<br>
	 * Agent<br>
	 */
	public enum ProvenanceGraphNodeType {
		PROV_ENTITY, PROV_ACTIVITY, PROV_AGENT
	}
	
	/**
	 * 	persistFile,<br>
		createMapping,<br>
		mapAnnotations,<br>
		runAlgorithm<br>
	 */
	public enum ProvenanceGraphActivityType {
		persistFile,
		createMapping,
		mapAnnotations,
		runAlgorithm
	}
	
	/**
	 * 	User,<br>
		Organisation,<br>
		SoftwareAgent<br>
	 */
	public enum ProvenanceGraphAgentType {
		User,
		Organisation,
		SoftwareAgent
	}
	/**
	 * Collection<br>
	 * Member<br>
	 */
	public enum ProvenanceGraphEntityType {
		Collection,
		Member // of a collection
	}
	
	/**
	 *	DISSOCIATION("SBO:0000180"),<br>
		PHOSPHORYLATION("SBO:0000216"),<br>
		DEPHOSPHORYLATION("SBO:0000330"),<br>
		UNCERTAINPROCESS("SBO:0000396"),<br>
		NONCOVALENTBINDING("SBO:0000177"),<br>
		STIMULATION("SBO:0000170"),<br>
		INHIBITION("SBO:0000169"),<br>
		GLYCOSYLATION("SBO:0000217"),<br>
		UBIQUITINATION("SBO:0000224"),<br>
		METHYLATION("SBO:0000214"),<br>
		MOLECULARINTERACTION("SBO:0000344"),<br>
		CONTROL("SBO:0000168");<br>
	 */
	
	public enum RelationTypes {
		DISSOCIATION("SBO:0000180"),
		PHOSPHORYLATION("SBO:0000216"),
		DEPHOSPHORYLATION("SBO:0000330"),
		UNCERTAINPROCESS("SBO:0000396"),
		NONCOVALENTBINDING("SBO:0000177"),
		STIMULATION("SBO:0000170"),
		INHIBITION("SBO:0000169"),
		GLYCOSYLATION("SBO:0000217"),
		UBIQUITINATION("SBO:0000224"),
		METHYLATION("SBO:0000214"),
		MOLECULARINTERACTION("SBO:0000344"),
		CONTROL("SBO:0000168");
		
		private final String relType;
		
		private RelationTypes(String sboString) {
			this.relType = sboString;
		}
		
		public String getRelType() {
			return this.relType;
		}
		
	}
	/**
	 * CONTAINS<br>
	 * FOR
	 */
	public enum WarehouseGraphEdgeType {
		CONTAINS,
		FOR
	}
	
	public enum ExternalResourceType {
		KEGGGENES("kegg.genes");
		
		private final String externalResourceType;
		
		private ExternalResourceType(String type) {
			this.externalResourceType = type;
		}
		
		public String getExternalResourceType() {
			return this.externalResourceType;
		}
	}
	
}
