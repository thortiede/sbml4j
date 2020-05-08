package org.tts.repository.warehouse;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.tts.model.api.Output.MappingReturnType;
import org.tts.model.api.Output.NodeNodeEdge;
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

	MappingNode getByBaseNetworkEntityUUIDAndGeneSymbolAndMinSizeAndMaxSize(String baseNetworkEntityUUID, String geneSymbol, int minSize, int maxSize);
	
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
	
}
