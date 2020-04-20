package org.tts.model.common;

public class GraphEnum {
	
	/**
	 * COPY,<br>
	 * ANNOTATE,<br>
	 * FILTER,<br>
	 * CONTEXT,<br>
	 * MULTIGENESUBNET,
	 */
	public enum MappingStep {
		COPY,
		ANNOTATE,
		FILTER,
		CONTEXT,
		MULTIGENESUBNET,
	}
	
	public enum NetworkMappingType {
		PPI,
		REGULATORY,
		METABOLIC,
		SIGNALLING,
		PATHWAYMAPPING
	}
	
	/**
	 * DATABASE<br>
	 * FILE<br>
	 * PATHWAY<br>
	 * KNOWLEDGEGRAPH (Deprecated)<br>
	 * MAPPING<br>
	 */
	public enum WarehouseGraphNodeType {
		DATABASE,
		FILE,
		PATHWAY,
		//KNOWLEDGEGRAPH, // instead it is a Pathway of a collection of Pathways and itself a Pathway
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
	 * graphml<br>
	 * sif
	 */
	public enum OutputType {
		graphml,
		sif
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
		runAlgorithm,
		createKnowledgeGraph,
		addMyDrugNodes
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
		UNKNOWNINSOURCE("unknown"),<br>
		TARGETS("targets"),<br>
		PRODUCT("product"),<br>
		REACTANT("reactant"),<br>
		CATALYST("catalyst");
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
		CONTROL("SBO:0000168"),
		UNKNOWNFROMSOURCE("unknownFromSource"),
		TARGETS("targets"),
		PRODUCT("product"),
		REACTANT("reactant"),
		CATALYST("catalyst");
		
		
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
	 * FOR<br>
	 * DERIVEDFROM
	 */
	public enum WarehouseGraphEdgeType {
		CONTAINS,
		FOR,
		DERIVEDFROM
	}
	
	/**
	 * 	KEGGGENES("kegg.genes"),<br>
		KEGGCOMPOUND("kegg.compound"),<br>
		KEGGDRUG("kegg.drug"),<br>
		KEGGREACTION("kegg.reaction");
		
	 */
	public enum ExternalResourceType {
		KEGGGENES("kegg.genes"),
		KEGGCOMPOUND("kegg.compound"),
		KEGGDRUG("kegg.drug"),
		KEGGREACTION("kegg.reaction");
		
		private final String externalResourceType;
		
		private ExternalResourceType(String type) {
			this.externalResourceType = type;
		}
		
		public String getExternalResourceType() {
			return this.externalResourceType;
		}
	}
	
	
	public enum IDSystem{
		KEGG,
		OTHER
	}
	
}
