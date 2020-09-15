/*
 * ----------------------------------------------------------------------------
	Copyright 2020 University of Tuebingen 	

	This file is part of SBML4j.

    SBML4j is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SBML4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SBML4j.  If not, see <https://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------------- 
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
		
	// match (m:MappingNode)-[:PROV]-(p:PROV_AGENT) where p.graphAgentName in ["All", 'Irene'] return m, p;
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
	
}
