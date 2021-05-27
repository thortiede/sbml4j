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

import org.sbml4j.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.sbml4j.model.warehouse.WarehouseGraphNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface WarehouseGraphNodeRepository extends Neo4jRepository<WarehouseGraphNode, Long> {

	@Query(value="MATCH"
			+ "(s:WarehouseGraphNode)"
			+ "-[e:Warehouse]->"
			+ "(p:ProvenanceEntity) "
			+ "WHERE s.entityUUID = $startNodeEntityUUID "
			+ "AND e.warehouseGraphEdgeType = $warehouseGraphEdgeType "
			+ "RETURN p")
	List<ProvenanceEntity> findAllByWarehouseGraphEdgeTypeAndStartNode(WarehouseGraphEdgeType warehouseGraphEdgeType,
			String startNodeEntityUUID);
	
	@Query(value="MATCH"
			+ "(s:ProvenanceEntity)"
			+ "-[e:Warehouse]->"
			+ "(p:ProvenanceEntity) "
			+ "WHERE p.entityUUID = $endNodeEntityUUID "
			+ "AND e.warehouseGraphEdgeType = $warehouseGraphEdgeType "
			+ "RETURN s")
	List<ProvenanceEntity> findAllByWarehouseGraphEdgeTypeAndEndNode(WarehouseGraphEdgeType warehouseGraphEdgeType,
			String endNodeEntityUUID);

	@Query(value = "MATCH p=(e1:ProvenanceEntity)-"
			+ "[w:Warehouse]-"
			+ "(e2:ProvenanceEntity) "
			+ "WHERE e1.entityUUID = $sourceEntityUUID "
			+ "AND e2.entityUUID = $targetEntityUUID "
			+ "AND w.warehouseGraphEdgeType = $edgetype "
			+ "RETURN count(p) > 0")
	boolean areWarehouseEntitiesConnectedWithWarehouseGraphEdgeType(String sourceEntityUUID, 
																String targetEntityUUID,
																WarehouseGraphEdgeType edgetype);

	
	@Query("MATCH "
			+ "(a:PROV_AGENT)"
			+ "<-[p:PROV]-"
			+ "(w:WarehouseGraphNode) "
			+ "WHERE a.graphAgentName=$graphAgentName "
			+ "AND p.provenanceGraphEdgeType = \"wasAttributedTo\" "
			+ "RETURN count(w)")
	int getNumberOfWarehouseGraphNodesAttributedProvAgent(String graphAgentName);
		
	
}
