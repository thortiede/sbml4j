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
package org.tts.repository.warehouse;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.warehouse.DatabaseNode;

public interface DatabaseNodeRepository extends Neo4jRepository<DatabaseNode, Long> {

	
	
	@Query("MATCH p="
			+ "(d:DatabaseNode)"
			+ "-[fo:FOR]->"
			+ "(o:Organism) "
			+ "where d.source = $source "
			+ "and d.sourceVersion = $sourceVersion "
			+ "RETURN p")
	List<DatabaseNode> findBySourceAndSourceVersion(String source, String sourceVersion);

	DatabaseNode findByEntityUUID(String entityUUID);

	@Query("MATCH "
			+ "(p:PathwayNode)"
			+ "-[provpw:PROV]->"
			+ "(f:FileNode)"
			+ "-[provfn:PROV]->"
			+ "(d:DatabaseNode)"
			+ "where p.entityUUID = $pathwayEntityUUID "
			+ "and provpw.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "and provfn.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "return d")
	DatabaseNode findByPathway(String pathwayEntityUUID);
	
	@Query("MATCH "
			+ "(pw:PathwayNode)"
			+ "-[provpw:PROV]->"
			+ "(f:FileNode)"
			+ "-[provfn:PROV]->"
			+ "(d:DatabaseNode) "
			+ "where pw.entityUUID in $pathwayEntityUUIDList "
			+ "and provpw.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "and provfn.provenanceGraphEdgeType = \"wasDerivedFrom\" "
			+ "WITH d MATCH p="
			+ "(d)"
			+ "-[fo:FOR]->"
			+ "(o:Organism) "
			+ "return p")
	List<DatabaseNode> findByPathwayList(List<String> pathwayEntityUUIDList);
	
}
