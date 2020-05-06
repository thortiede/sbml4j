package org.tts.repository.provenance;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.tts.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.tts.model.provenance.ProvenanceEntity;

@Repository
public interface ProvenanceEntityRepository extends Neo4jRepository<ProvenanceEntity, Long> {

	@Query(value = "MATCH p=(e1:ProvenanceEntity)"
				+ "-[pr:PROV]-"
				+ "(e2:ProvenanceEntity) "
				+ "WHERE e1.entityUUID = $sourceEntityUUID "
				+ "AND pr.provenanceGraphEdgeType = $edgetype "
				+ "AND e2.entityUUID = $targetEntityUUID "
				+ "RETURN count(p) > 0")
	boolean areProvenanceEntitiesConnectedWithProvenanceEdgeType(String sourceEntityUUID, String targetEntityUUID,
			ProvenanceGraphEdgeType edgetype);

	
	public ProvenanceEntity findByEntityUUID(String entityUUID);

	@Query(value = "MATCH "
			+ "(p:ProvenanceEntity)"
			+ "<-[pr:PROV]-"
			+ "(c:ProvenanceEntity) "
			+ "WHERE pr.provenanceGraphEdgeType = $edgetype "
			+ "AND c.entityUUID = $startNodeEntityUUID "
			+ "RETURN p")
	ProvenanceEntity findByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType edgetype,
			String startNodeEntityUUID);

	@Query(value = "MATCH "
			+ "(p:ProvenanceEntity)"
			+ "<-[pr:PROV]-"
			+ "(c:ProvenanceEntity) "
			+ "WHERE p.entityUUID = $endNodeEntityUUID "
			+ "AND pr.provenanceGraphEdgeType = $edgetype "
			+ "RETURN c")
	ProvenanceEntity findByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType edgetype,
			String endNodeEntityUUID);
	
	
	@Query(value = "MATCH "
			+ "(p:ProvenanceEntity)"
			+ "<-[pr:PROV]-"
			+ "(c:ProvenanceEntity) "
			+ "WHERE pr.provenanceGraphEdgeType = $edgetype "
			+ "AND c.entityUUID = $startNodeEntityUUID "
			+ "RETURN p")
	Iterable<ProvenanceEntity> findAllByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType edgetype,
			String startNodeEntityUUID);
	
	

	@Query(value = "MATCH "
			+ "(p:ProvenanceEntity)"
			+ "<-[pr:PROV]-"
			+ "(c:ProvenanceEntity) "
			+ "WHERE p.entityUUID = $endNodeEntityUUID "
			+ "AND pr.provenanceGraphEdgeType = $edgetype "
			+ "RETURN c")
	Iterable<ProvenanceEntity> findAllByProvenanceGraphEdgeTypeAndEndNode(ProvenanceGraphEdgeType edgetype,
			String endNodeEntityUUID);


}
