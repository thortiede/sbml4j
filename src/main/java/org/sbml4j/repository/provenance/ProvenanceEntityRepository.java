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
package org.sbml4j.repository.provenance;

import org.sbml4j.model.common.GraphEnum.ProvenanceGraphEdgeType;
import org.sbml4j.model.provenance.ProvenanceEntity;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

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
	// There could be multiple, use Iterable<ProvenanceEntity> findAll... instead
	@Deprecated
	ProvenanceEntity findByProvenanceGraphEdgeTypeAndStartNode(ProvenanceGraphEdgeType edgetype,
			String startNodeEntityUUID);

	@Query(value = "MATCH "
			+ "(p:ProvenanceEntity)"
			+ "<-[pr:PROV]-"
			+ "(c:ProvenanceEntity) "
			+ "WHERE p.entityUUID = $endNodeEntityUUID "
			+ "AND pr.provenanceGraphEdgeType = $edgetype "
			+ "RETURN c")
	// There could be multiple, use Iterable<ProvenanceEntity> findAll... instead
	@Deprecated
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
