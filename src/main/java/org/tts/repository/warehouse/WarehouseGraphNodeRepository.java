package org.tts.repository.warehouse;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.common.GraphEnum.WarehouseGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;
import org.tts.model.warehouse.WarehouseGraphNode;

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

}
