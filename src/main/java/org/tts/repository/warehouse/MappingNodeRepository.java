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
import org.tts.model.flat.FlatSpecies;
import org.tts.model.warehouse.MappingNode;

public interface MappingNodeRepository extends Neo4jRepository<MappingNode, Long> {

	MappingNode findByEntityUUID(String mappingNodeEntityUUID);

	@Query(value = "MATCH "
			+ "(m:MappingNode)"
			+ "-[w:Warehouse]"
			+ "->(fs:FlatSpecies) "
			+ "WHERE m.entityUUID = $mappingNodeEntityUUID "
			+ "AND w.warehouseGraphEdgeType = \"CONTAINS\" "
			+ "RETURN fs")
	List<FlatSpecies> getMappingFlatSpecies(String mappingNodeEntityUUID);
	
	@Query("MATCH "
			+ "(a:PROV_AGENT)"
			+ "<-[p:PROV]-"
			+ "(m:MappingNode) "
			+ "WHERE a.graphAgentName=$graphAgentName "
			+ "AND p.provenanceGraphEdgeType = \"wasAttributedTo\" "
			+ "RETURN count(m)")
	int getNumberOfMappingNodesAttributedProvAgent(String graphAgentName);
		
	@Query(value = "MATCH "
			+ "(m:MappingNode)"
			+ "-[:PROV]-"
			+ "(a:PROV_AGENT) "
			+ "WHERE "
			+ "a.graphAgentName in $users "
			+ "return m")
	List<MappingNode> findAllFromUsers(List<String> users);

	@Query(value = "MATCH "
			+ "(m:MappingNode)"
			+ "-[:PROV]-"
			+ "(a:PROV_AGENT) "
			+ "WHERE "
			+ "m.isActive = true and "
			+ "a.graphAgentName in $users "
			+ "return m")
	List<MappingNode> findAllActiveFromUsers(List<String> users);
	
	@Query(value = "MATCH "
			+ "(m:MappingNode)"
			+ "-[p:PROV]->"
			+ "(a:PROV_AGENT) "
			+ "WHERE "
			+ "m.entityUUID = $entityUUID "
			+ "AND p.provenanceGraphEdgeType = \"wasAttributedTo\" "
			+ "AND a.graphAgentName = $user "
			+ "RETURN count(m) > 0")
	boolean isMappingNodeAttributedToUser(String entityUUID, String user);

	@Query(value = "MATCH "
			+ "(m:MappingNode)"
			+ "-[p:PROV]->"
			+ "(a:PROV_AGENT) "
			+ "WHERE m.mappingName = $name "
			+ "AND p.provenanceGraphEdgeType = \"wasAttributedTo\" "
			+ "AND a.graphAgentName = $user "
			+ "RETURN m")
	MappingNode findByMappingNameAndUser(String name, String user);
	
}
