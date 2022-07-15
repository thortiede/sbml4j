/**
 * --------------------------------------------------------------------------
 *                                 SBML4j
 * --------------------------------------------------------------------------
 * University of Tuebingen, 2020.
 * 
 * This code is part of the SBML4j software package and subject to the terms
 * and conditions defined by its license (MIT License). For license details
 * please refer to the LICENSE file included as part of this source code
 * package.
 * 
 * For a full list of authors, please refer to the file AUTHORS.
 */
package org.sbml4j.repository.warehouse;

import java.util.List;
import java.util.Set;

import org.sbml4j.model.queryResult.MetabolicPathwayReturnType;
import org.sbml4j.model.warehouse.PathwayNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PathwayNodeRepository extends Neo4jRepository<PathwayNode, Long> {

	@Query(value = "MATCH "
			+ "(p:PathwayNode)"
			+ "-[pr:PROV]-"
			+ "(pa:PROV_AGENT) "
			+ "WHERE pr.provenanceGraphEdgeType = \"wasAttributedTo\" "
			+ "AND pa.graphAgentName = $username "
			+ "RETURN p")
	Iterable<PathwayNode> findAllPathwaysAttributedToUser(String username);
	
	@Query(value = "MATCH "
			+ "(p:PathwayNode)"
			+ "-[pr:PROV]-"
			+ "(pa:PROV_AGENT) "
			+ "WHERE pr.provenanceGraphEdgeType = \"wasAttributedTo\" "
			+ "AND pa.graphAgentName = $username "
			+ "AND NOT EXISTS {"
			  + "(p)-[np:PROV]->(pc:PathwayCollectionNode) "
			   + "WHERE np.provenanceGraphEdgeType = \"wasDerivedFrom\" "
		    + "} "
			+ "RETURN p")
	Iterable<PathwayNode> findNonCollectionPathwaysAttributedToUser(String username);

	PathwayNode findByEntityUUID(String entityUUID);
	
	@Query("MATCH "
			+ "(p:PathwayNode)-[w:Warehouse]->(s:SBMLSpecies) "
			+ "WHERE p.entityUUID = $pathwayNodeEntityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN DISTINCT s.sBaseSboTerm")
	Iterable<String> getAllDistinctSpeciesSboTermsOfPathway(String pathwayNodeEntityUUID);

	@Query("MATCH "
			+ "(p:PathwayNode)-[w:Warehouse]->(t:SBMLSimpleTransition) "
			+ "WHERE p.entityUUID = $pathwayNodeEntityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN DISTINCT t.sBaseSboTerm")
	Iterable<String> getAllDistinctTransitionSboTermsOfPathway(String pathwayNodeEntityUUID);

	@Query("MATCH "
			+ "(g1:GraphBaseEntity)"
			+ "-[w1:Warehouse]->"
			+ "(s1:SBMLSpecies)"
			+ "<-[wc1:Warehouse]-"
			+ "(p:PathwayNode)"
			+ "-[wc2:Warehouse]->"
			+ "(s2:SBMLSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(g2:GraphBaseEntity), "
			+ "(pc:PathwayCollectionNode) "
			+ "WHERE g1.entityUUID = $entity1UUID "
			+ "AND g2.entityUUID = $entity2UUID "
			+ "AND w1.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND w2.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND wc1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND wc2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND NOT EXISTS {"
				+ "(p)-[np:PROV]->(pc:PathwayCollectionNode) "
				+ "WHERE np.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "} "
			+ "RETURN p")
	List<PathwayNode> findAllBySharedDerivedFromWarehouseEdge(String entity1UUID, String entity2UUID);
	
	/*@Query("MATCH "
			+ "(g1:GraphBaseEntity)"
			+ "-[w1:Warehouse]->"
			+ "(s1:SBMLSpecies)"
			+ "<-[wc1:Warehouse]-"
			+ "(p:PathwayNode)"
			+ "-[wc2:Warehouse]->"
			+ "(s2:SBMLSpecies)"
			+ "<-[w2:Warehouse]-"
			+ "(g2:GraphBaseEntity) "
			+ "WHERE g1.entityUUID = $entity1UUID "
			+ "AND g2.entityUUID = $entity2UUID "
			+ "AND w1.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND w2.warehouseGraphEdgeType = \"DERIVEDFROM\" "
			+ "AND wc1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND wc2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND NOT p.entityUUID IN $exclusionPathwayEntityUUIDs "
			+ "RETURN p")
	List<PathwayNode> findAllBySharedDerivedFromWarehouseEdgeWithExlusion(String entity1UUID, String entity2UUID, List<String> exclusionPathwayEntityUUIDs);

	@Query("MATCH "
			+ "(p:PathwayNode)"
			+ "-[pr:PROV]->"
			+ "(pc:PathwayCollectionNode) "
			+ "WHERE pr.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "RETURN p.entityUUID")
	List<String> findAllPathwaysDirectlyDerivedFromCollections();
*/
	
	/* match (m:MappingNode)-[wm:Warehouse]->(f:FlatSpecies) 
	 * where m.entityUUID = "2f5b5686-c877-4043-9164-1047cf839816" and wm.warehouseGraphEdgeType = "CONTAINS" and f.symbol = "BRCA1" 
	 * with f match (s)<-[w:Warehouse]-(p:PathwayNode) 
	 * where w.warehouseGraphEdgeType = "CONTAINS" and s.entityUUID = f.simpleModelEntityUUID return p;
	 * 
	 */
/*	@Query(""
			+ "MATCH "
			+ "(m:MappingNode)"
			+ "-[wm:Warehouse]->"
			+ "(f:FlatSpecies) "
				+ "WHERE m.entityUUID = $mappingEntityUUID "
				+ "AND wm.warehouseGraphEdgeType = \"CONTAINS\" "
				+ "AND f.entityUUID = $flatSpeciesEntityUUID "
			+ "WITH f MATCH "
				+ "(s)"
				+ "<-[wp:Warehouse]-"
				+ "(p:PathwayNode) "
					+ "WHERE s.entityUUID = f.simpleModelEntityUUID "
					+ "AND wp.warehouseGraphEdgeType = \"CONTAINS\" "
					+ "AND NOT EXISTS { "
						+ "MATCH (p)-[np:PROV]->(pc:PathwayCollectionNode) "
						+ "WHERE np.provenanceGraphEdgeType = \"wasDerivedFrom\" "
					+ "} "
			+ "RETURN p.entityUUID")
	List<String> findAllForFlatSpeciesEntityUUIDInMapping(String flatSpeciesEntityUUID, String mappingEntityUUID);

*/
	@Query(""
			+ "MATCH "
			+ "(g1:SBase)<-[w1:Warehouse]-(p1:PathwayNode)-[w2:Warehouse]->(x:SBase)<-[w3:Warehouse]-(p2:PathwayNode)-[w4:Warehouse]->(g2:SBase) "
			+ "WHERE w1.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "  AND w2.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "  AND w3.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "  AND w4.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "  AND p1.entityUUID = $pathway1UUID "
			+ "  AND p2.entityUUID = $pathway2UUID "
			+ "  AND g1.sBaseName = $geneOfPathway1 "
			+ "  AND g2.sBaseName in $genesOfPathway2 "
			+ "RETURN count(x) " + 
			"")
	int findNumberOfConnectingGenesForTwoPathwaysOfGeneAndGeneSet(String pathway1UUID, String pathway2UUID,
			String geneOfPathway1, Set<String> genesOfPathway2);
	
	/*
	 * match (p:PathwayNode) where p.pathwayIdString = "path_hsa05200" 
	 * with p match (p)-[w:Warehouse]->(sb:SBase) 
	 * where w.warehouseGraphEdgeType = "CONTAINS" 
	 * with sb.entityUUID as simpleModelUUIDs MATCH (m:MappingNode) where m.entityUUID = "2f5b5686-c877-4043-9164-1047cf839816" return simpleModelUUIDs
	 * 
	 */
	@Query("MATCH "
		+ "(p:PathwayNode)-[w:Warehouse]->(sb:SBase) "
		+ "WHERE p.entityUUID = $pathwayUUID "
		+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
		+ "RETURN sb.entityUUID")
	List<String> getSBaseUUIDsOfPathwayNodes(String pathwayUUID);
	
	@Query("MATCH "
			+ "(p:PathwayNode)-[w:Warehouse]->(sb:SBase) "
			+ "WHERE sb.entityUUID = $sBaseEntityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "AND NOT EXISTS { "
				+ "MATCH (p)-[np:PROV]->(pc:PathwayCollectionNode) "
				+ "WHERE np.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "} "
			+ "RETURN p")
	List<PathwayNode> getPathwayNodesOfSBase(String sBaseEntityUUID);
		
	@Query("MATCH "
			+ " (p:PathwayNode)"
			+ "-[w:Warehouse]->"
			+ "(r:SBMLSimpleReaction) "
			+ "WHERE p.entityUUID = $pathwayUUID "
			+ "AND w.warehouseGraphEdgeType=\"CONTAINS\" "
			+ "WITH r "
			+ "MATCH "
			+ "(r)"
			+ "-[rel:IS_PRODUCT|IS_REACTANT|IS_CATALYST]->"
			+ "(s:SBMLSpecies) "
			+ "WHERE s.sBaseSboTerm IN $nodeSBOTerms "
			+ "WITH r, rel, s "
			+ "MATCH "
			+ "(s)"
			+ "-[b:BQ]->"
			+ "(e:ExternalResourceEntity) "
			+ "WHERE b.type = \"BIOLOGICAL_QUALIFIER\" "
			+ "AND b.qualifier IN [\"BQB_HAS_VERSION\", \"BQB_IS\", \"BQB_IS_ENCODED_BY\"] "
			+ "RETURN e, "
			+ "b, "
			+ "s as species, "
			+ "type(rel) as typeOfRelation, "
			+ "r as reaction")
	@Deprecated
	Iterable<MetabolicPathwayReturnType> getAllMetabolicPathwayReturnTypes(String pathwayUUID,
			List<String> nodeSBOTerms);
}
